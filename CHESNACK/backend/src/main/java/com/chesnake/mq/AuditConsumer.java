package com.chesnake.mq;

import com.chesnake.config.RabbitConfig;
import com.chesnake.entity.AdminLog;
import com.chesnake.mapper.AdminLogMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 管理员操作审计消费者（优化6）：手动 Ack，落库成功才确认；失败进死信队列
 */
@Slf4j
@Component
public class AuditConsumer {

    private final AdminLogMapper adminLogMapper;

    public AuditConsumer(AdminLogMapper adminLogMapper) {
        this.adminLogMapper = adminLogMapper;
    }

    @RabbitListener(queues = RabbitConfig.AUDIT_QUEUE)
    public void onAudit(AuditMessage msg, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            AdminLog adminLog = new AdminLog();
            adminLog.setUsername(msg.username());
            adminLog.setAction(msg.action());
            adminLog.setDetail(msg.detail());
            adminLogMapper.insert(adminLog);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("审计日志落库失败，进入死信队列", e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
