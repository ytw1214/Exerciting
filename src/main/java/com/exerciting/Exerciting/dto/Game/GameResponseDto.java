package com.exerciting.Exerciting.dto.Game;

import com.exerciting.Exerciting.Entity.Game;
import com.exerciting.Exerciting.Entity.Stadium;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@Builder
public class GameResponseDto {
    private Long gameId;
    private LocalDateTime gameStartTime;
    private Stadium stadium;
    private String homeTeam;
    private String awayTeam;

    public GameResponseDto(Game game) {
        this.gameId = game.getId();
        this.gameStartTime = game.getGameStartTime();
        this.stadium = game.getStadium();
        this.homeTeam = game.getHomeTeam();
        this.awayTeam = game.getAwayTeam();
    }
}
