package com.chesnake.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 注册（优化11）：/ws/notify 端点，握手时做 JWT 认证
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotifyWebSocketHandler notifyWebSocketHandler;
    private final WsAuthHandshakeInterceptor wsAuthHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notifyWebSocketHandler, "/ws/notify")
                .addInterceptors(wsAuthHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
