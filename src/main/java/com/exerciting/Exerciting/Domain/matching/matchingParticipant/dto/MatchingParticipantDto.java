package com.exerciting.Exerciting.Domain.matching.matchingParticipant.dto;

import lombok.Getter;

@Getter

public class MatchingParticipantDto {
    private Long userId;
    private String nickname;
    private String role;
    private LocalDateTime joinedAt;

}
