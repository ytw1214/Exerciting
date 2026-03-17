package com.exerciting.Exerciting.Infrastructure.configuration;

import com.exerciting.Exerciting.Infrastructure.webSocket.ChatHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class SocketConfig implements WebSocketConfigurer {
    private final ChatHandler chatHandler;
    public SocketConfig(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/websocket");
    }

}
