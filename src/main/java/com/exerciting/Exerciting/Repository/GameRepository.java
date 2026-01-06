package com.exerciting.Exerciting.Repository;

import com.exerciting.Exerciting.Entity.Game;
import com.exerciting.Exerciting.Entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByHomeTeam(String homeTeam);
    List<Game> findByAwayTeam(String awayTeam);

    List<Game> findByStadium(Stadium stadium);

    List<Game> findByGameStartBefore(LocalDateTime localDateTime);
    List<Game> findByGameStartBetween(LocalDateTime start, LocalDateTime end);
    List<Game> findByGameStartAfter(LocalDateTime localDateTime);

    List<Game> findByMaxViewerSmaller(int currentviewer);
}
