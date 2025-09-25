package com.mrokga.carrot_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 구독 prefix
        config.enableSimpleBroker("/sub");
        // 발행 prefix
        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat", "/ws-notification")
                .setAllowedOriginPatterns("*") // 배포 시에는 프론트 도메인만 허용
                .withSockJS();

        //postman 테스트용
        registry.addEndpoint("/ws-notification")
                .setAllowedOriginPatterns("*");
    }
}
