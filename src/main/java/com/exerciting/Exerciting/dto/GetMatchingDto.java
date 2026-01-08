package com.exerciting.Exerciting.dto;

import com.exerciting.Exerciting.Entity.Matching;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GetMatchingDto {
    private Long id;
    private String matchName;
    private String description;
    private int maxPerson;
    private int currentPerson;
    private Activity activity;
    private LocalDateTime matchTime;

    public GetMatchingDto(Matching matching) {
        this.id = matching.getId();
        this.matchName = matching.getMatchName();
        this.description = matching.getDescription();
        this.maxPerson = matching.getMaxPerson();
        this.currentPerson = matching.getCurrentPerson();
        this.activity = matching.getActivity(); // Enum이므로 안전
        this.matchTime = matching.getMatchTime();
    }
}
