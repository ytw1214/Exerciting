package com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.service;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Exception.MatchingChatRoomNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchingChatRoomService {
    private final MatchingChatRoomRepository matchingChatRoomRepository;

    public MatchingChatRoom findByRoomId(Long chatRoomId) {
        return matchingChatRoomRepository.findById(chatRoomId)
                .orElseThrow(()-> new MatchingChatRoomNotFoundException());
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
