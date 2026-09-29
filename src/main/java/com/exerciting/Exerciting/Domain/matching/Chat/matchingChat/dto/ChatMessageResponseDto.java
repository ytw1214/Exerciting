package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;

import java.time.LocalDateTime;

public record ChatMessageResponseDto(
        Long senderId,
        String message,
        LocalDateTime sendAt,
        String senderName   // 닉네임. 가입 때 받은 실명(name)은 채팅에 노출하지 않는다
) {
    public static ChatMessageResponseDto from(MatchingChat chat) {
        return new ChatMessageResponseDto(
                chat.getSender().getId(),
                chat.getMessage(),
                chat.getSendAt(),
                chat.getSender().getNickname()
        );
    }
}
