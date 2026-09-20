package com.exerciting.Exerciting.Domain.matching.matching.controller;

import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingQueryResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingCustomCond;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Infrastructure.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matching")
public class MatchingController {
    private final MatchingService matchingService;

    @PostMapping
    public ResponseEntity<Long> saveMatching(
            @RequestBody MatchingRequestDto dto,
            @AuthenticationPrincipal LoginUser loginUser) {
        Long matchingId = matchingService.createMatching(dto, loginUser.getId());
        return ResponseEntity.ok(matchingId);
    }

    @GetMapping
    public ResponseEntity<Page<MatchingQueryResponseDto>> getMatching(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(matchingService.getAllMatching(page, size));
    }

    @PostMapping("/{matchingId}/join")
    public ResponseEntity<Void> joinMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal LoginUser loginUser) {
        matchingService.joinMatching(matchingId, loginUser.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{matchingId}/reopen")
    public ResponseEntity<Void> reopenMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal LoginUser loginUser) {
        matchingService.reopenMatching(matchingId, loginUser.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{matchingId}/close")
    public ResponseEntity<Void> closeMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal LoginUser loginUser) {
        matchingService.closeMatching(matchingId, loginUser.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{matchingId}")
    public ResponseEntity<Void> updateMatching(
            @PathVariable Long matchingId,
            @RequestBody MatchingRequestDto dto,
            @AuthenticationPrincipal LoginUser loginUser) {
        matchingService.updateMatching(matchingId, loginUser.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{matchingId}")
    public ResponseEntity<Void> deleteMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal LoginUser loginUser) {
        matchingService.deleteMatching(matchingId, loginUser.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{matchingId}/leave")
    public ResponseEntity<Void> leaveMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal LoginUser loginUser) {
        matchingService.leaveMatching(matchingId, loginUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<MatchingQueryResponseDto>> searchMatching(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) LocalDateTime meetTime,
            @AuthenticationPrincipal LoginUser loginUser) {
        MatchingCustomCond cond = new MatchingCustomCond(title, description, teamName, meetTime);
        return ResponseEntity.ok(matchingService.searchDetailMatching(cond, loginUser.getId()));
    }
}
