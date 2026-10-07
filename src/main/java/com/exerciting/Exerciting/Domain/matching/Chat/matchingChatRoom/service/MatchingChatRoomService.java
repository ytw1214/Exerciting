package com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.service;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.ParticipantStatus;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository.MatchingParticipantRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Exception.MatchingChatRoomNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchingChatRoomService {
    private final MatchingChatRoomRepository matchingChatRoomRepository;
    private final MatchingParticipantRepository matchingParticipantRepository;

    public MatchingChatRoom findByRoomId(Long chatRoomId) {
        return matchingChatRoomRepository.findById(chatRoomId)
                .orElseThrow(()-> new MatchingChatRoomNotFoundException());
    }

    /** 채팅방이 속한 매칭에 지금 참가 중(JOINED)인지. STOMP 구독 인가에 쓴다. */
    @Transactional(readOnly = true)
    public boolean isJoinedMember(Long chatRoomId, Long userId) {
        return matchingChatRoomRepository.findById(chatRoomId)
                .map(room -> matchingParticipantRepository.existsByMatching_IdAndUser_IdAndStatus(
                        room.getMatching().getId(), userId, ParticipantStatus.JOINED))
                .orElse(false);
    }

    @Transactional
    public MatchingChatRoom createChatRoom(Matching matching, User user) {
        return matchingChatRoomRepository.save(
                MatchingChatRoom.builder()
                        .matching(matching)
                        .requester(user)
                        .build()
        );
    }
}
