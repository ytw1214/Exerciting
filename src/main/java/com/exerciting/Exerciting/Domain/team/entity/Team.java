package com.exerciting.Exerciting.Domain.team.entity;

import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.stadium.entity.Stadium;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    //구단명 풀네임
    private String name;
    //크롤링 기준 매치 구단명
    private String shortName;
    @ManyToMany
    @JoinTable(
            name="team_stadium",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "stadium_id")
    )
    private List<Stadium> stadium;
    @Enumerated(EnumType.STRING)
    private SportType sportType;
    private String imgUrl;

    @Builder
    public Team(String name, List<Stadium> stadium, SportType sportType, String imgUrl) {
        this.name = name;
        this.stadium = stadium;
        this.sportType = sportType;
        this.imgUrl = imgUrl;
    }

}
