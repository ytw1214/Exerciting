package com.exerciting.Exerciting.Domain.user.controller;

import com.exerciting.Exerciting.Domain.user.dto.*;
import com.exerciting.Exerciting.Domain.user.dto.request.LoginRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.request.UserSignUpRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.request.UserUpdateRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.response.*;
import com.exerciting.Exerciting.Domain.user.entity.RefreshToken;
import com.exerciting.Exerciting.Domain.user.entity.UserDetails;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Exception.InvalidTokenException;
import com.exerciting.Exerciting.Infrastructure.security.RefreshTokenCookieProvider;
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
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

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
        TokenPairDto token = userService.login(dto.userId(),dto.password());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.create(token.refreshToken()).toString())
                .body(new TokenResponseDto(token.accessToken()));
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
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.expire().toString())
                .build();
    }
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue(
            @CookieValue(name = RefreshTokenCookieProvider.COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException();
        }
        TokenPairDto tokenPair = userService.reissue(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.create(tokenPair.refreshToken()).toString())
                .body(new TokenResponseDto(tokenPair.accessToken()));
    }
}
