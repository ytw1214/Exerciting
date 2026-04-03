package com.exerciting.Exerciting.Domain.global;

import lombok.Getter;

@Getter
public enum SportType {
    BASEBALL("야구","https://www.koreabaseball.com/Record/TeamRank/TeamRankDaily.aspx"),
    FOOTBALL("축구","https://www.kleague.com/schedule.do");

    /*ESPORTS_("E스포츠"),
    BASKETBALL("농구"),
    VOLLEYBALL("배구");


     */
    private final String name;
    private final String rankUrl;

    SportType(String name,String rankUrl) {
        this.name = name;
        this.rankUrl = rankUrl;
    }

}
