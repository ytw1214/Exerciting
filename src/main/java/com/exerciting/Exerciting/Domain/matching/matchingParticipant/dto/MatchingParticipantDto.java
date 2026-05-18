package com.exerciting.Exerciting.Domain.matching.matchingParticipant.dto;

import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.MatchingParticipant;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter

public class MatchingParticipantDto {
    private Long userId;
    private String nickname;
    private LocalDateTime joinedAt;

    public MatchingParticipantDto(Long userId, String nickname, LocalDateTime joinedAt) {
        this.userId = userId;
        this.nickname = nickname;
        this.joinedAt = joinedAt;
    }
    public static MatchingParticipantDto from(MatchingParticipant matchingParticipant) {
        return new MatchingParticipantDto(
                matchingParticipant.getUser().getId(),
                matchingParticipant.getUser().getNickname(),
                matchingParticipant.getCreatedAt()
        );
    }
}
