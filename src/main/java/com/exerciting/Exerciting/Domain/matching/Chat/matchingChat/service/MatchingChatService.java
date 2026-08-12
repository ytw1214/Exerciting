package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.service;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.dto.ChatMessageResponseDto;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository.MatchingChatRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.MatchingParticipant;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository.MatchingParticipantRepository;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.service.MatchingParticipantService;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Exception.UnauthorizedUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingChatService {
    private final MatchingChatRepository matchingChatRepository;
    private final MatchingChatRoomRepository matchingChatRoomRepository;
    private final MatchingParticipantService matchingParticipantService;
    private final MatchingParticipantRepository matchingParticipantRepository;
    @Transactional
    public ChatMessageResponseDto saveMessage(MatchingChatRoom chatRoom, User sender, String message) {
        if(!matchingParticipantRepository.existsByMatchingAndUser(chatRoom.getMatching(), sender)) {
            throw new UnauthorizedUserException();
        }
        MatchingChat chat = matchingChatRepository.save(MatchingChat.builder()
                .matchingChatRoom(chatRoom)
                .sender(sender)
                .message(message)
                .build());
        return ChatMessageResponseDto.from(chat);
    }

    public List<ChatMessageResponseDto> getMessages(MatchingChatRoom chatRoom, User user, Pageable pageable) {
        readMessages(chatRoom, user);
        Slice<MatchingChat> slice = matchingChatRepository.findByMatchingChatRoomOrderBySendAtDesc(chatRoom, pageable);
        List<ChatMessageResponseDto> messages = slice.getContent().stream()
                .map(chat -> new ChatMessageResponseDto(
                        chat.getSender().getId(),
                        chat.getMessage(),
                        chat.getSendAt(),
                        chat.getSender().getName()
                ))
                .collect(Collectors.toList());
        Collections.reverse(messages);
        return messages;
    }

    @Transactional
    public void readMessages(MatchingChatRoom chatRoom, User user) {
        matchingParticipantService.updateLastReadAt(chatRoom.getMatching(), user);
    }

}
