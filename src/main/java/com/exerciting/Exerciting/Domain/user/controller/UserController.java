package com.exerciting.Exerciting.Domain.user.controller;

import com.exerciting.Exerciting.Domain.user.dto.UserRequestDto;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody UserRequestDto dto) {
        Long userId = userService.signUp(dto);
        return ResponseEntity.ok(userId);
    }
}
