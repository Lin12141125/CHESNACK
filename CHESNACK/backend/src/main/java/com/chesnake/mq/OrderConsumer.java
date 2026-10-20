package com.chesnake.mq;

import com.chesnake.config.RabbitConfig;
import com.chesnake.entity.Cart;
import com.chesnake.entity.Goods;
import com.chesnake.entity.Orders;
import com.chesnake.mapper.CartMapper;
import com.chesnake.mapper.GoodsMapper;
import com.chesnake.mapper.OrdersMapper;
import com.chesnake.service.GoodsService;
import com.chesnake.service.MqIdempotentService;
import com.chesnake.ws.WsPushService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 下单支付异步消费者（优化5+6 升级版）：生成订单 + 扣库存 + 清购物车 + 商品热度
 *
 * 订单并发「N抢1」防线：
 * ① pay 接口 DB 预检 + Redis Lua 预扣（快速失败，压力前移）
 * ② 本消费者 DB 原子条件扣减 `UPDATE ... SET stock = stock - n WHERE stock >= n`（最终防线，防超卖）
 * ③ 消费幂等表（与业务同事务，回滚不残留） + (order_no, good_id) 唯一索引 DuplicateKey 兜底
 * ④ 手动 Ack：业务事务提交成功才 ack；失败 basicNack(requeue=false) 进死信队列
 * ⑤ 预扣库存回补统一由死信耗尽分支执行（避免重试循环重复回补导致视图虚增）
 * ⑥ 成功后 WebSocket 实时推送订单完成通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrdersMapper ordersMapper;
    private final GoodsMapper goodsMapper;
    private final CartMapper cartMapper;
    private final GoodsService goodsService;
    private final MqIdempotentService idempotentService;
    private final WsPushService wsPushService;
    private final TransactionTemplate transactionTemplate;

    @RabbitListener(queues = RabbitConfig.ORDER_QUEUE)
    public void onOrder(OrderMessage msg, Message message, Channel channel) throws IOException {
        String msgId = message.getMessageProperties().getMessageId();
        // 幂等键优先用消息级 msgId（死信重投时保留）；兼容旧消息回退 orderNo
        String idempotentKey = (msgId == null || msgId.isBlank()) ? "order:" + msg.orderNo() : msgId;
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("收到下单消息: msgId={}, orderNo={}, email={}, items={}",
                msgId, msg.orderNo(), msg.email(), msg.items().size());

        try {
            // 幂等第一道：重复投递/重复消费探测（已处理则直接 ack）
            if (idempotentService.isProcessed(idempotentKey, "ORDER")) {
                log.info("订单消息已处理过(幂等)，跳过: orderNo={}", msg.orderNo());
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 业务 + 幂等抢占在同一事务：任一步失败整体回滚（幂等记录不残留，重投可重试）
            transactionTemplate.executeWithoutResult(status -> doBusiness(msg, idempotentKey));

            // 事务提交成功才确认消息
            channel.basicAck(deliveryTag, false);
            wsPushService.pushOrder(msg.email(), msg.orderNo());
            log.info("订单 {} 处理完成", msg.orderNo());
        } catch (Exception e) {
            // 注意：此处不做 Redis 预扣回补。若回补后再经死信重试成功，DB 已扣而 Redis 视图未扣，
            // 反而造成视图虚增；若每次失败都回补（重试循环 N 次），库存会虚增 N+1 倍。
            // 预扣库存保持「冻结」，统一由死信耗尽分支（DeadLetterConsumer）做一次性回补。
            log.error("订单消息处理失败，Nack 进入死信队列: orderNo={}", msg.orderNo(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void doBusiness(OrderMessage msg, String idempotentKey) {
        // 幂等第二道：事务内抢占记录（并发重复时 DuplicateKey 抛异常 → 整体回滚）
        idempotentService.acquire(idempotentKey, "ORDER");

        List<Long> successIds = new ArrayList<>();
        for (OrderMessage.Item item : msg.items()) {
            // N抢1 最终防线：原子条件扣减，库存不足返回 0，天然防超卖
            int rows = goodsMapper.deductStock(item.goodId(), item.amount());
            if (rows <= 0) {
                // Redis 已预扣成功而 DB 扣减失败 = 库存视图不一致，抛异常回滚重试
                throw new IllegalStateException("商品 " + item.goodId() + " DB 库存不足，Redis/DB 视图不一致");
            }
            Orders order = new Orders();
            order.setOrderNo(msg.orderNo());
            order.setEmail(msg.email());
            order.setGoodId(item.goodId());
            order.setAmount(item.amount());
            order.setPrice(item.price());
            order.setStatus(1);
            try {
                ordersMapper.insert(order);
            } catch (DuplicateKeyException e) {
                // 幂等第三道：并发/重投撞 (order_no, good_id) 唯一索引，该商品行已处理过。
                // 本次 deductStock 已把库存扣走，必须恢复，否则重投会重复扣库存
                goodsMapper.restoreStock(item.goodId(), item.amount());
                log.info("订单 {} 商品 {} 重复插入已忽略并恢复库存", msg.orderNo(), item.goodId());
                continue;
            }

            // 商品热度 +3（原子 SQL）
            goodsMapper.update(null, new LambdaUpdateWrapper<Goods>()
                    .setSql("score = score + 3")
                    .eq(Goods::getId, item.goodId()));
            successIds.add(item.goodId());
        }

        // 清购物车：只清理成功扣库存的商品
        if (!successIds.isEmpty()) {
            cartMapper.delete(new LambdaQueryWrapper<Cart>()
                    .eq(Cart::getEmail, msg.email())
                    .in(Cart::getGoodId, successIds));
        }

        // 缓存一致性：下单改变库存/销量/热度 → 失效详情与列表缓存
        goodsService.evictDetails(successIds);
        goodsService.evictListCache();
    }
}
