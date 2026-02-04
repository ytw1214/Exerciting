package com.exerciting.Exerciting.Domain.game.dto;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.stadium.entity.Stadium;
import com.exerciting.Exerciting.Domain.team.entity.Team;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameSearchRequestDto {
    private Long gameId;
    private LocalDateTime gameStartTime;
    private SportType sportType;
    private Team homeTeam;
    private Team awayTeam;
    private Stadium stadium;

    public Game toEntity() {
        return Game.builder()
                .gameStartTime(this.gameStartTime)
                .sportType(this.sportType)
                .homeTeam(this.homeTeam)
                .awayTeam(this.awayTeam)
                .stadium(this.stadium)
                .build();
    }

}
