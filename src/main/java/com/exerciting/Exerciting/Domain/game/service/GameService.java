package com.exerciting.Exerciting.Domain.game.service;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import org.springframework.stereotype.Service;

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

}
