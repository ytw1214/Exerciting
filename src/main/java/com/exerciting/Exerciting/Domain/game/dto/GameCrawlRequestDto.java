package com.exerciting.Exerciting.Domain.game.dto;

import lombok.Builder;
import lombok.Getter;

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
