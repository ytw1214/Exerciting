package com.exerciting.Exerciting.Controller;

import com.exerciting.Exerciting.Service.Group.MatchService;
import com.exerciting.Exerciting.dto.MatchingRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class MatchingController {
    private final MatchService matchService;

    public MatchingController(MatchService matchService) {
        this.matchService = matchService;
    }
    @PostMapping("/api/v1/Matching")
    public ResponseEntity<Long> saveMatching(@RequestBody MatchingRequestDto dto, Long hostId) {
        hostId = 1L;
        Long data = matchService.createMatching(dto, hostId);
        return ResponseEntity.created(URI.create("/api/v1/Matching/" + data)).build();
    }
}
