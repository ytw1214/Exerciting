package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.controller;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageRequestDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageResponseDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository.MatchingChatRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.service.MatchingChatService;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Exception.MatchingChatRoomNotFoundException;
import com.exerciting.Exerciting.Exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MatchingChatController {
    private final MatchingChatService matchingChatService;
    private final MatchingChatRoomRepository matchingChatRoomRepository;
    private final UserRepository userRepository;
    private final MatchingChatRepository matchingChatRepository;

    @MessageMapping("/chat/{chatRoomId}")
    @SendTo("/matching/chat/{chatRoomId}")
    public ChatMessageRequestDto handleMessage(
            @DestinationVariable Long chatRoomId,
            ChatMessageRequestDto dto,
            Principal principal) {
        MatchingChatRoom chatroom = matchingChatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new MatchingChatRoomNotFoundException("해당 채팅방을 찾을 수 없습니다."));
        User user = userRepository.findByUserId(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다."));

        MatchingChat chat = matchingChatService.saveMessage(chatroom, user, dto.message());
        return new ChatMessageRequestDto(chat.getMessage());
    }

    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(
            Long chatRoomId,
            Principal principal) {
        MatchingChatRoom chatRoom = matchingChatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new MatchingChatRoomNotFoundException("해당 채팅방을 찾을 수 없습니다."));
        User user = userRepository.findByUserId(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));
        List<ChatMessageResponseDto> messages = matchingChatService.getMessages(chatRoom);
        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(messages);
    }
}
