package com.exerciting.Exerciting.dto;

import com.exerciting.Exerciting.Entity.Matching;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
public class MatchingRequestDto {

    private String matchName;
    private String description;
    private int maxMember;
    private LocalDateTime matchTime;
    private Activity activity;


    public Matching toEntity(Long hostId) {
        return Matching.builder()
                .matchName(this.matchName)
                .description(this.description)
                .maxPerson(this.maxMember)
                .activity(this.activity)
                .matchTime(this.matchTime)
                .build();
    }

}
