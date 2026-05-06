package com.exerciting.Exerciting.Domain.game.service;

import com.exerciting.Exerciting.Domain.game.dto.GameQueryResponseDto;
import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.game.repository.GameCustomCond;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;

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
    };

    public List<Game> getGameByStadium(String name) {
        return gameRepository.findByStadiumNameContaining(name);
    }
    // 월별 경기 조회
    public List<GameQueryResponseDto> getMonthGames(GameCustomCond cond) {
        LocalDate target = cond.startTime() != null ? cond.startTime() : LocalDate.now();
        LocalDateTime start = target.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = target.withDayOfMonth(target.lengthOfMonth()).atTime(LocalTime.MAX);
        System.out.println("-----------월-----------");
        System.out.println(start);
        System.out.println(end);
        return gameRepository.search(cond, start, end);
    }
    //일별 경기 조회
    public List<GameQueryResponseDto> getDailyGames(GameCustomCond cond) {
        LocalDateTime start = cond.startTime().atStartOfDay();
        LocalDateTime end = cond.startTime().atTime(LocalTime.MAX);
        System.out.println("-----------일-----------");
        System.out.println(start);
        System.out.println(end);
        return gameRepository.search(cond,start,end);
    }
}
