package com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.service;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Exception.MatchingChatRoomNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchingChatRoomService {
    private final MatchingChatRoomRepository matchingChatRoomRepository;

    public MatchingChatRoom findByRoomId(Long chatRoomId) {
        return matchingChatRoomRepository.findById(chatRoomId)
                .orElseThrow(()-> new MatchingChatRoomNotFoundException());
    }

}
