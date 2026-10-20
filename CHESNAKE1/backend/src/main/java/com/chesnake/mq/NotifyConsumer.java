package com.chesnake.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chesnake.config.RabbitConfig;
import com.chesnake.entity.Notification;
import com.chesnake.mapper.NotificationMapper;
import com.chesnake.service.MqIdempotentService;
import com.chesnake.ws.WsPushService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 通知消费者（优化6+11）：异步消息落库为站内通知 + WebSocket 实时推送
 * 手动 Ack：落库成功才确认；失败 Nack(requeue=false) 进死信队列
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyConsumer {

    private final NotificationMapper notificationMapper;
    private final MqIdempotentService idempotentService;
    private final WsPushService wsPushService;

    @RabbitListener(queues = RabbitConfig.NOTIFY_QUEUE)
    public void onNotify(NotifyMessage msg, Message message, Channel channel) throws IOException {
        String msgId = message.getMessageProperties().getMessageId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("收到通知消息: msgId={}, email={}, title={}", msgId, msg.email(), msg.title());
        try {
            if (idempotentService.isProcessed(msgId, "NOTIFY")) {
                channel.basicAck(deliveryTag, false);
                return;
            }
            idempotentService.acquire(msgId, "NOTIFY");
            try {
                Notification n = new Notification();
                n.setEmail(msg.email());
                n.setTitle(msg.title());
                n.setContent(msg.content());
                n.setIsRead(0);
                notificationMapper.insert(n);
            } catch (Exception e) {
                // 落库失败：释放幂等记录，重投后可以重试
                idempotentService.release(msgId, "NOTIFY");
                throw e;
            }
            channel.basicAck(deliveryTag, false);

            // WebSocket 实时推送（含最新未读数）
            Long unread = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                    .eq(Notification::getEmail, msg.email()).eq(Notification::getIsRead, 0));
            wsPushService.pushNotification(msg.email(), msg.title(), msg.content(), unread == null ? 0 : unread);
        } catch (Exception e) {
            log.error("通知消息处理失败，进入死信队列: msgId={}", msgId, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
