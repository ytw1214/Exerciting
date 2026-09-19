package com.exerciting.Exerciting.Domain.matching.matching.dto;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.entity.MatchingStatus;

public record MatchingCreateResponseDto(
        Long matchingId,
        Long chatRoomId,
        MatchingStatus status,
        String statusLabel,
        int currentPerson,
        int maxPerson
) {
    public static MatchingCreateResponseDto of(Matching matching, Long chatRoomId, long currentPerson) {
        return new MatchingCreateResponseDto(
                matching.getId(),
                chatRoomId,
                matching.getStatus(),
                matching.getStatus().getMatchingStatus(),
                (int) currentPerson,
                matching.getMaxPerson()
        );
    }
}
