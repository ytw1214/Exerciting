package com.exerciting.Exerciting.Domain.team.service;

import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.team.dto.TeamRankCrawlDto;
import com.exerciting.Exerciting.Domain.team.entity.TeamRank;
import com.exerciting.Exerciting.Domain.team.repository.TeamRankRepository;
import com.exerciting.Exerciting.Infrastructure.crawler.CrawlerHelper;
import com.exerciting.Exerciting.Infrastructure.crawler.fetcher.KboRankFetcher;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
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
        return teamRankRepository.findAllByOrderByTeamRankAsc();
    }
    //@PostConstruct
    public void saveTeamRank() {
        List<TeamRankCrawlDto> arr = kboRankFetcher.fetch();
        List<TeamRank> arr1 = new ArrayList<>();
        for(TeamRankCrawlDto dto : arr) {
            arr1.add(dto.toEntity());
        }
        teamRankRepository.saveAll(arr1);
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

        Map<String,TeamRank> map = teamRankRepository.findAllByTeamNameIn(teamNames)
                .stream()
                .collect(Collectors.toMap(TeamRank::getTeamName, team -> team));

        for(TeamRankCrawlDto dto : rankings) {
            TeamRank crawlingTeamRank = map.get(dto.getTeamName());
            if(crawlingTeamRank == null || crawlingTeamRank.isChanged(dto)) {
                changeLists.add(dto.toEntity());
            }
        }
        return changeLists;
    }

}
