package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.controller;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageRequestDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageResponseDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.service.MatchingChatService;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.service.MatchingChatRoomService;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import com.exerciting.Exerciting.Exception.MatchingChatRoomNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MatchingChatController {
    private final MatchingChatService matchingChatService;
    private final UserService userService;
    private final MatchingChatRoomService matchingChatRoomService;
    @MessageMapping("/chat/{chatRoomId}")
    @SendTo("/sub/matching/chat/{chatRoomId}")
    public ChatMessageResponseDto sendMessage(
            @DestinationVariable Long chatRoomId,
            ChatMessageRequestDto dto,
            Principal principal) {
        MatchingChatRoom chatRoom = matchingChatRoomService.findByRoomId(chatRoomId);
        User user = userService.getUserByUserId(principal.getName());

        MatchingChat chat = matchingChatService.saveMessage(chatRoom, user, dto.message());
        return new ChatMessageResponseDto(
                chat.getSender().getId(),
                chat.getMessage(),
                chat.getSendAt(),
                chat.getSender().getName(),
                chat.isRead()
        );
    }

    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(
            Long chatRoomId,
            Principal principal) {
        MatchingChatRoom chatRoom = matchingChatRoomService.findByRoomId(chatRoomId);
        User user = userService.getUserByUserId(principal.getName());
        List<ChatMessageResponseDto> messages = matchingChatService.getMessages(chatRoom,user);
        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(messages);
    }
}
