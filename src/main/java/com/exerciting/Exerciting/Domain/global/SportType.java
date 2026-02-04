package com.exerciting.Exerciting.Domain.global;

import lombok.Getter;

@Getter
public enum SportType {
    BASEBALL("야구","https://www.koreabaseball.com/Record/TeamRank/TeamRankDaily.aspx","https://www.koreabaseball.com/Player/Search.aspx"),
    FOOTBALL("축구","https://www.kleague.com/schedule.do","https://www.kleague.com/player.do");

    /*ESPORTS_("E스포츠"),
    BASKETBALL("농구"),
    VOLLEYBALL("배구");


     */
    private final String name;
    private final String rankUrl;
    private final String playerUrl;

    SportType(String name,String rankUrl, String playerUrl) {
        this.name = name;
        this.rankUrl = rankUrl;
        this.playerUrl = playerUrl;
    }

}
