package com.chesnake.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chesnake.common.BusinessException;
import com.chesnake.config.RabbitConfig;
import com.chesnake.dto.PayDTO;
import com.chesnake.entity.CartVO;
import com.chesnake.entity.Goods;
import com.chesnake.entity.OrderVO;
import com.chesnake.mapper.CartMapper;
import com.chesnake.mapper.GoodsMapper;
import com.chesnake.mapper.OrdersMapper;
import com.chesnake.mq.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务（优化5 秒杀链路升级）
 *
 * 完整下单链路：
 * ① DB 库存预检（快速失败，拦截明显不足的请求）
 * ② 信号量限流：控制同时在途的下单请求数，防止请求堆积
 * ③ Redis Lua 原子预扣：多商品「检查+扣减」一条脚本完成，任一不足整体回滚，
 *    把库存判断压力从 DB 前移到 Redis
 * ④ 本地消息表可靠投递（MqOutboxService）：生成订单/扣库存/清购物车由消费者异步完成，
 *    Confirm 未确认由补偿任务重投
 * ⑤ 消费者侧：消费幂等表 + DB 原子条件扣减（最终防线防超卖）
 *
 * 库存视图：Redis 预扣为运行期视图，DB 始终是唯一权威。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    /** 信号量等待超时 */
    private static final long SEMAPHORE_TIMEOUT_MS = 3000;

    private final OrdersMapper ordersMapper;
    private final CartMapper cartMapper;
    private final GoodsMapper goodsMapper;
    private final SeckillStockService stockService;
    private final SeckillLimiter seckillLimiter;
    private final MqOutboxService outboxService;

    /**
     * 提交支付：库存预检 → 信号量 → Redis Lua 预扣 → 可靠投递异步消息
     */
    public String pay(String email, PayDTO dto) {
        List<CartVO> all = cartMapper.selectCartVO(email);
        List<OrderMessage.Item> items = new ArrayList<>();
        for (CartVO cart : all) {
            if (dto.goodIds().contains(cart.getGoodId())) {
                items.add(new OrderMessage.Item(cart.getGoodId(), cart.getAmount(), cart.getPrice()));
            }
        }
        if (items.isEmpty()) {
            throw new BusinessException("购物车中没有选中的商品");
        }

        // ① 第一道防线：DB 库存预检，明显不足快速失败，拦截无效流量
        List<Long> ids = items.stream().map(OrderMessage.Item::goodId).toList();
        Map<Long, Goods> goodsMap = new HashMap<>();
        for (Goods g : goodsMapper.selectBatchIds(ids)) {
            goodsMap.put(g.getId(), g);
        }
        for (OrderMessage.Item item : items) {
            Goods goods = goodsMap.get(item.goodId());
            if (goods == null) {
                throw new BusinessException("商品不存在，请刷新购物车");
            }
            if (goods.getStock() < item.amount()) {
                throw new BusinessException("商品「" + goods.getName() + "」库存不足（剩余 " + goods.getStock() + "）");
            }
        }

        // ② 信号量限流：同时在途下单请求有上限，防止堆积拖垮线程池/DB
        boolean acquired = seckillLimiter.tryAcquire(SEMAPHORE_TIMEOUT_MS);
        if (!acquired) {
            throw new BusinessException("下单人数过多，请稍后再试");
        }
        try {
            // ③ Redis Lua 原子预扣（任一商品不足脚本内自动回滚，返回 false）
            List<SeckillStockService.StockItem> stockItems = items.stream()
                    .map(i -> new SeckillStockService.StockItem(i.goodId(), i.amount()))
                    .toList();
            boolean deducted = stockService.tryDeduct(stockItems);
            if (!deducted) {
                // 预热视图可能缺失（Redis 重启/未预热）：回源 DB 预热后重试一次
                ensurePreheated(stockItems, goodsMap);
                if (!stockService.tryDeduct(stockItems)) {
                    throw new BusinessException("商品库存不足，下单失败");
                }
            }

            // ④ 本地消息表可靠投递：订单/扣库存/清购物车在消费者异步完成
            String orderNo = generateOrderNo();
            OrderMessage message = new OrderMessage(orderNo, email, items);
            try {
                String msgId = outboxService.send(RabbitConfig.EXCHANGE, RabbitConfig.ORDER_ROUTING_KEY, message);
                log.info("下单消息已可靠投递: orderNo={}, msgId={}", orderNo, msgId);
            } catch (Exception e) {
                // 本地消息表落库失败（DB/表异常）：回滚 Redis 预扣，避免库存凭空消失
                stockService.rollback(stockItems);
                log.error("下单消息落库失败，已回滚预扣库存: orderNo={}", orderNo, e);
                throw new BusinessException("下单失败，请稍后重试");
            }
            return orderNo;
        } finally {
            seckillLimiter.release();
        }
    }

    /** 订单分页（含商品信息，一次查询返回），支持关键词/日期区间/是否已评价筛选 */
    public Page<OrderVO> list(String email, long page, long size, String keyword,
                              String startDate, String endDate, Integer evaluated) {
        return ordersMapper.selectOrderVO(new Page<>(page, size), email, keyword, startDate, endDate, evaluated);
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return time + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    /**
     * 预热视图缺失补偿：Redis 中无该商品库存 key 时，回源 DB 补齐（SETNX 不覆盖已有值）
     */
    private void ensurePreheated(List<SeckillStockService.StockItem> items, Map<Long, Goods> goodsMap) {
        for (SeckillStockService.StockItem item : items) {
            if (stockService.remaining(item.goodId()) == null) {
                Goods goods = goodsMap.get(item.goodId());
                if (goods != null) {
                    stockService.preheatIfAbsent(item.goodId(), goods.getStock());
                    log.info("库存视图缺失，已回源预热: goodId={}, stock={}", item.goodId(), goods.getStock());
                }
            }
        }
    }
}
