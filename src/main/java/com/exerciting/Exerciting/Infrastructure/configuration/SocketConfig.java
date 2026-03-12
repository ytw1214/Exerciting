package com.exerciting.Exerciting.Infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(), "/websocket")
                .setAllowedOrigins("*");

    }
    @Bean
    org.springframework.web.socket.WebSocketHandler webSocketHandler() {
        return new WebSocketHandler();
    }
    @Bean
    private WebSocketHandler webSocketHandler() {
        return new WebSocketHandler();
    }
}
