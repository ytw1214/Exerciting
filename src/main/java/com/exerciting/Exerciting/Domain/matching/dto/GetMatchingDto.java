package com.exerciting.Exerciting.Domain.matching.dto;

import com.exerciting.Exerciting.Domain.matching.entity.Matching;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GetMatchingDto {
    private Long id;
    private String title;
    private String description;
    private int maxPerson;
    private int currentPerson;
    private LocalDateTime meetTime;

    public GetMatchingDto(Matching matching) {
        this.id = matching.getId();
        this.title = matching.getTitle();
        this.description = matching.getDescription();
        this.maxPerson = matching.getMaxPerson();
        this.currentPerson = matching.getCurrentPerson();
        this.meetTime = matching.getMeetTime();
    }
}
