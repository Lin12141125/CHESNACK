package com.chesnake.mq;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chesnake.entity.MqOutbox;
import com.chesnake.mapper.MqOutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 生产者 Confirm 回调（优化6）：
 * 交换机收到消息（ack）→ 本地消息表状态置 1；未收到（nack）→ 置 2，等待补偿任务重投。
 * 独立组件，避免与 RabbitTemplate 产生循环依赖。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqConfirmCallback implements RabbitTemplate.ConfirmCallback {

    private final MqOutboxMapper outboxMapper;

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (correlationData == null || correlationData.getId() == null) {
            return;
        }
        String msgId = correlationData.getId();
        try {
            outboxMapper.update(null, new LambdaUpdateWrapper<MqOutbox>()
                    .eq(MqOutbox::getMsgId, msgId)
                    .set(MqOutbox::getStatus, ack ? 1 : 2));
            if (!ack) {
                log.error("[MQ Confirm] 消息未到达交换机 msgId={}, cause={}，等待补偿重投", msgId, cause);
            }
        } catch (Exception e) {
            log.error("[MQ Confirm] 更新本地消息表失败 msgId={}", msgId, e);
        }
    }
}
