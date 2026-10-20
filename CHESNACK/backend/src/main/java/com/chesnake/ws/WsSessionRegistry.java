package com.chesnake.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket 会话注册表：email → 该用户的所有在线会话（支持多端登录）
 */
@Component
public class WsSessionRegistry {

    private final ConcurrentHashMap<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public static final String ATTR_EMAIL = "chesnake.email";

    public void register(String email, WebSocketSession session) {
        sessions.computeIfAbsent(email, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void unregister(String email, WebSocketSession session) {
        Set<WebSocketSession> set = sessions.get(email);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                sessions.remove(email, set);
            }
        }
    }

    public Set<WebSocketSession> sessionsOf(String email) {
        return sessions.getOrDefault(email, Set.of());
    }
}
