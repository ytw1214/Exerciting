package com.exerciting.Exerciting.Controller;

import com.exerciting.Exerciting.Service.Crawling.BaseballCrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranks")
@RequiredArgsConstructor
public class TeamRankController {

    private final BaseballCrawlingService crawlingService;

    // 사용자가 http://localhost:8080/api/ranks/update 접속 시 실행

    /*
    @GetMapping("/update")
    public ResponseEntity<List<TeamRank>> updateRankings() {
        //List<TeamRank> updatedData = crawlingService.getRank();
        return ResponseEntity.ok(updatedData);
    }

     */
}