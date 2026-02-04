package com.exerciting.Exerciting.Domain.team.dto;

import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.team.entity.TeamRank;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TeamRankCrawlDto {
    // 종목별 필드
    private SportType sportType;
    private String teamName;
    private int rank;
    // 공통 필드
    private int games;
    private int wins;
    private int draws;
    private int losses;
    private LocalDateTime crawledAt; //크롤링 시간
    private String dataSource;
    //종목별 세부필드
    private BigDecimal winRate; // 야구 : 승률, 축구 : 승점, 배구 : 승점
    private BigDecimal gamesBehind; // 야구 : 게임차, 축구 : 득실차, 배구 : 세트득실률

    public TeamRank toEntity() {
        return TeamRank.builder()
                .teamName(teamName)
                .teamRank(rank)
                .games(games)
                .wins(wins)
                .draws(draws)
                .losses(losses)
                .crawledAt(crawledAt)
                .winRate(winRate)
                .gamesBehind(gamesBehind)
                .sportType(sportType)
                .dataSource(dataSource)
                .build();
    }
}
