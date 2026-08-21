package com.exerciting.Exerciting.Domain.team.controller;

import com.exerciting.Exerciting.Domain.team.dto.TeamRankResponseDto;
import com.exerciting.Exerciting.Domain.team.entity.TeamRank;
import com.exerciting.Exerciting.Domain.team.service.TeamRankService;
import com.exerciting.Exerciting.Exception.DisMatchedSizeException;
import com.exerciting.Exerciting.Infrastructure.crawler.fetcher.KboRankFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TeamRankController {

    private final KboRankFetcher kboRankFetcher;
    private final TeamRankService teamRankService;

    @GetMapping("/ranks")
    public ResponseEntity<List<TeamRank>> getTeamRank() {
        List<TeamRank> rankList = teamRankService.getAllTeamByRank();
        if(rankList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(rankList);
    }
    @GetMapping("/ranks/sync")
    public ResponseEntity<String> updateTeamRank() {
        int updatedCount = teamRankService.updateTeamRank();
        return ResponseEntity.ok(updatedCount+"개팀 순위 수정 완료");
    }
}