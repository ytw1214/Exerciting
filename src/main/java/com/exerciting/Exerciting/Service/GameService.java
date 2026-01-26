package com.exerciting.Exerciting.Service;

import com.exerciting.Exerciting.Entity.Game;
import com.exerciting.Exerciting.Entity.GameStatus;
import com.exerciting.Exerciting.Entity.Team;
import com.exerciting.Exerciting.Repository.GameRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameService {
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getPastGames() {
        return gameRepository.findByGameStatus(GameStatus.FINISHED);
    }
    public List<Game> getCurrentGames() {
        return gameRepository.findByGameStatus(GameStatus.PROCEEDING);
    }
    public List<Game> getFutureGames() {
        return gameRepository.findByGameStatus(GameStatus.BEFORE);
    }

    public List<Game> getGameByTeamName(String name) {
        return gameRepository.findByTeamName(name);
    }
    public List<Game> getGameByTeamNameContaining(String name) {
        return gameRepository.findByTeamNameContaining(name);
    }
    public List<Game> getGameByStadium(String name) {
        return gameRepository.findByStadiumNameContaining(name);
    }

    /*
    @Transactional
    public Long savePlayer(Long teamId, String name, String position, int age) {

    }

     */
}
