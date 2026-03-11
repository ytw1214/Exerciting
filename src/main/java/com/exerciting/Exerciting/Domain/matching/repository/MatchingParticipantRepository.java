package com.exerciting.Exerciting.Domain.matching.repository;

import com.exerciting.Exerciting.Domain.matching.entity.MatchingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingParticipantRepository extends JpaRepository<Long, MatchingParticipant> {
    List<MatchingParticipant> findByMatchingId(Long matchingId);
}
