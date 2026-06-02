package com.exerciting.Exerciting.Domain.matching.matching.controller;

import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingQueryResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingCustomCond;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingResponseDto;
import com.exerciting.Exerciting.Domain.user.entity.UserDetails;
import com.exerciting.Exerciting.Domain.user.service.UserService;
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
    private final UserService userService;
    @PostMapping
    public ResponseEntity<Long> saveMatching(
            @RequestBody MatchingRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUserId(userDetails.getUserId()).getId();
        Long matchingId = matchingService.createMatching(dto, userId);
        return ResponseEntity.ok(matchingId);
    }
    @GetMapping
    public ResponseEntity<Page<MatchingQueryResponseDto>> getMatching(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(matchingService.getAllMatching(page,size));
    }
    @PostMapping("/{matchingId}/join")
    public ResponseEntity<Void> joinMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUserId(userDetails.getUserId()).getId();
        matchingService.joinMatching(matchingId, userId);
        return ResponseEntity.ok().build();
    }
    @PatchMapping("/{matchingId}/reopen")
    public ResponseEntity<Void> reopenMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUserId(userDetails.getUserId()).getId();
        matchingService.reopenMatching(matchingId, userId);
        return ResponseEntity.ok().build();
    }
    @PatchMapping("{matchingId}/close")
    public ResponseEntity<Void> closeMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUserId(userDetails.getUserId()).getId();
        matchingService.closeMatching(matchingId, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{matchingId}")
    public ResponseEntity<Void> updateMatching(
            @PathVariable Long matchingId,
            @RequestBody MatchingRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUserId(userDetails.getUserId()).getId();
        matchingService.updateMatching(matchingId, userId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{matchingId}")
    public ResponseEntity<Void> deleteMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUserId(userDetails.getUserId()).getId();
        matchingService.deleteMatching(matchingId, userId);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{matchingId}/leave")
    public ResponseEntity<Void> leaveMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUserId(userDetails.getUserId()).getId();
        matchingService.leaveMatching(matchingId, userId);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<List<MatchingQueryResponseDto>> searchMatching(
            @PathVariable String title,
            @PathVariable String description,
            @PathVariable String teamName,
            @PathVariable LocalDateTime meetTime,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserByUserId(userDetails.getUserId()).getId();
        MatchingCustomCond cond = new MatchingCustomCond(title,description,teamName,meetTime);
        List<MatchingQueryResponseDto> matching = matchingService.searchDetailMatching(cond, userId);

        return ResponseEntity.ok(matching);
    }

}