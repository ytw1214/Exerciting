package com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.MatchingParticipant;
import com.exerciting.Exerciting.Domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingParticipantRepository extends JpaRepository<MatchingParticipant,Long> {
    List<MatchingParticipant> findByMatchingId(Long matchingId);
    boolean existsByMatchingAndUser(Matching matching, User user);
    long countByMatching(Matching matching);
}
