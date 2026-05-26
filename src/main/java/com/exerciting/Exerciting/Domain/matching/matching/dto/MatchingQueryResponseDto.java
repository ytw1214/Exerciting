package com.exerciting.Exerciting.Domain.matching.matching.dto;

import java.time.LocalDateTime;

public record MatchingQueryResponseDto(
        Long matchingId,
        String title,
        int maxPerson,
        LocalDateTime meetTime,
        String homeTeamEnglish,
        String awayTeamEnglish,
        String stadiumName,
        String sportType
) {
}