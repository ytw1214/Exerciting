package com.exerciting.Exerciting.dto.Matching;

import com.exerciting.Exerciting.Entity.Matching;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
public class MatchingRequestDto {
    private Long id;
    private String title;
    private String description;
    private int maxPerson;
    private LocalDateTime meetTime;


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

    public Matching toEntity(Long hostId) {
        return Matching.builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .maxPerson(this.maxPerson)
                .meetTime(this.meetTime)
                .build();
    }
}
