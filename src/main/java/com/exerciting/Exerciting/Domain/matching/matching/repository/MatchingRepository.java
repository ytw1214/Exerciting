package com.exerciting.Exerciting.Domain.matching.matching.repository;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long>, MatchingRepositoryCustom {
    List<Matching> findByMeetTimeBefore(LocalDateTime meetTime);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Matching m WHERE m.id = :id")
    Optional<Matching> findByIdWithLock(@Param("id") Long id);
    //List<MatchingQueryResponseDto> search(MatchingCustomCond cond);

}
