package com.exerciting.Exerciting.Entity;

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

    private String name;
    @Column(name="name_en")
    private String name_english;
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;
    @Enumerated(EnumType.STRING)
    private SportType sportType;
    @OneToMany(mappedBy="team")
    private List<Player> player = new ArrayList<Player>();
    private String imgUrl;


    @Builder
    public Team(String name, Stadium stadium, SportType sportType, List<Player> player, String imgUrl) {
        this.name = name;
        this.stadium = stadium;
        this.sportType = sportType;
        this.player = player;
        this.imgUrl = imgUrl;
    }

}
