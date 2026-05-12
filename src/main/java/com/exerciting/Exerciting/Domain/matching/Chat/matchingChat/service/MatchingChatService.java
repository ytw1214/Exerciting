package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.service;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageResponseDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository.MatchingChatRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<ChatMessageResponseDto> getMessages(MatchingChatRoom chatRoom,User user) {
        readMessages(chatRoom, user);
        return matchingChatRepository.findByMatchingChatRoomOrderBySendAtAsc(chatRoom)
                .stream()
                .map(chat -> new ChatMessageResponseDto(
                        chat.getSender().getId(),
                        chat.getMessage(),
                        chat.getSendAt(),
                        chat.getSender().getName(),
                        chat.isRead()
                ))
                .collect(Collectors.toList());
    }

    public MatchingChatRoom createChatRoom(Matching matching, User requester) {
        return matchingChatRoomRepository.save(
                MatchingChatRoom.builder()
                        .matching(matching)
                        .requester(requester)
                        .build()
        );
    }
    @Transactional
    public void readMessages(MatchingChatRoom chatRoom, User user) {
        matchingChatRepository
                .findByMatchingChatRoomAndIsReadFalseAndSenderNot(chatRoom, user)
                .forEach(chat -> chat.updateIsRead(true));
    }
}
