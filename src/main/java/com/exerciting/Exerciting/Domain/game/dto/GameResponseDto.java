package com.exerciting.Exerciting.Domain.game.dto;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.stadium.entity.Stadium;
import com.exerciting.Exerciting.Domain.team.entity.Team;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
public class GameResponseDto {
    private LocalDateTime gameStartTime;
    private Stadium stadium;
    private Team homeTeam;
    private Team awayTeam;

    @Builder
    public GameResponseDto(Game game) {
        this.gameStartTime = game.getGameStartTime();
        this.stadium = game.getStadium();
        this.homeTeam = game.getHomeTeam();
        this.awayTeam = game.getAwayTeam();
    }
}
