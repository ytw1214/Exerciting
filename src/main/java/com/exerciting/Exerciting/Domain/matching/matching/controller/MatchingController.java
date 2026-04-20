package com.exerciting.Exerciting.Domain.matching.matching.controller;

import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MatchingController {
    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }




    @PostMapping("/api/v1/Matching")
    public ResponseEntity<Long> saveMatching(@RequestBody MatchingRequestDto dto, Long hostId) {
        Long data = matchingService.createMatching(dto, hostId);
        return ResponseEntity.ok(data);
    }


    @GetMapping("/api/v1/Matching")
    public ResponseEntity<List<MatchingResponseDto>> getMatching() {
        /*
        List<Matching> list = matchingService.getAllMatching();

        List<MatchingResponseDto> getMatchingList = new ArrayList<MatchingResponseDto>();

        for(Matching element : list) {
            getMatchingList.add(MatchingResponseDto.fromEntity(element));
        }

        if(getMatchingList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(getMatchingList);


         */

        List<Matching> list = matchingService.getAllMatching();
        List<MatchingResponseDto> result = list.stream()
                .map(MatchingResponseDto::fromEntity)
                .toList();

        if(result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
    @GetMapping("/")
    public String HelloController() {
        return "Yammy!~";
    }
    }
