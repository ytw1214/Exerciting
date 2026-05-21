package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;

import java.time.LocalDateTime;

public record ChatMessageResponseDto(
        Long senderId,
        String message,
        LocalDateTime sendAt,
        String senderName
) {
    public static ChatMessageResponseDto from(MatchingChat chat) {
        return new ChatMessageResponseDto(
                chat.getSender().getId(),
                chat.getMessage(),
                chat.getSendAt(),
                chat.getSender().getName()
        );
    }
}
