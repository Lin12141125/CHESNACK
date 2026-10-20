package com.chesnake.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chesnake.entity.MqOutbox;
import com.chesnake.mapper.MqOutboxMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 生产者可靠性（优化6）：本地消息表 + Confirm 回调 + 补偿重投
 *
 * 发送链路：业务落库(mq_outbox, status=0) → convertAndSend(带 msgId CorrelationData)
 * Confirm 回调：ack → status=1；nack → status=2
 * 补偿任务：定时扫描「待确认/确认失败且超过 60s」的记录重新投递，
 *          重复投递由消费侧幂等表 + 业务唯一索引兜底，不会重复处理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MqOutboxService {

    /** 超时未确认的兜底等待时间（秒） */
    private static final long RESEND_AFTER_SECONDS = 60;

    private final MqOutboxMapper outboxMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 可靠投递：消息先落库再发送。
     * 落库失败（DB/表异常）向上抛异常，由调用方回滚业务；投递失败仅记录日志，
     * 记录已落库（status=0），由补偿任务重投。
     */
    public String send(String exchange, String routingKey, Object payload) {
        String msgId = UUID.randomUUID().toString().replace("-", "");
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("消息序列化失败", e);
        }

        MqOutbox outbox = new MqOutbox();
        outbox.setMsgId(msgId);
        outbox.setExchange(exchange);
        outbox.setRoutingKey(routingKey);
        outbox.setPayload(json);
        outbox.setStatus(0);
        outboxMapper.insert(outbox);

        try {
            // messageId 写入消息属性：消费侧据此做幂等；CorrelationData 用于 Confirm 关联
            rabbitTemplate.convertAndSend(exchange, routingKey, payload,
                    m -> {
                        m.getMessageProperties().setMessageId(msgId);
                        return m;
                    },
                    new CorrelationData(msgId));
        } catch (Exception e) {
            // 已落库，补偿任务会重投；不向上抛，避免调用方重复回滚
            log.error("[MQ] 消息投递异常（已落库，等待补偿重投）: msgId={}", msgId, e);
        }
        return msgId;
    }

    /**
     * Confirm 回调由 {@link com.chesnake.mq.MqConfirmCallback} 独立组件处理
     * （拆分为独立组件是为了避免 RabbitTemplate ↔ 本服务 的循环依赖）。
     */

    /**
     * 补偿任务：扫描「待确认 / 确认失败」且超过 60 秒的消息重新投递。
     * 消费侧幂等表保证重投不会重复处理。
     */
    @Scheduled(fixedDelay = 30_000)
    public void resendUnconfirmed() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(RESEND_AFTER_SECONDS);
        List<MqOutbox> records = outboxMapper.selectList(
                new LambdaQueryWrapper<MqOutbox>()
                        .ne(MqOutbox::getStatus, 1)
                        .lt(MqOutbox::getUpdateTime, threshold)
                        .last("LIMIT 100"));
        for (MqOutbox record : records) {
            try {
                // 以 JSON 消息直接重投（保留原始消息体，消费端转换器可正确反序列化）
                Message message = MessageBuilder
                        .withBody(record.getPayload().getBytes(StandardCharsets.UTF_8))
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .setMessageId(record.getMsgId())
                        .build();
                rabbitTemplate.send(record.getExchange(), record.getRoutingKey(),
                        message, new CorrelationData(record.getMsgId()));
                // touch update_time，避免同一轮反复重投
                outboxMapper.update(null, new LambdaUpdateWrapper<MqOutbox>()
                        .eq(MqOutbox::getId, record.getId())
                        .set(MqOutbox::getStatus, 0)
                        .set(MqOutbox::getUpdateTime, LocalDateTime.now()));
                log.info("[MQ 补偿] 重新投递消息 msgId={}", record.getMsgId());
            } catch (Exception e) {
                log.error("[MQ 补偿] 重新投递失败 msgId={}", record.getMsgId(), e);
            }
        }
    }
}
