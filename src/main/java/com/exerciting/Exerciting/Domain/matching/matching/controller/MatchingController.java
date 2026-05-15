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
    private final MatchingChatRoomService matchingChatRoomService;
    private final MatchingParticipantService matchingParticipantService;
    @PostMapping
    public ResponseEntity<Long> saveMatching(
            @RequestBody MatchingRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUserId(userDetails.getUserId());
        Long matchingId = matchingService.createMatching(dto, user.getId());
        Matching matching = matchingService.findById(matchingId);
        matchingChatRoomService.createChatRoom(matching,user);
        matchingParticipantService.joinMatching(matchingId, user.getId());
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
}