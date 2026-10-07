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
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_team_rank_team_name", columnNames = "team_name"))
public class TeamRank {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SportType sportType;

    private int teamRank;
    @Column(name = "team_name")
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

    /** 같은 팀의 최신 크롤링 결과로 덮어쓴다 (upsert의 update 쪽). */
    public void updateFrom(TeamRankCrawlDto dto) {
        this.sportType = dto.getSportType();
        this.teamRank = dto.getRank();
        this.games = dto.getGames();
        this.wins = dto.getWins();
        this.losses = dto.getLosses();
        this.draws = dto.getDraws();
        this.winRate = dto.getWinRate();
        this.gamesBehind = dto.getGamesBehind();
        this.dataSource = dto.getDataSource();
        this.crawledAt = dto.getCrawledAt();
    }
}
