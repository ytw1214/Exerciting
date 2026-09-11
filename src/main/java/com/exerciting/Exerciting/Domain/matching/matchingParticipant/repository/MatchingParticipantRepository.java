package com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.MatchingParticipant;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.ParticipantStatus;
import com.exerciting.Exerciting.Domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchingParticipantRepository extends JpaRepository<MatchingParticipant,Long> {
    List<MatchingParticipant> findByMatchingId(Long matchingId);
    boolean existsByMatchingAndUserAndStatus(Matching matching, User user, ParticipantStatus status);

    long countByMatchingAndStatus(Matching matching, ParticipantStatus status);
    Optional<MatchingParticipant> findByMatchingAndUserAndStatus(Matching matching, User user,ParticipantStatus status);

    List<MatchingParticipant> findByMatchingAndStatus(Matching matching, ParticipantStatus status);
}
