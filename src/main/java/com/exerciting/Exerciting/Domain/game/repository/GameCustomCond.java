package com.exerciting.Exerciting.Domain.game.repository;

import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.global.SportType;

import java.time.LocalDate;

public record GameCustomCond(
        GameStatus gameStatus,
        SportType sportType,
        LocalDate startTime,
        String teamName,
        boolean containsAwayTeam) {
    public static GameCustomCond of(GameStatus status, SportType type, LocalDate startTime, String teamName, boolean containsAwayTeam) {
        return new GameCustomCond(status, type, startTime, teamName, containsAwayTeam);
    }

}
