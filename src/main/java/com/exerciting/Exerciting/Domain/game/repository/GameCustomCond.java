package com.exerciting.Exerciting.Domain.game.repository;

import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.global.SportType;

import java.time.LocalDate;

public record GameCustomCond(GameStatus gameStatus, SportType sportType, LocalDate dateTime) {

    public GameCustomCond {

    }

    public static GameCustomCond of(GameStatus status, SportType type, LocalDate dateTime) {
        return new GameCustomCond(status, type, dateTime);
    }

}
