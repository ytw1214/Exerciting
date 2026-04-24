package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.controller;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageResponseDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository.MatchingChatRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.service.MatchingChatService;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Exception.MatchingChatRoomNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MatchingChatController {
    private final MatchingChatService matchingChatService;
    private final MatchingChatRoomRepository matchingChatRoomRepository;

    public ChatMessageDto handleMessage(
            @DestinationVariable Long chatRoomId,
            ChatMessageDto dto) {
        MatchingChatRoom chatroom = matchingChatRoomRepository.findById(chatRoomId)
                .orElseThrow (() -> new MatchingChatRoomNotFoundException("해당 채팅방을 찾을 수 없습니다."));
        matchingChatService.saveMessage(chatroom, dto.senderId(), dto.message());
    }
    @MessageMapping("/chat/{chatRoomId}")
    public ChatMessageResponseDto handleMessage(Principal principal) {
        User sender = userRepository.findById(Long.parseLong(principal.getName()))
    }
}
