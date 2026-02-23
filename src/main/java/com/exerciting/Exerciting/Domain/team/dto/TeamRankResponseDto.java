package com.exerciting.Exerciting.Domain.team.dto;

import com.exerciting.Exerciting.Domain.global.SportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class TeamRankResponseDto {
    // 종목별 필드
    private SportType sportType;
    private String teamName;
    private int rank;
    // 공통 필드
    private int games;
    private int wins;
    private int losses;
    private int draws;

    //종목별 세부필드
    private BigDecimal winRate; // 야구 : 승률, 축구 : 승점, 배구 : 승점
    private BigDecimal gamesBehind; // 야구 : 게임차, 축구 : 득실차, 배구 : 세트득실률

}
