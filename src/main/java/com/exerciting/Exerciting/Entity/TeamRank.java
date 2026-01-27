package com.exerciting.Exerciting.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRank {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SportType sportType;

    private int teamRank;
    private String teamName;
    private int games;
    private int wins;
    private int losses;
    private int draws;
    private BigDecimal winRate;
    private BigDecimal gamesBehind;

    private String dataSource;
    private LocalDateTime crawledAt;
}
