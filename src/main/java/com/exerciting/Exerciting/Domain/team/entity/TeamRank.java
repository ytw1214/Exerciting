package com.exerciting.Exerciting.Domain.team.entity;

import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.team.dto.TeamRankCrawlDto;
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
    @Column(scale = 3)
    private BigDecimal winRate;
    @Column(scale = 3)
    private BigDecimal gamesBehind;

    private String dataSource;
    private LocalDateTime crawledAt;

    public boolean isChanged(TeamRankCrawlDto dto) {
        return !(dto.getGames() == this.games) ||
                !(dto.getWins() == this.wins) ||
                !(dto.getLosses() == this.losses) ||
                !(dto.getWinRate() == this.winRate);
    }
}
