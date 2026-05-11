package com.exerciting.Exerciting.Domain.matching.matching.dto;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MatchingResponseDto {
    private Long id;
    private String title;
    private String description;
    private int maxPerson;
    private LocalDateTime meetTime;
    private String homeTeam;
    private String awayTeam;

    public MatchingResponseDto(Long id, String title, String description, int maxPerson, LocalDateTime meetTime, String homeTeam, String awayTeam) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.maxPerson = maxPerson;
        this.meetTime = meetTime;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public static MatchingResponseDto fromEntity(Matching matching) {
        return MatchingResponseDto.builder()
                .id(matching.getId())
                .title(matching.getTitle())
                .description(matching.getDescription())
                .maxPerson(matching.getMaxPerson())
                .meetTime(matching.getMeetTime())
                .homeTeam(matching.getGame().getHomeTeam().getShortName())
                .awayTeam(matching.getGame().getAwayTeam().getShortName())
                .build();
    }
}
