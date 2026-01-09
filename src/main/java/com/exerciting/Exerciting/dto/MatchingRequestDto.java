package com.exerciting.Exerciting.dto;

import com.exerciting.Exerciting.Entity.Matching;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
public class MatchingRequestDto {

    private String title;
    private String description;
    private int maxPerson;
    private LocalDateTime meetTime;


    public Matching toEntity(Long hostId) {
        return Matching.builder()
                .title(this.title)
                .description(this.description)
                .maxPerson(this.maxPerson)
                .meetTime(this.meetTime)
                .build();
    }

}
