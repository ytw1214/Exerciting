package com.exerciting.Exerciting.Repository;

import com.exerciting.Exerciting.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    //List<Game> findByStadium(Stadium stadium);
    /*
    @Query("Select g from Game g where g.gameStartTime >= start AND g.gameStartTime < end")
    List<Game> findGameInPeriod(@Param("start")LocalDateTime start, @Param("end")LocalDateTime end);


     */

    List<Game> findByGameStatus(GameStatus gameStatus);
@Query("Select g from Game g where g.homeTeam.name = :name OR g.awayTeam.name = :name")
    List<Game> findByTeamName(@Param("name")String name);
    @Query("Select g from Game g where g.homeTeam.name LIKE %:name% OR g.awayTeam.name LIKE %:name%")
    List<Game> findByTeamNameContaining(@Param("team") String name);

    List<Game> findByStadiumNameContaining(String name);
}
