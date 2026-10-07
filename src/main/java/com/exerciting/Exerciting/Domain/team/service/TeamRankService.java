package com.exerciting.Exerciting.Domain.team.service;

import com.exerciting.Exerciting.Domain.team.dto.TeamRankCrawlDto;
import com.exerciting.Exerciting.Domain.team.entity.TeamRank;
import com.exerciting.Exerciting.Domain.team.repository.TeamRankRepository;
import com.exerciting.Exerciting.Exception.CrawlingException;
import com.exerciting.Exerciting.Infrastructure.crawler.fetcher.KboRankFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamRankService {
    private final TeamRankRepository teamRankRepository;
    private final KboRankFetcher kboRankFetcher;

    public List<TeamRank> getAllTeamByRank() {
        return teamRankRepository.findAllByOrderByTeamRankAsc();
    }

    /**
     * 순위 동기화 = 팀 이름 기준 upsert.
     * 예전에는 호출할 때마다 새 행을 saveAll 해서, 5번 동기화하면 순위표에 1위가 5번 나왔다.
     */
    @Transactional
    public int updateTeamRank() {
        List<TeamRankCrawlDto> crawled = kboRankFetcher.fetch();
        if (crawled.isEmpty()) {
            // 순위 표를 못 찾으면 빈 목록이 온다. "0개팀 수정 완료(200)"로 넘기지 않고 실패로 알린다.
            throw new CrawlingException();
        }
        List<String> teamNames = crawled.stream().map(TeamRankCrawlDto::getTeamName).toList();
        Map<String, TeamRank> saved = teamRankRepository.findAllByTeamNameIn(teamNames).stream()
                .collect(Collectors.toMap(TeamRank::getTeamName, Function.identity(), (first, duplicate) -> first));

        int inserted = 0;
        for (TeamRankCrawlDto dto : crawled) {
            TeamRank existing = saved.get(dto.getTeamName());
            if (existing == null) {
                teamRankRepository.save(dto.toEntity());
                inserted++;
            } else {
                existing.updateFrom(dto);
            }
        }
        log.info("팀 순위 동기화 - 신규 {}팀, 갱신 {}팀", inserted, crawled.size() - inserted);
        return crawled.size();
    }

    public List<TeamRank> getTeamContainingName(String name) {
        return teamRankRepository.findByTeamNameContaining(name);
    }
}
