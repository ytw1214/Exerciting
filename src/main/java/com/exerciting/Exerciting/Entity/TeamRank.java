package com.exerciting.Exerciting.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRank {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    private int rank;
    private String teamName;
    private int games;
    private int wins;
    private int losses;
    private int draws;
    private Double winRate;
    private String gamesBehind;

    private String dataSource;
    private LocalDateTime crawledAt;
}
