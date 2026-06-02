package com.exerciting.Exerciting.Domain.matching.matching.dto;


import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;

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
    public static MatchingQueryResponseDto from(Matching matching) {
        return new MatchingQueryResponseDto(
                matching.getId(),
                matching.getTitle(),
                matching.getMaxPerson(),
                matching.getMeetTime(),
                matching.getGame().getHomeTeam().getShortName(),
                matching.getGame().getAwayTeam().getShortName(),
                matching.getGame().getStadium().getName(),
                matching.getGame().getSportType().name()

                );

    }
}