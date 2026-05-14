package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto;

import java.time.LocalDateTime;

public record ChatMessageResponseDto(
        Long senderId,
        String message,
        LocalDateTime sendAt,
        String senderName
) {
}
