package com.exerciting.Exerciting.Infrastructure.configuration;

import com.exerciting.Exerciting.Infrastructure.webSocket.ChatHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class SocketConfig implements WebSocketMessageBrokerConfigurer {
    private final ChatHandler chatHandler;
    public SocketConfig(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns("**")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .withSockJS();
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/matching");
        config.setApplicationDestinationPrefixes("/send");
    }
}
