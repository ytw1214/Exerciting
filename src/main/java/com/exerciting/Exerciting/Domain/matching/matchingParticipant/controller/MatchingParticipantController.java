package com.exerciting.Exerciting.Domain.matching.matchingParticipant.controller;

import com.exerciting.Exerciting.Domain.matching.matchingParticipant.dto.MatchingParticipantDto;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.service.MatchingParticipantService;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/participant")
public class MatchingParticipantController {
    private final MatchingParticipantService matchingParticipantService;
    private final UserService userService;
    @GetMapping("/{matchingId}")
    public ResponseEntity<List<MatchingParticipantDto>> getParticipants(
            @PathVariable Long matchingId) {
        List<MatchingParticipantDto> result = matchingParticipantService.getParticipants(matchingId);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
}
