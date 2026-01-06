package com.exerciting.Exerciting.Repository;

import com.exerciting.Exerciting.Entity.Matching;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
    //Optional<Matching> findById(Long Id);
    Optional<Matching> findByMatchName(String matchName);
    Optional<Matching> findByMatchTime(LocalDateTime matchTime);
}
