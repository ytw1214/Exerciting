package com.exerciting.Exerciting.dto.Game;

import com.exerciting.Exerciting.Entity.Game;
import com.exerciting.Exerciting.Entity.SportType;
import com.exerciting.Exerciting.Entity.Stadium;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameSearchRequestDto {
    private Long gameId;
    private LocalDateTime gameStartTime;
    private SportType sportType;
    private String team;
    private Stadium stadium;

    public Game toEntity() {
        return Game.builder()
                .gameStartTime(this.gameStartTime)
                .sportType(this.sportType)
                .homeTeam(this.team)
                .stadium(this.stadium)
                .build();
    }

}
