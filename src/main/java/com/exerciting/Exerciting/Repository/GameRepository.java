package com.exerciting.Exerciting.Repository;

import com.exerciting.Exerciting.Entity.Game;
import com.exerciting.Exerciting.Entity.Matching;
import com.exerciting.Exerciting.Entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByHomeTeam(String homeTeam);
    List<Game> findByAwayTeam(String awayTeam);

    //List<Game> findByStadium(Stadium stadium);
    @Query("Select g from Game g where g.gameStartTime >= start AND g.gameStartTime < end")
    List<Game> findGameInPeriod(@Param("start")LocalDateTime start, @Param("end")LocalDateTime end);


    List<Game> findByMaxViewerLessThan(int maxViewer);
    List<Game> findByCurrentViewerLessThan(int currentViewer);
    @Query("Select g from Game where g.homeTeam LIKE %:name% OR g.awayTeam LIKE %:name%")
    List<Game> findByTeamName(@Param("team") String name);

    List<Game> findByStadiumContaining(String stadium);
}
