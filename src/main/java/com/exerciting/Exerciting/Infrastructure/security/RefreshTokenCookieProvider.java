package com.exerciting.Exerciting.Infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
@Component
public class RefreshTokenCookieProvider {

    public static final String COOKIE_NAME = "REFRESH_TOKEN";
    private static final String COOKIE_PATH = "/user";
    private final boolean secure;
    private final Duration maxAge;

    public RefreshTokenCookieProvider(
            @Value("${app.cookie.secure:true}") boolean secure,
            JwtTokenProvider jwtTokenProvider) {
        this.secure = secure;
        this.maxAge = jwtTokenProvider.getRefreshTokenValidity();
    }
    public ResponseCookie create(String refreshToken) {
        return baseCookie(refreshToken).maxAge(maxAge).build();
    }
    public ResponseCookie expire() {
        return baseCookie("").maxAge(0).build();
    }
    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(COOKIE_PATH);
    }
}
