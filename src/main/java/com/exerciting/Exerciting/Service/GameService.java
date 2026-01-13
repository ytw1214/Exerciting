package com.exerciting.Exerciting.Service;

import com.exerciting.Exerciting.Entity.Game;
import com.exerciting.Exerciting.Repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameService {
    @Autowired
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }
    public List<Game> getPastGames(LocalDateTime localDateTime) {
        LocalDateTime current = LocalDateTime.now();
        return gameRepository.findGameInPeriod(localDateTime, current);
    }
    public List<Game> getCurrentGames() {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime current = LocalDateTime.now();
        return gameRepository.findGameInPeriod(today, current);
    }
    public List<Game> getFutureGames(LocalDateTime localDateTime) {
        LocalDateTime current = LocalDateTime.now();
        return gameRepository.findGameInPeriod(current, localDateTime);
    }

    public List<Game> getGameByTeamName(String name) {
        return gameRepository.findByTeamName(name);
    }
    public List<Game> findByStadium(String stadium) {
        return gameRepository.findByStadiumContaining(stadium);
    }
}
