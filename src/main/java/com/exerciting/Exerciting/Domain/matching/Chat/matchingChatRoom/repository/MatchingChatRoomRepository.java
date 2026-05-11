package com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface MatchingChatRoomRepository extends JpaRepository<MatchingChatRoom, Long> {
    Optional<MatchingChatRoom> findByMatching(Matching matching);
}
