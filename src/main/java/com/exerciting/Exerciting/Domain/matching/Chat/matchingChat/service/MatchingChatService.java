package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.service;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository.MatchingChatRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingChatService {
    private final MatchingChatRepository matchingChatRepository;
    private final MatchingChatRoomRepository matchingChatRoomRepository;

    public MatchingChat saveMessage(MatchingChatRoom chatRoom, User senderId, String message) {
        MatchingChat chat = MatchingChat.builder()
                .matchingChatRoom(chatRoom)
                .sender(senderId)
                .message(message)
                .build();
        return matchingChatRepository.save(chat);
    }

    public List<MatchingChat> getMessages(MatchingChatRoom chatRoom) {
        return matchingChatRepository.findByMatchingChatRoomOrderBySendAtAsc(chatRoom);
    }

    public MatchingChatRoom createChatRoom(Matching matching, User requester) {
        return matchingChatRoomRepository.save(
                MatchingChatRoom.builder()
                        .matching(matching)
                        .requester(requester)
                        .build()
        );
    }
}
