package com.chesnake.mq;

import com.chesnake.config.RabbitConfig;
import com.chesnake.entity.MqDeadRecord;
import com.chesnake.mapper.MqDeadRecordMapper;
import com.chesnake.service.SeckillStockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 死信队列消费者（优化6 补偿闭环）：
 * 1. 重试计数 < 上限：重新投递回原业务队列（自定义头记录重试次数），形成「重试循环」
 * 2. 重试耗尽：落 mq_dead_record 补偿表，供人工/定时补偿
 * 3. 订单类死信：解析消息体回补 Redis 预扣库存，保证库存视图一致
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final MqDeadRecordMapper deadRecordMapper;
    private final SeckillStockService stockService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitConfig.DEAD_QUEUE)
    public void onDead(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            Integer retryCount = message.getMessageProperties().getHeader(RabbitConfig.RETRY_COUNT_HEADER);
            int current = retryCount == null ? 0 : retryCount;
            String body = new String(message.getBody(), StandardCharsets.UTF_8);

            if (current < RabbitConfig.DEAD_RETRY_LIMIT) {
                // 未达重试上限：重投回原业务队列（原队列名从 x-death 死亡头解析）
                String originalQueue = resolveOriginalQueue(message);
                if (originalQueue == null) {
                    log.error("[死信] 无法解析原队列，直接落补偿表: {}", body);
                    saveDeadRecord(message, body, "无法解析原队列");
                } else {
                    Message retry = MessageBuilder
                            .withBody(message.getBody())
                            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                            .setHeader(RabbitConfig.RETRY_COUNT_HEADER, current + 1)
                            .build();
                    if (message.getMessageProperties().getMessageId() != null) {
                        retry.getMessageProperties().setMessageId(message.getMessageProperties().getMessageId());
                    }
                    // 使用默认交换机，routingKey=队列名 直接投递到原队列
                    rabbitTemplate.send("", originalQueue, retry);
                    log.info("[死信] 第 {} 次重试，已重投队列 {}: msgId={}",
                            current + 1, originalQueue, message.getMessageProperties().getMessageId());
                }
            } else {
                // 重试耗尽：落补偿表 + 订单消息回补预扣库存
                saveDeadRecord(message, body, "重试 " + current + " 次仍失败");
                rollbackOrderStock(body);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[死信] 处理死信消息失败", e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void saveDeadRecord(Message message, String body, String error) {
        try {
            MqDeadRecord record = new MqDeadRecord();
            record.setMsgId(message.getMessageProperties().getMessageId());
            record.setQueue(resolveOriginalQueue(message));
            record.setBody(body.length() > 4000 ? body.substring(0, 4000) : body);
            record.setError(error);
            record.setStatus(0);
            deadRecordMapper.insert(record);
            log.error("[死信] 已落补偿表: queue={}, msgId={}", record.getQueue(), record.getMsgId());
        } catch (Exception e) {
            log.error("[死信] 落补偿表失败", e);
        }
    }

    /**
     * 从 RabbitMQ 死亡头解析原业务队列名：
     * x-death 是一个数组，取第一条记录的 queue 字段
     */
    @SuppressWarnings("unchecked")
    private String resolveOriginalQueue(Message message) {
        Object xDeath = message.getMessageProperties().getHeader("x-death");
        if (xDeath instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof java.util.Map<?, ?> map) {
                Object queue = map.get("queue");
                if (queue != null) {
                    return queue.toString();
                }
            }
        }
        // 兼容：RabbitMQ 在消息属性上也提供 x-first-death-queue
        Object fallback = message.getMessageProperties().getHeader("x-first-death-queue");
        return fallback == null ? null : fallback.toString();
    }

    /**
     * 订单死信：解析消息体回补 Redis 预扣库存（DB 已回滚，Redis 视图同步恢复）
     */
    private void rollbackOrderStock(String body) {
        try {
            OrderMessage order = objectMapper.readValue(body, OrderMessage.class);
            if (order != null && order.items() != null) {
                stockService.rollback(order.items().stream()
                        .map(i -> new SeckillStockService.StockItem(i.goodId(), i.amount()))
                        .toList());
                log.warn("[死信] 订单 {} 回补预扣库存", order.orderNo());
            }
        } catch (Exception ignored) {
            // 非订单消息，忽略
        }
    }
}
