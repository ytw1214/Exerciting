package com.exerciting.Exerciting.Controller;

import com.exerciting.Exerciting.Entity.TeamRank;
import com.exerciting.Exerciting.Service.Crawling.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranks")
@RequiredArgsConstructor
public class TeamRankController {

    private final CrawlingService crawlingService;

    // 사용자가 http://localhost:8080/api/ranks/update 접속 시 실행

    /*
    @GetMapping("/update")
    public ResponseEntity<List<TeamRank>> updateRankings() {
        //List<TeamRank> updatedData = crawlingService.getRank();
        return ResponseEntity.ok(updatedData);
    }

     */
}