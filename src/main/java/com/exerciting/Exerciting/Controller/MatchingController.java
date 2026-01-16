package com.exerciting.Exerciting.Controller;

import com.exerciting.Exerciting.Entity.Matching;
import com.exerciting.Exerciting.Service.MatchingService;
import com.exerciting.Exerciting.dto.GetMatchingDto;
import com.exerciting.Exerciting.dto.MatchingRequestDto;
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



    /*
    @PostMapping("/api/v1/Matching")
    public ResponseEntity<Long> saveMatching(@RequestBody MatchingRequestDto dto, Long hostId) {
        hostId = 1L;
        Long data = matchingService.createMatching(dto, hostId);
        return ResponseEntity.created(URI.create("/api/v1/Matching/" + data)).build();
    }

     */
    @GetMapping("/api/v1/Matching")
    public ResponseEntity<List<GetMatchingDto>> getMatching() {
        List<Matching> list = matchingService.getAllMatching();

        List<GetMatchingDto> getMatchingList = new ArrayList<GetMatchingDto>();

        for(Matching element : list) {
            getMatchingList.add(new GetMatchingDto(element));
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
