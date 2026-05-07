package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto;

import java.time.LocalDateTime;

public record ChatMessageRequestDto(
        Long senderId,
        String message
) {

}
