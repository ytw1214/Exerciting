package com.exerciting.Exerciting.Domain.matching.matching.controller;

import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingResponseDto;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.entity.UserDetails;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;
    private final UserService userService;

    @PostMapping("/api/v1/Matching")
    public ResponseEntity<Long> saveMatching(
            @RequestBody MatchingRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUserId(userDetails.getUserId());
        Long data = matchingService.createMatching(dto, user.getId());

        return ResponseEntity.ok(data);
    }


    @GetMapping("/api/v1/Matching")
    public ResponseEntity<List<MatchingResponseDto>> getMatching() {
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
