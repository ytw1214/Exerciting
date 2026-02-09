package com.exerciting.Exerciting.Domain.team.service;

import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.team.dto.TeamRankCrawlDto;
import com.exerciting.Exerciting.Domain.team.entity.TeamRank;
import com.exerciting.Exerciting.Domain.team.repository.TeamRankRepository;
import com.exerciting.Exerciting.Infrastructure.crawler.CrawlerHelper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class TeamRankService {
    private final CrawlerHelper crawlerHelper;
    private final TeamRankRepository teamRankRepository;
    public TeamRankService(CrawlerHelper crawlerHelper, TeamRankRepository teamRankRepository) {
        this.crawlerHelper = crawlerHelper;
        this.teamRankRepository = teamRankRepository;
    }

    public List<TeamRank> getAllTeamByRank() {
        return teamRankRepository.findByTeamName()
                .stream()
                .sorted(Comparator.comparing(TeamRank::getTeamRank))
                .toList();
    }

    public List<TeamRank> getTeamContainingName(String name) {
        return teamRankRepository.findByTeamNameContaining(name)
                .stream()
                .toList();
    }
}
