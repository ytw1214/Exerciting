package com.exerciting.Exerciting.dto.Game;

import com.exerciting.Exerciting.Entity.Game;
import com.exerciting.Exerciting.Entity.SportType;
import com.exerciting.Exerciting.Entity.Stadium;
import com.exerciting.Exerciting.Entity.Team;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameCrawlRequestDto {
    private String stadiumName;
    private String sportType;
    private String homeTeam;
    private String awayTeam;
    private String gameStartTime;

    public GameCrawlRequestDto(String stadiumName, String sportType, String homeTeam, String awayTeam, String gameStartTime) {
        this.stadiumName = stadiumName;
        this.sportType = sportType;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.gameStartTime = gameStartTime;
    }

}
