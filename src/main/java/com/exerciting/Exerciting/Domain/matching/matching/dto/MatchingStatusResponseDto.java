package com.exerciting.Exerciting.Domain.matching.matching.dto;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.entity.MatchingStatus;

public record MatchingStatusResponseDto(
        Long matchingId,
        MatchingStatus status,
        String statusLabel,
        int currentPerson,
        int maxPerson
) {
    public static MatchingStatusResponseDto of(Matching matching, long currentPerson) {
        return new MatchingStatusResponseDto(
                matching.getId(),
                matching.getStatus(),
                matching.getStatus().getMatchingStatus(),
                (int) currentPerson,
                matching.getMaxPerson()
        );
    }
}
