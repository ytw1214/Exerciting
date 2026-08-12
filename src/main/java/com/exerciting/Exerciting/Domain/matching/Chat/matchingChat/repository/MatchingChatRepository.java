package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MatchingChatRepository extends JpaRepository<MatchingChat, Long> {
    @Query("select c from MatchingChat c join fetch c.sender where c.matchingChatRoom=:matchingChatRoom order by c.sendAt desc")
    Slice<MatchingChat> findByMatchingChatRoomOrderBySendAtDesc(@Param("matchingChatRoom") MatchingChatRoom matchingChatRoom, Pageable pageable);

    long countByMatchingChatRoomAndSendAtAfter(MatchingChatRoom matchingChatRoom, LocalDateTime time);
}

