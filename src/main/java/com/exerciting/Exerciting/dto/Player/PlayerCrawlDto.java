package com.exerciting.Exerciting.dto.Player;

import com.exerciting.Exerciting.Entity.Player;
import com.exerciting.Exerciting.Entity.SportType;
import com.exerciting.Exerciting.Entity.Team;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

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
