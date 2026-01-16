package com.exerciting.Exerciting.Entity;

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
    private String name;
    private String position;
    private int age;

    @Builder
    public Player(Team team, String name, String position, int age) {
        this.team = team;
        this.name = name;
        this.position = position;
        this.age = age;
    }

}
