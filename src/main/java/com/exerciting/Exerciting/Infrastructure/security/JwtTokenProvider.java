package com.exerciting.Exerciting.Infrastructure.security;

import com.exerciting.Exerciting.Domain.user.entity.Role;
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

    // 토큰에 붙이는 "이름표". access 토큰과 refresh 토큰을 구분한다.
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    // 사용자 PK. 숫자를 문자열로 담아 파싱 방식에 따른 타입 문제를 피한다.
    private static final String USER_PK_CLAIM = "uid";
    // 권한. 토큰만으로 ADMIN 여부를 판단한다(권한이 바뀌면 다시 로그인해야 반영된다)
    private static final String ROLE_CLAIM = "role";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private static final Duration ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(30);
    private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(14);

    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /** access 토큰에는 사용자 PK를 함께 담아, 요청마다 DB를 조회하지 않게 한다. */
    public String createToken(String userId, Long userPk, Role role) {
        return Jwts.builder()
                .claim(USER_PK_CLAIM, String.valueOf(userPk))
                .claim(ROLE_CLAIM, (role == null ? Role.USER : role).name())
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .claim(TOKEN_TYPE_CLAIM, ACCESS)
                .setIssuedAt(new Date())
                .setExpiration(expirationFrom(ACCESS_TOKEN_VALIDITY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** refresh 토큰은 재발급에만 쓰이고, 그때 DB에서 사용자를 찾으므로 PK를 담지 않는다. */
    public String createRefreshToken(String userId) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .claim(TOKEN_TYPE_CLAIM, REFRESH)
                .setIssuedAt(new Date())
                .setExpiration(expirationFrom(REFRESH_TOKEN_VALIDITY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Date expirationFrom(Duration validity) {
        return new Date(System.currentTimeMillis() + validity.toMillis());
    }

    public String getUserId(String token) {
        return parse(token).getSubject();
    }

    /** 토큰만으로 출입증을 만든다. PK가 없는 옛 토큰이면 null을 반환한다. */
    public LoginUser getLoginUser(String token) {
        Claims claims = parse(token);
        String userPk = claims.get(USER_PK_CLAIM, String.class);
        if (userPk == null) {
            log.debug("사용자 PK가 없는 토큰입니다. 다시 로그인이 필요합니다.");
            return null;
        }
        try {
            return new LoginUser(Long.valueOf(userPk), claims.getSubject(), roleOf(claims.get(ROLE_CLAIM, String.class)));
        } catch (NumberFormatException e) {
            log.debug("사용자 PK 형식이 올바르지 않습니다: {}", userPk);
            return null;
        }
    }

    // role 클레임이 없는 예전 토큰이나 알 수 없는 값은 일반 사용자로 본다
    private Role roleOf(String value) {
        if (value == null) {
            return Role.USER;
        }
        try {
            return Role.valueOf(value);
        } catch (IllegalArgumentException e) {
            return Role.USER;
        }
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
