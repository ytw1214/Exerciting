package com.exerciting.Exerciting.Domain.matching.matching.repository;

import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingQueryResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long>, MatchingRepositoryCustom {
    //Optional<Matching> findById(Long Id);
    //Optional<Matching> findByMatchName(String matchName);
    //Optional<Matching> findByMatchTime(LocalDateTime matchTime);
    List<Matching> findByMeetTimeBefore(LocalDateTime meetTime);
    List<Matching> findByTitle(String title);
    List<Matching> findByDescriptionContaining(String Description);
    @Query("Select m from Matching m where m.maxPerson > m.currentPerson")
    List<Matching> findAvailableMatchingbyPerson();
    Optional<Matching> findById(Long id);
    List<MatchingQueryResponseDto> search(MatchingCustomCond cond);

}
