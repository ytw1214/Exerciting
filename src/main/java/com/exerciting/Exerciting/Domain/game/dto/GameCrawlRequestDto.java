package com.exerciting.Exerciting.Domain.game.dto;

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
    private LocalDateTime gameStartTime;

    public GameCrawlRequestDto(String stadiumName, String sportType, String homeTeam, String awayTeam, LocalDateTime gameStartTime) {
        this.stadiumName = stadiumName;
        this.sportType = sportType;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.gameStartTime = gameStartTime;
    }

}
