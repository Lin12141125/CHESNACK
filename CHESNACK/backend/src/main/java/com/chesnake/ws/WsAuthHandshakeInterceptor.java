package com.chesnake.ws;

import com.chesnake.security.JwtUtil;
import com.chesnake.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手认证：浏览器 WS 无法携带自定义 Header，token 通过 query 参数传递。
 * 校验 JWT 合法性 + 黑名单，通过后把 email 写入握手 attributes。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query == null || !query.contains("token=")) {
            return false;
        }
        String token = null;
        for (String pair : query.split("&")) {
            if (pair.startsWith("token=")) {
                token = pair.substring(6);
                break;
            }
        }
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            if (tokenBlacklistService.isBlacklisted(token)) {
                return false;
            }
            String email = jwtUtil.parse(token).getSubject();
            attributes.put(WsSessionRegistry.ATTR_EMAIL, email);
            return true;
        } catch (Exception e) {
            log.debug("WS 握手 token 非法: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
