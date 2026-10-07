package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.service;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageResponseDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository.MatchingChatRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.matching.entity.MatchingStatus;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.MatchingParticipant;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.ParticipantStatus;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository.MatchingParticipantRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Exception.UnauthorizedUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MatchingChatService {
    // 지난 대화를 볼 수 있는 상태: 참가 중이거나, 경기가 끝나 출석 처리된 사람
    private static final Set<ParticipantStatus> CAN_READ = Set.of(ParticipantStatus.JOINED, ParticipantStatus.ATTENDED);

    private final MatchingChatRepository matchingChatRepository;
    private final MatchingParticipantRepository matchingParticipantRepository;

    @Transactional
    public ChatMessageResponseDto saveMessage(MatchingChatRoom chatRoom, User sender, String message) {
        if (message == null || message.isBlank() || message.length() > MatchingChat.MAX_MESSAGE_LENGTH) {
            throw new InvalidInputException();
        }
        // 끝나지 않은 매칭의 참가자만 보낼 수 있다(취소된 매칭에서 대화가 이어지던 문제).
        // chatRoom.getMatching()은 지연 로딩 프록시라 id만 꺼내 쓴다(WebSocket 스레드에는 OSIV가 없다).
        if (!matchingParticipantRepository.existsByMatching_IdAndUser_IdAndStatusAndMatching_StatusIn(
                chatRoom.getMatching().getId(), sender.getId(), ParticipantStatus.JOINED, MatchingStatus.ACTIVE_STATUSES)) {
            throw new UnauthorizedUserException();
        }
        MatchingChat chat = matchingChatRepository.save(MatchingChat.builder()
                .matchingChatRoom(chatRoom)
                .sender(sender)
                .message(message)
                .build());
        return ChatMessageResponseDto.from(chat);
    }

    /**
     * 참가 중이거나 출석 처리된 사람만 지난 대화를 볼 수 있다.
     * 예전에는 lastReadAt 갱신의 부수 효과로 권한을 검사해, 나간(LEFT) 사람도 대화를 계속 읽을 수 있었고
     * 참가한 적 없는 사람은 403이 아니라 400을 받았다.
     */
    @Transactional
    public List<ChatMessageResponseDto> getMessages(MatchingChatRoom chatRoom, User user, Pageable pageable) {
        MatchingParticipant participant = matchingParticipantRepository
                .findByMatchingAndUser(chatRoom.getMatching(), user)
                .filter(p -> CAN_READ.contains(p.getStatus()))
                .orElseThrow(UnauthorizedUserException::new);
        participant.updateLastReadAt(LocalDateTime.now());

        Slice<MatchingChat> slice = matchingChatRepository.findByMatchingChatRoomOrderBySendAtDesc(chatRoom, pageable);
        List<ChatMessageResponseDto> messages = new ArrayList<>(
                slice.getContent().stream().map(ChatMessageResponseDto::from).toList());
        Collections.reverse(messages); // 화면에는 오래된 것부터
        return messages;
    }
}
