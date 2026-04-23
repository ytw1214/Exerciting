package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatoom.entity.MatchingChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchingChatRepository extends JpaRepository<MatchingChat, Long> {
    List<MatchingChat> findByMatchingChatRoomOrderBySendAtAsc(MatchingChatRoom matchingChatRoom);
}
