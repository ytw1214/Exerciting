package com.exerciting.Exerciting.Domain.matching.matching.controller;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.service.MatchingChatRoomService;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingResponseDto;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.service.MatchingParticipantService;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.entity.UserDetails;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    )

}