package com.familyshop.config;

import com.familyshop.ws.WsAuthHandshakeInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WsAuthHandshakeInterceptor wsAuthHandshakeInterceptor;

    public WebSocketConfig(WsAuthHandshakeInterceptor wsAuthHandshakeInterceptor) {
        this.wsAuthHandshakeInterceptor = wsAuthHandshakeInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173", "http://localhost", "http://127.0.0.1")
                .addInterceptors(wsAuthHandshakeInterceptor)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // брокер будет обслуживать топики вида /topic/...
        registry.enableSimpleBroker("/topic");
        // если когда-нибудь захочешь @MessageMapping — используем префикс /app
        registry.setApplicationDestinationPrefixes("/app");
    }
}