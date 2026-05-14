package com.exerciting.Exerciting.Domain.matching.matchingParticipant.controller;

import com.exerciting.Exerciting.Domain.matching.matchingParticipant.service.MatchingParticipantService;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.entity.UserDetails;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/participant")
public class MatchingParticipantController {
    private final MatchingParticipantService matchingParticipantService;
    private final UserService userService;
    @PostMapping("/{matchingId}/join")
    public ResponseEntity<Void> joinMatching(
            @PathVariable Long matchingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUserId(userDetails.getUsername());
        matchingParticipantService.joinMatching(matchingId, user.getId());
        return ResponseEntity.ok().build();
    }
}
