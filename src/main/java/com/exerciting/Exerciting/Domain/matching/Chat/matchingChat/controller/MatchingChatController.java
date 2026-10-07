package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.controller;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageRequestDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageResponseDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.service.MatchingChatService;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.service.MatchingChatRoomService;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Infrastructure.security.LoginUser;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import com.exerciting.Exerciting.Exception.BusinessException;
import com.exerciting.Exerciting.Infrastructure.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
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
    private static final int MAX_PAGE_SIZE = 100;

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
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        MatchingChatRoom chatRoom = matchingChatRoomService.findByRoomId(chatRoomId);
        User user = userService.getById(loginUser.getId());
        // size 상한이 없으면 size=1000000 한 번으로 방 전체를 메모리에 올릴 수 있다
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        List<ChatMessageResponseDto> messages = matchingChatService.getMessages(chatRoom, user, pageable);
        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(messages);
    }

    /**
     * 메시지 전송 실패(권한 없음, 길이 초과 등)를 보낸 사람에게만 알려준다.
     * 예전에는 예외가 로그에만 남고 발신자는 메시지가 사라진 이유를 알 수 없었다.
     * 클라이언트는 /user/queue/errors 를 구독해 받는다.
     */
    @MessageExceptionHandler(BusinessException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorResponse handleChatError(BusinessException e) {
        return ErrorResponse.of(e.getErrorCode());
    }
}
