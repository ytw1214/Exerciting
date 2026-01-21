package com.exerciting.Exerciting.Repository;

import com.exerciting.Exerciting.Entity.Game;
import com.exerciting.Exerciting.Entity.Matching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
    //Optional<Matching> findById(Long Id);
    //Optional<Matching> findByMatchName(String matchName);
    //Optional<Matching> findByMatchTime(LocalDateTime matchTime);
    List<Matching> findByMeetTimeBefore(LocalDateTime meetTime);
    List<Matching> findByTitle(String title);
    List<Matching> findByDescriptionContaining(String Description);
    @Query("Select m from Matching m where m.maxPerson > m.currentPerson")
    List<Matching> findAvailableMatching();
}
