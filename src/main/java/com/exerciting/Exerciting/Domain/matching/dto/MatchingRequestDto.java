package com.exerciting.Exerciting.Domain.matching.dto;

import com.exerciting.Exerciting.Domain.matching.entity.Matching;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@Builder
public class MatchingRequestDto {
    private Long id;
    private String title;
    private String description;
    private int maxPerson;
    private LocalDateTime meetTime;

    public MatchingRequestDto(String title, String description, int maxPerson, LocalDateTime meetTime) {
        this.title = title;
        this.description = description;
        this.maxPerson = maxPerson;
        this.meetTime = meetTime;
    }
    public static MatchingRequestDto fromEntity(Matching matching) {
        return MatchingRequestDto.builder()
                .title(matching.getTitle())
                .description(matching.getDescription())
                .maxPerson(matching.getMaxPerson())
                .meetTime(matching.getMeetTime())
                .build();

    }
    /*
    public Matching toEntity(Long hostId) {
        return Matching.builder()
                .title(this.title)
                .description(this.description)
                .maxPerson(this.maxPerson)
                .meetTime(this.meetTime)
                .build();
    }
     */

    /*
    public Matching toEntity(Long hostId) {
        return Matching.builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .maxPerson(this.maxPerson)
                .meetTime(this.meetTime)
                .build();
    }

     */
}
