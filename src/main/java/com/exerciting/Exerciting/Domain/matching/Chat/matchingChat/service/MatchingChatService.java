package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.service;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository.MatchingChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchingChatService {
    private final MatchingChatRepository matchingChatRepository;

}
