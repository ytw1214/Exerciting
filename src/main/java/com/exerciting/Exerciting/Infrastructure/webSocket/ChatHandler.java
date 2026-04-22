package com.exerciting.Exerciting.Infrastructure.webSocket;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.service.MatchingChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private MatchingChatService matchingChatService;
    private final Map<String, Set<WebSocketSession>> matchingSessions = new ConcurrentHashMap<>();
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            session.sendMessage(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String matchingId = getMatchingId(session);
        matchingSessions.getOrDefault(matchingId, ConcurrentHashMap.newKeySet())
                .remove(session);
    }
    private String getMatchingId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String matchingId = getMatchingId(session);

        matchingSessions
                .computeIfAbsent(matchingId, k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }
}