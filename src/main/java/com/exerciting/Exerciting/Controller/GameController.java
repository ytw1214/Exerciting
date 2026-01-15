package com.exerciting.Exerciting.Controller;

import com.exerciting.Exerciting.Repository.GameRepository;
import com.exerciting.Exerciting.Service.GameService;
import com.exerciting.Exerciting.dto.Game.GameSearchRequestDto;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class GameController {
    private final GameService gameService;
    private final GameRepository gameRepository;

    public GameController(GameService gameService, GameRepository gameRepository) {
        this.gameService = gameService;
        this.gameRepository = gameRepository;
    }


    public ResponseEntity<GameSearchRequestDto> getGames(
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end) {
        if(gameService.getLIVEGames().isEmpty()) {
            return new ResponseEntity<>(HttpStatusCode.)
        }
    }

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
