package com.exerciting.Exerciting.Domain.game.repository;

import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.global.SportType;
import org.springframework.util.StringUtils;

public record GameCustomCond(GameStatus gameStatus, SportType sportType) {

    public GameCustomCond {

    }

    public static GameCustomCond of(GameStatus status, SportType type) {
        return new GameCustomCond(status, type);
    }

}
