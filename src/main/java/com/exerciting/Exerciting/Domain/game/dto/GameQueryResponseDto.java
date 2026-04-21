package com.exerciting.Exerciting.Domain.game.dto;

import com.exerciting.Exerciting.Domain.team.entity.Team;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record GameQueryResponseDto (
    Long gameId,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    LocalDateTime gameStartTime,
    String homeTeamName,
    String awayTeamName,
    String sportType,
    String gameStatus,
    String stadiumName
    ) {
}
