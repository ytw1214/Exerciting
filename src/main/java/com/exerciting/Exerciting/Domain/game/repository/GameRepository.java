package com.exerciting.Exerciting.Domain.game.repository;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long>, GameRepositoryCustom {

    List<Game> findByGameStatus(GameStatus gameStatus);
    @Query("Select g from Game g where g.homeTeam.name = :name OR g.awayTeam.name = :name")
    List<Game> findByTeamName(@Param("name")String name);
    //@Query("Select g from Game g where g.homeTeam.name LIKE %:name% OR g.awayTeam.name LIKE %:name%")
    //List<Game> findByTeamNameContaining(@Param("name") String name);
    List<Game> findByStadiumNameContaining(String name);
}
