package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchingChatRepository extends JpaRepository<MatchingChat, Long> {
    @Query("select c from MatchingChat c join fetch c.sender where c.matchingChatRoom=:matchingChatRoom order by c.sendAt asc")
    List<MatchingChat> findByMatchingChatRoomOrderBySendAtAsc(@Param("matchingChatRoom")MatchingChatRoom matchingChatRoom);
    long countByMatchingChatRoomAndSendAtAfter(MatchingChatRoom matchingChatRoom, LocalDateTime time);
}
