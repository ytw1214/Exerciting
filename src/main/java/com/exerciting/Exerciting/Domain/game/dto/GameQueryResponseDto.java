package com.exerciting.Exerciting.Domain.game.dto;

import com.exerciting.Exerciting.Domain.team.entity.Team;

import java.time.LocalDateTime;

public record GameQueryResponseDto (
    Long gameId,
    LocalDateTime gameStartTime,
    String homeTeamName,
    String awayTeamName,
    String sportType,
    String gameStatus,
    String stadiumName
    ) {
}
