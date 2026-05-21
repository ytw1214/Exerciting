package com.exerciting.Exerciting.Domain.matching.matching.dto;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.dto.MatchingParticipantDto;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class MatchingDetailResponseDto {
    private Long matchingId;
    private String title;
    private String description;
    private int maxPerson;
    private boolean isJoined;
    private String status;
    private LocalDateTime meetTime;
    private String homeTeam;
    private String awayTeam;
    private List<MatchingParticipantDto> participants;

    private MatchingDetailResponseDto() {}

    public static MatchingDetailResponseDto of(
            Matching matching,
            boolean isJoined,
            List<MatchingParticipantDto> participants) {

        MatchingDetailResponseDto dto = new MatchingDetailResponseDto();
        dto.matchingId   = matching.getId();
        dto.title        = matching.getTitle();
        dto.description  = matching.getDescription();
        dto.maxPerson    = matching.getMaxPerson();
        dto.isJoined     = isJoined;
        dto.status       = matching.getStatus().name();
        dto.meetTime     = matching.getMeetTime();
        dto.homeTeam     = matching.getGame().getHomeTeam().getShortName();
        dto.awayTeam     = matching.getGame().getAwayTeam().getShortName();
        dto.participants = participants;
        return dto;
    }
}
