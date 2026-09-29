package com.exerciting.Exerciting.Domain.matching.matching.controller;

import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingCreateResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingQueryResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingStatusResponseDto;
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
    public ResponseEntity<MatchingCreateResponseDto> saveMatching(
            @RequestBody MatchingRequestDto dto,
            @AuthenticationPrincipal LoginUser loginUser) {
        return ResponseEntity.ok(matchingService.createMatching(dto, loginUser.getId()));
    }

    @GetMapping
    public ResponseEntity<Page<MatchingQueryResponseDto>> getMatching(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // size 상한이 없으면 한 번의 요청으로 전체 매칭과 연관 엔티티를 메모리에 올릴 수 있다
        return ResponseEntity.ok(matchingService.getAllMatching(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
    }

    @PostMapping("/{matchingId}/join")
    public ResponseEntity<MatchingStatusResponseDto> joinMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal LoginUser loginUser) {
        return ResponseEntity.ok(matchingService.joinMatching(matchingId, loginUser.getId()));
    }

    @PatchMapping("/{matchingId}/reopen")
    public ResponseEntity<MatchingStatusResponseDto> reopenMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal LoginUser loginUser) {
        return ResponseEntity.ok(matchingService.reopenMatching(matchingId, loginUser.getId()));
    }

    @PatchMapping("/{matchingId}/close")
    public ResponseEntity<MatchingStatusResponseDto> closeMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal LoginUser loginUser) {
        return ResponseEntity.ok(matchingService.closeMatching(matchingId, loginUser.getId()));
    }

    @PatchMapping("/{matchingId}")
    public ResponseEntity<MatchingStatusResponseDto> updateMatching(
            @PathVariable Long matchingId,
            @RequestBody MatchingRequestDto dto,
            @AuthenticationPrincipal LoginUser loginUser) {
        return ResponseEntity.ok(matchingService.updateMatching(matchingId, loginUser.getId(), dto));
    }

    @DeleteMapping("/{matchingId}")
    public ResponseEntity<MatchingStatusResponseDto> deleteMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal LoginUser loginUser) {
        return ResponseEntity.ok(matchingService.deleteMatching(matchingId, loginUser.getId()));
    }

    @DeleteMapping("/{matchingId}/leave")
    public ResponseEntity<MatchingStatusResponseDto> leaveMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal LoginUser loginUser) {
        return ResponseEntity.ok(matchingService.leaveMatching(matchingId, loginUser.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MatchingQueryResponseDto>> searchMatching(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) LocalDateTime meetTime) {
        MatchingCustomCond cond = new MatchingCustomCond(title, description, teamName, meetTime);
        return ResponseEntity.ok(matchingService.searchDetailMatching(cond));
    }
}
