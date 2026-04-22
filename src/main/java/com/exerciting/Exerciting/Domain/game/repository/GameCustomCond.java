package com.exerciting.Exerciting.Domain.game.repository;

import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.global.SportType;

import java.time.LocalDate;
<<<<<<< HEAD

public record GameCustomCond(
        GameStatus gameStatus,
        SportType sportType,
        LocalDate startTime) {
    public static GameCustomCond of(GameStatus status, SportType type, LocalDate startTime) {
        return new GameCustomCond(status, type, startTime);
=======

public record GameCustomCond(GameStatus gameStatus, SportType sportType, LocalDate dateTime) {

    public GameCustomCond {

    }

    public static GameCustomCond of(GameStatus status, SportType type, LocalDate dateTime) {
        return new GameCustomCond(status, type, dateTime);
>>>>>>> 873bd9aa5415303c4880acba78e3c772f19c65bd
    }

}
