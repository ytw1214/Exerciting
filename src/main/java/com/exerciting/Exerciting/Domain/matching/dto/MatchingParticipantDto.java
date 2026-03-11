package com.exerciting.Exerciting.Domain.matching.dto;

import lombok.Getter;

@Getter

public class MatchingParticipantDto {
    private Long userId;
    private String nickname;
    private String role;
    private LocalDateTime joinedAt;

}
