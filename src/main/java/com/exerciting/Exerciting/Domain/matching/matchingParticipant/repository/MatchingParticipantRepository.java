package com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository;

import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.MatchingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingParticipantRepository extends JpaRepository<MatchingParticipant,Long> {
    List<MatchingParticipant> findByMatchingId(Long matchingId);
}
