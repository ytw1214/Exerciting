package com.exerciting.Exerciting.Infrastructure.security;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.service.MatchingChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * STOMP 프레임 단위 인증·인가.
 *
 * - CONNECT  : access 토큰을 검증하고 LoginUser를 세션 주체로 등록한다.
 * - SUBSCRIBE: 채팅 토픽은 그 매칭에 참가 중(JOINED)인 사용자만 구독할 수 있다.
 *              예전에는 CONNECT만 검사해 로그인한 누구나 /sub/matching/chat/{id}를 구독해
 *              남의 채팅방 대화를 실시간으로 받을 수 있었다.
 *
 * 인터셉터에서 예외가 나면 Spring이 STOMP ERROR 프레임을 보내고 연결을 끊는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    static final String CHAT_TOPIC_PREFIX = "/sub/matching/chat/";
    static final String ERROR_QUEUE = "/user/queue/errors";

    private final JwtTokenProvider jwtTokenProvider;
    private final MatchingChatRoomService matchingChatRoomService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeSubscribe(accessor);
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : header;
        if (token == null || !jwtTokenProvider.validateAccessToken(token)) {
            throw new MessagingException("인증에 실패했습니다.");
        }
        LoginUser loginUser = jwtTokenProvider.getLoginUser(token);
        if (loginUser == null) {
            throw new MessagingException("다시 로그인해주세요.");
        }
        // 여기서 등록한 주체는 같은 세션의 이후 프레임(SUBSCRIBE, SEND)에도 그대로 붙는다
        accessor.setUser(new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
    }

    private void authorizeSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (ERROR_QUEUE.equals(destination)) {
            return; // 자기 자신에게 오는 오류 메시지 큐
        }
        if (destination == null || !destination.startsWith(CHAT_TOPIC_PREFIX)) {
            throw new MessagingException("구독할 수 없는 경로입니다.");
        }
        LoginUser loginUser = loginUserOf(accessor.getUser());
        Long chatRoomId = parseRoomId(destination.substring(CHAT_TOPIC_PREFIX.length()));

        if (!matchingChatRoomService.isJoinedMember(chatRoomId, loginUser.getId())) {
            log.warn("채팅방 구독 거부 - chatRoomId: {}, userId: {}", chatRoomId, loginUser.getId());
            throw new MessagingException("채팅방 참가자만 구독할 수 있습니다.");
        }
    }

    private LoginUser loginUserOf(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken authentication
                && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new MessagingException("인증되지 않은 세션입니다.");
    }

    private Long parseRoomId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new MessagingException("잘못된 채팅방 경로입니다.");
        }
    }
}
