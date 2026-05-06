package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchingChatRepository extends JpaRepository<MatchingChat, Long> {
    @Query("select c from MatchingChat c join fetch c.sender where c.matchingChatRoom=:chatroom order by c.sendAt asc")
    List<MatchingChat> findByMatchingChatRoomOrderBySendAtAsc(MatchingChatRoom matchingChatRoom);

    List<MatchingChat> findByMatchingChatRoomAndIsReadFalseAndSenderNot(MatchingChatRoom chatRoom, User sender);
}
