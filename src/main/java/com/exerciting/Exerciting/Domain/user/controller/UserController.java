package com.exerciting.Exerciting.Domain.user.controller;

import com.exerciting.Exerciting.Domain.user.dto.LoginRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.UserRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.UserResponseDto;
import com.exerciting.Exerciting.Domain.user.dto.UserUpdateDto;
import com.exerciting.Exerciting.Domain.user.entity.UserDetails;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody @Valid UserRequestDto dto) {
        Long Id = userService.signUp(dto);
        return ResponseEntity.ok(Id);
    }
    @DeleteMapping("/me")
    public ResponseEntity<Long> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        Long id = userService.deleteUser(userDetails.getUserId());
        return ResponseEntity.ok(id);
    }
    @PatchMapping("/me")
    public ResponseEntity<Long> updateUser(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserUpdateDto dto) {
        Long id = userService.updateUserDetail(userDetails.getUsername(),dto);
        return ResponseEntity.ok(id);
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto dto) {
        String token = userService.login(dto.userId(), dto.password());
        return ResponseEntity.ok(token);
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDto dto = userService.getUser(userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }
}
