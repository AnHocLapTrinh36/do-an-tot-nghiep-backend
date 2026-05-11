package com.example.Backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration // Đánh dấu lớp là cấu hình Spring, sẽ được nạp bởi container
@EnableWebSocketMessageBroker // Bật hạ tầng WebSocket + STOMP message broker trong Spring
public class WsConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                // Đăng ký endpoint STOMP cho client kết nối (ws://host/ws-pos). tức là client kết nối web socket bằng đường đẫn ws://localhost:8080/ws-pos
                .addEndpoint("/ws-pos")
                // Cho phép CORS từ mọi nguồn (pattern)
                .setAllowedOriginPatterns("*")
                // Bật SockJS fallback để hỗ trợ trình duyệt không có WebSocket
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Bật "simple broker" nội bộ cho các topic mà client có thể subscribe
        // Ví dụ client subscribe /topic/updates để nhận broadcast
        registry.enableSimpleBroker("/topic");

        // Tiền tố cho các message client gửi vào app (đến @MessageMapping)
        // Ví dụ gửi tới /app/order sẽ được map vào @MessageMapping("/order")
        registry.setApplicationDestinationPrefixes("/app");
    }
}