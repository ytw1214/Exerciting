package com.exerciting.Exerciting.Domain.matching.dto;

import com.exerciting.Exerciting.Domain.matching.entity.Matching;
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
    private int currentPerson;
    private LocalDateTime meetTime;


    public MatchingResponseDto(Long id, String title, String description, int maxPerson, int currentPerson, LocalDateTime meetTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.maxPerson = maxPerson;
        this.currentPerson = currentPerson;
        this.meetTime = meetTime;
    }

    public static MatchingResponseDto fromEntity(Matching matching) {
        return MatchingResponseDto.builder()
                .id(matching.getId())
                .title(matching.getTitle())
                .description(matching.getDescription())
                .maxPerson(matching.getMaxPerson())
                .currentPerson(matching.getCurrentPerson())
                .meetTime(matching.getMeetTime())
                .build();
    }
}
