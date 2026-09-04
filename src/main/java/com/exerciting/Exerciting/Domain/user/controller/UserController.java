package com.exerciting.Exerciting.Domain.user.controller;

import com.exerciting.Exerciting.Domain.user.dto.*;
import com.exerciting.Exerciting.Domain.user.dto.request.LoginRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.request.UserSignUpRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.request.UserUpdateRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.response.*;
import com.exerciting.Exerciting.Domain.user.entity.UserDetails;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private static final String REFRESH_TOKEN_COOKIE = "REFERSH_TOKEN";

    @PostMapping("/signup")
    public ResponseEntity<UserSignUpResponseDto> signup(@RequestBody @Valid UserSignUpRequestDto dto) {
        UserSignUpResponseDto result = userService.signUp(dto);
        return ResponseEntity.ok(result);
    }
    @DeleteMapping("/me")
    public ResponseEntity<UserDeleteResponseDto> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDeleteResponseDto result = userService.deleteUser(userDetails.getUserId());
        return ResponseEntity.ok(result);
    }
    @PatchMapping("/me")
    public ResponseEntity<UserUpdateResponseDto> updateUser(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserUpdateRequestDto dto) {
        UserUpdateResponseDto result = userService.updateUserDetail(userDetails.getUsername(),dto);
        return ResponseEntity.ok(result);
    }
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto dto) {
        TokenResponseDto token = userService.login(dto.userId(),dto.password());
        return ResponseEntity.ok(token);
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDto dto = userService.getUser(userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        userService.logout(userDetails.getUserId());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expireRefreshTokenCookie().toString())
                .build();
    }
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue(@CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }
        TokenReissuePairDto tokenPair = userService.reissue(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(tokenPair.refreshToken()).toString())
                .body(new TokenResponseDto(tokenPair.accessToken(), tokenPair.refreshToken()));
    }
    private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE,refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/user")
                .maxAge(java.time.Duration.ofDays(14))
                .build();
    }

    private ResponseCookie expireRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/user")
                .maxAge(0)
                .build();
    }
}
