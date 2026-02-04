package com.exerciting.Exerciting.Infrastructure.crawler.fetcher;

import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.team.dto.TeamRankCrawlDto;

import java.util.List;

public interface RankFetcher {
    boolean supports(SportType sportType);
    List<TeamRankCrawlDto> fetch();

}
