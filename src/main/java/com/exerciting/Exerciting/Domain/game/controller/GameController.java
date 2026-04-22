package com.exerciting.Exerciting.Domain.game.controller;

import com.exerciting.Exerciting.Domain.game.dto.GameQueryResponseDto;
import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.game.repository.GameCustomCond;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.game.service.GameService;
import com.exerciting.Exerciting.Domain.global.SportType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
public class GameController {
    private final GameService gameService;
    private final GameRepository gameRepository;

    public GameController(GameService gameService, GameRepository gameRepository) {
        this.gameService = gameService;
        this.gameRepository = gameRepository;
    }
    /*
    public ResponseEntity<GameSearchRequestDto> getGames(
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end) {
        if(gameService.getLIVEGames().isEmpty()) {
            return new ResponseEntity<>(HttpStatusCode.)
        }
    }

     */
    @GetMapping("/games/month")
    public ResponseEntity<List<GameQueryResponseDto>> getMonthGames(
        @RequestParam(required = false) LocalDate startTime,
        @RequestParam(required = false) SportType sportType,
        @RequestParam(required = false) String teamName,
        @RequestParam(defaultValue = "false") boolean containsAwayTeam) {
        GameCustomCond cond = new GameCustomCond(null,sportType,startTime,teamName,containsAwayTeam);
        List<GameQueryResponseDto> result = gameService.getMonthGames(cond);
        if(result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
    @GetMapping("/api/v1/games")
    public ResponseEntity<List<GameQueryResponseDto>> getGamesByDate(
            @RequestParam(required = false) GameStatus gameStatus,
            @RequestParam(required = false) SportType sportType,
            @RequestParam(required = false) LocalDate startTime,
            @RequestParam(required = false) String teamName,
            @RequestParam(defaultValue = "false") boolean containsAwayTeam) {

        GameCustomCond cond = GameCustomCond.of(gameStatus, sportType, startTime, teamName,containsAwayTeam);
        List<GameQueryResponseDto> result = gameService.getDailyGames(cond);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
    /*
    @GetMapping("/api/v1/games")
    public ResponseEntity<List<GameQueryResponseDto>> getGames(
            @RequestParam(required = false) GameStatus gameStatus,
            @RequestParam(required = false) SportType sportType,
            @RequestParam(required = false) LocalDate dateTime) {

        GameCustomCond cond = GameCustomCond.of(gameStatus, sportType,dateTime);
        List<GameQueryResponseDto> result = gameService.getGameDetail(cond);

        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

     */
    /*
    public ResponseEntity<GameRequestDto> getGamesByTeam(String teamName) {
        if(teamName == null) {
            return new ResponseEntity.status(400)
        }
    }


     */
    /*
    public ResponseEntity<GameRequestDto> createGame(GameRequestDto dto) {
        if(dto.getGameStartTime().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("시간 오류");
        }
        Game game = dto.toEntity();
        Game savedGame = gameRepository.save(game);

    }

     */
}
