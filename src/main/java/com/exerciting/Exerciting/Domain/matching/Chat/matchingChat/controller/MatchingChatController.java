package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.controller;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageRequestDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageResponseDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.service.MatchingChatService;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.service.MatchingChatRoomService;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.entity.UserDetails;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

        return matchingChatService.saveMessage(chatRoom, user, dto.message());
    }
    @GetMapping("/api/v1/chat/{chatRoomId}/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        MatchingChatRoom chatRoom = matchingChatRoomService.findByRoomId(chatRoomId);
        User user = userService.getUserByUserId(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        List<ChatMessageResponseDto> messages = matchingChatService.getMessages(chatRoom, user, pageable);
        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(messages);
    }
}
