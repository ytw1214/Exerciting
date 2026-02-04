package com.exerciting.Exerciting.Domain.player.entity;

import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.team.entity.Team;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Player {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
    private String teamName;
    private String name;
    @Enumerated(EnumType.STRING)
    private SportType sportType;
    private String position;
    private int age;

    @Builder
    public Player(Team team, String teamName, String name, SportType sportType, String position, int age) {
        this.team = team;
        this.teamName = teamName;
        this.name = name;
        this.sportType = sportType;
        this.position = position;
        this.age = age;
    }

}
