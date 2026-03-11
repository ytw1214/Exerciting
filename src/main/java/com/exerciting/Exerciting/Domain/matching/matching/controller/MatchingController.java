package com.exerciting.Exerciting.Domain.matching.matching.controller;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MatchingController {
    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }



    /*
    @PostMapping("/api/v1/Matching")
    public ResponseEntity<Long> saveMatching(@RequestBody MatchingRequestDto dto, Long hostId) {
        hostId = 1L;
        Long data = matchingService.createMatching(dto, hostId);
        return ResponseEntity.created(URI.create("/api/v1/Matching/" + data)).build();
    }

     */
    @GetMapping("/api/v1/Matching")
    public ResponseEntity<List<MatchingResponseDto>> getMatching() {
        List<Matching> list = matchingService.getAllMatching();

        List<MatchingResponseDto> getMatchingList = new ArrayList<MatchingResponseDto>();

        for(Matching element : list) {
            getMatchingList.add(MatchingResponseDto.fromEntity(element));
        }

        if(getMatchingList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(getMatchingList);
    }
    @GetMapping("/")
    public String HelloController() {
        return "Yammy!~";
    }
    }
