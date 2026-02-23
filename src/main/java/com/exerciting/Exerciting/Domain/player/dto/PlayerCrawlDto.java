package com.exerciting.Exerciting.Domain.player.dto;

import com.exerciting.Exerciting.Domain.player.entity.Player;
import com.exerciting.Exerciting.Domain.global.SportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PlayerCrawlDto {
    private String teamName;
    private String name;
    private SportType sportType;
    private String position;
    private int age;

    public Player toEntity() {
        return Player.builder()
                .teamName(teamName)
                .name(name)
                .sportType(sportType)
                .build();
    }
}
