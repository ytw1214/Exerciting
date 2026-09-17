package com.exerciting.Exerciting.Infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private static final Duration ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(30);
    private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(14);

    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String createToken(String userId) {
        return build(userId, ACCESS, ACCESS_TOKEN_VALIDITY);
    }

    public String createRefreshToken(String userId) {
        return build(userId, REFRESH, REFRESH_TOKEN_VALIDITY);
    }

    private String build(String userId, String type, Duration validity) {
        Date now = new Date();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .claim(TOKEN_TYPE_CLAIM, type)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validity.toMillis()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public String getUserId(String token) {
        return parse(token).getSubject();
    }

    /** API 요청과 WebSocket 연결에는 access 토큰만 허용한다. */
    public boolean validateAccessToken(String token) {
        return hasType(token, ACCESS);
    }

    /** 재발급(/user/reissue)에는 refresh 토큰만 허용한다. */
    public boolean validateRefreshToken(String token) {
        return hasType(token, REFRESH);
    }

    private boolean hasType(String token, String expectedType) {
        try {
            Claims claims = parse(token);
            return expectedType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 토큰: {}", e.getMessage());
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Duration getRefreshTokenValidity() {
        return REFRESH_TOKEN_VALIDITY;
    }

    public LocalDateTime getRefreshTokenExpiresAt() {
        return LocalDateTime.now().plus(REFRESH_TOKEN_VALIDITY);
    }

    /** DB에는 refresh 토큰 원문 대신 SHA-256 해시(지문)만 저장한다. */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("해시 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}