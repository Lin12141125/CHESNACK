package com.chesnake.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 站内通知 WebSocket 处理器：
 * - 连接建立：注册会话（email 来自握手 attributes）
 * - 心跳：收到 "ping" 回复 "pong"（前端定时发送保活）
 * - 连接关闭：注销会话
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyWebSocketHandler extends TextWebSocketHandler {

    private final WsSessionRegistry registry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String email = (String) session.getAttributes().get(WsSessionRegistry.ATTR_EMAIL);
        if (email == null) {
            try {
                session.close(CloseStatus.POLICY_VIOLATION);
            } catch (Exception ignored) {
            }
            return;
        }
        registry.register(email, session);
        log.debug("WS 连接建立: email={}, session={}", email, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if ("ping".equals(message.getPayload())) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String email = (String) session.getAttributes().get(WsSessionRegistry.ATTR_EMAIL);
        if (email != null) {
            registry.unregister(email, session);
            log.debug("WS 连接关闭: email={}, session={}", email, session.getId());
        }
    }
}
