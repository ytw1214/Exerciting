package com.exerciting.Exerciting.Domain.team.dto;

import com.exerciting.Exerciting.Domain.global.SportType;

import java.math.BigDecimal;

public record TeamRankResponseDto(
        SportType sportType,
        String teamName,
        int rank,
        int games,
        int wins,
        int losses,
        int draws,
        BigDecimal winRate,
        BigDecimal gamesBehind
) {

}
