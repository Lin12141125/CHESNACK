package com.chesnake.mq;

import com.chesnake.config.RabbitConfig;
import com.chesnake.entity.Community;
import com.chesnake.entity.Follow;
import com.chesnake.entity.Notification;
import com.chesnake.mapper.CommunityMapper;
import com.chesnake.mapper.FollowMapper;
import com.chesnake.mapper.NotificationMapper;
import com.chesnake.service.MqIdempotentService;
import com.chesnake.ws.WsPushService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * 社区事件消费者（优化6+11）：
 * - 发帖：异步通知作者的粉丝
 * - 评论：异步通知帖子作者
 * 落库后 WebSocket 实时推送；手动 Ack，失败进死信队列
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityEventConsumer {

    private final NotificationMapper notificationMapper;
    private final FollowMapper followMapper;
    private final CommunityMapper communityMapper;
    private final MqIdempotentService idempotentService;
    private final WsPushService wsPushService;

    @RabbitListener(queues = RabbitConfig.COMMUNITY_QUEUE)
    public void onEvent(CommunityEventMessage msg, Message message, Channel channel) throws IOException {
        String msgId = message.getMessageProperties().getMessageId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if (idempotentService.isProcessed(msgId, "COMMUNITY")) {
                channel.basicAck(deliveryTag, false);
                return;
            }
            idempotentService.acquire(msgId, "COMMUNITY");
            try {
                doBusiness(msg);
            } catch (Exception e) {
                idempotentService.release(msgId, "COMMUNITY");
                throw e;
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("社区事件处理失败，进入死信队列: msgId={}", msgId, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void doBusiness(CommunityEventMessage msg) {
        if (CommunityEventMessage.TYPE_POST.equals(msg.type())) {
            // 通知作者的所有粉丝（按粉丝 email 落库 + 推送；老数据无 email 回退用户名）
            List<Follow> followers = followMapper.selectList(
                    new LambdaQueryWrapper<Follow>().eq(Follow::getAccount, msg.toEmail()));
            int notified = 0;
            for (Follow f : followers) {
                String targetEmail = (f.getFolloweeEmail() != null && !f.getFolloweeEmail().isBlank())
                        ? f.getFolloweeEmail() : f.getFollowee();
                insertNotify(targetEmail, "你关注的人发布了新帖子",
                        msg.fromUser() + " 发布了帖子：" + msg.text());
                notified++;
            }
            log.info("发帖事件处理完成, 通知 {} 位粉丝", notified);
        } else if (CommunityEventMessage.TYPE_COMMENT.equals(msg.type())) {
            Community post = communityMapper.selectById(msg.postId());
            if (post == null || post.getAccount() == null) return;
            if (post.getAccount().equals(msg.toEmail())) return; // 不通知自己
            insertNotify(post.getAccount(), "你的帖子收到新评论",
                    msg.fromUser() + " 评论了你的帖子：" + msg.text());
            log.info("评论事件处理完成, 已通知帖子作者 {}", post.getAccount());
        }
    }

    private void insertNotify(String email, String title, String content) {
        Notification n = new Notification();
        n.setEmail(email);
        n.setTitle(title);
        n.setContent(content);
        n.setIsRead(0);
        notificationMapper.insert(n);
        // WebSocket 实时推送
        Long unread = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getEmail, email).eq(Notification::getIsRead, 0));
        wsPushService.pushNotification(email, title, content, unread == null ? 0 : unread);
    }
}
