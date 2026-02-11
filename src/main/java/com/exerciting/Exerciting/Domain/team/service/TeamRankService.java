package com.exerciting.Exerciting.Domain.team.service;

import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.team.dto.TeamRankCrawlDto;
import com.exerciting.Exerciting.Domain.team.entity.TeamRank;
import com.exerciting.Exerciting.Domain.team.repository.TeamRankRepository;
import com.exerciting.Exerciting.Infrastructure.crawler.CrawlerHelper;
import com.exerciting.Exerciting.Infrastructure.crawler.fetcher.KboRankFetcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeamRankService {
    private final CrawlerHelper crawlerHelper;
    private final TeamRankRepository teamRankRepository;
    private final KboRankFetcher kboRankFetcher;
    public TeamRankService(CrawlerHelper crawlerHelper, TeamRankRepository teamRankRepository, KboRankFetcher kboRankFetcher) {
        this.crawlerHelper = crawlerHelper;
        this.teamRankRepository = teamRankRepository;
        this.kboRankFetcher = kboRankFetcher;
    }

    public List<TeamRank> getAllTeamByRank() {
        return teamRankRepository.findByRanking()
                .stream()
                .sorted(Comparator.comparing(TeamRank::getTeamRank))
                .toList();
    }

    public List<TeamRank> getTeamContainingName(String name) {
        return teamRankRepository.findByTeamNameContaining(name)
                .stream()
                .toList();
    }

    public List<TeamRank> comparingData() {
        List<TeamRankCrawlDto> rankings = kboRankFetcher.fetch();
        List<TeamRank> changeLists = new ArrayList<>();
        List<String> teamNames = rankings.stream()
                .map(TeamRankCrawlDto::getTeamName)
                .toList();
        for(TeamRankCrawlDto dto : rankings) {
            TeamRank current = dto.toEntity(teamNames.indexOf(dto.getTeamName()));
            if(current == null || current.isChanged(dto)) {
                changeLists.add(dto.toEntity());
            }
        }
        return changeLists;
    }
}
