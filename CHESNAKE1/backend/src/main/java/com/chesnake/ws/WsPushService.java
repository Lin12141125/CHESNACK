package com.chesnake.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * WebSocket 推送服务（优化11）：站内通知 / 订单状态实时推送到在线用户
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WsPushService {

    /** 消息类型：站内通知 */
    public static final String TYPE_NOTIFICATION = "notification";
    /** 消息类型：订单完成 */
    public static final String TYPE_ORDER = "order";

    private final WsSessionRegistry registry;
    private final ObjectMapper objectMapper;

    /** 推送站内通知（含最新未读数） */
    public void pushNotification(String email, String title, String content, long unread) {
        push(email, Map.of(
                "type", TYPE_NOTIFICATION,
                "title", title,
                "content", content,
                "unread", unread));
    }

    /** 推送订单完成 */
    public void pushOrder(String email, String orderNo) {
        push(email, Map.of(
                "type", TYPE_ORDER,
                "orderNo", orderNo,
                "title", "订单处理完成",
                "content", "订单 " + orderNo + " 已支付成功"));
    }

    private void push(String email, Map<String, Object> message) {
        if (email == null) {
            return;
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("WS 消息序列化失败: {}", message, e);
            return;
        }
        for (WebSocketSession session : registry.sessionsOf(email)) {
            try {
                if (session.isOpen()) {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(json));
                    }
                }
            } catch (Exception e) {
                log.debug("WS 推送失败(忽略): email={}, session={}", email, session.getId());
            }
        }
    }
}
