package com.exerciting.Exerciting.Domain.matching.matching.dto;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@Builder
public class MatchingRequestDto {
    private String title;
    private String description;
    private int maxPerson;
    private LocalDateTime meetTime;
    private Long gameId;
    public MatchingRequestDto(String title, String description, int maxPerson, LocalDateTime meetTime, Long gameId) {
        this.title = title;
        this.description = description;
        this.maxPerson = maxPerson;
        this.meetTime = meetTime;
        this.gameId = gameId;
    }
    public static MatchingRequestDto fromEntity(Matching matching) {
        return MatchingRequestDto.builder()
                .title(matching.getTitle())
                .description(matching.getDescription())
                .maxPerson(matching.getMaxPerson())
                .meetTime(matching.getMeetTime())
                .build();

    }
    public Matching toEntity(User user, Game game) {
        return Matching.builder()
                .title(this.getTitle())
                .description(this.getDescription())
                .maxPerson(this.getMaxPerson())
                .meetTime(this.meetTime)
                .user(user)
                .game(game)
                .build();
    }
}
