package com.exerciting.Exerciting.Domain.game.entity;

public enum GameRecord{
    HomeWin("홈팀 승리"),
    AwayWin("원정팀 승리"),
    DRAW("무승부")

    private final String status;

    GameRecord(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
