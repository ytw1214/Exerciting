package com.exerciting.Exerciting.Domain.team.service;

import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.team.repository.TeamRankRepository;
import com.exerciting.Exerciting.Infrastructure.crawler.CrawlerHelper;
import org.springframework.stereotype.Service;

@Service
public class TeamRankService {
    private final CrawlerHelper crawlerHelper;
    private final TeamRankRepository teamRankRepository;
    public TeamRankService(CrawlerHelper crawlerHelper, TeamRankRepository teamRankRepository) {
        this.crawlerHelper = crawlerHelper;
        this.teamRankRepository = teamRankRepository;
    }

    public void getTeamRank(SportType sportType) {

    }
}
