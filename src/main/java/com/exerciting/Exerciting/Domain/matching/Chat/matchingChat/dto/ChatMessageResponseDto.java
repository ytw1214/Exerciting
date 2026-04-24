package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto;

import java.time.LocalDateTime;

public record ChatMessageResponseDto(
        String message,
        LocalDateTime sendAt,
        String senderName
) {
}
