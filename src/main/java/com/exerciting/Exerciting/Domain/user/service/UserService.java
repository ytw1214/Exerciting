package com.exerciting.Exerciting.Domain.user.service;

import com.exerciting.Exerciting.Domain.user.dto.*;
import com.exerciting.Exerciting.Domain.user.dto.request.UserSignUpRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.request.UserUpdateRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.response.*;
import com.exerciting.Exerciting.Domain.user.entity.RefreshToken;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.RefreshTokenRepository;
import com.exerciting.Exerciting.Exception.*;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;
import com.exerciting.Exerciting.Infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public UserSignUpResponseDto signUp(UserSignUpRequestDto dto) {
        if(userRepository.existsByUserId(dto.userId())) {
            throw new DuplicateResourceException(ErrorCode.DUPLICATE_USER_ID);
        }
        if(userRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException(ErrorCode.DUPLICATE_EMAIL);
        }
        if(userRepository.existsByNickname(dto.nickname())) {
            throw new DuplicateResourceException(ErrorCode.DUPLICATE_NICKNAME);
        }
        String encodedPw = passwordEncoder.encode(dto.pw());
        User user = userRepository.save(dto.toEntity(encodedPw));
        return UserSignUpResponseDto.of(user);

    }
    @Transactional
    public UserDeleteResponseDto deleteUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException());

        log.info("{} 회원 탈퇴 완료",user.getUserId());
        userRepository.delete(user);
        return UserDeleteResponseDto.of(user.getId());
    }
    @Transactional
    public UserUpdateResponseDto updateUserDetail(String userId, UserUpdateRequestDto dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException());
        String encodedPw = null;
        if (dto.pw() != null && !dto.pw().isEmpty()) {
            encodedPw = passwordEncoder.encode(dto.pw());
        }
        user.update(dto, encodedPw);
        log.info("{} 유저 정보 변경 완료", userId);
        return UserUpdateResponseDto.of(user.getId());
    }
    @Transactional
    public TokenPairDto login(String userId, String pw) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(LoginFailedException::new);

        if (!passwordEncoder.matches(pw, user.getPw())) {
            throw new LoginFailedException();
        }
        return issueTokens(user);
    }
    @Transactional(noRollbackFor = TokenReuseDetectedException.class)
    public TokenPairDto reissue(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException();
        }
        String userId = jwtTokenProvider.getUserId(refreshToken);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(InvalidTokenException::new);
        RefreshToken savedToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(InvalidTokenException::new);

        String incomingHash = jwtTokenProvider.hashToken(refreshToken);
        if (!savedToken.hasSameHash(incomingHash)) {
            refreshTokenRepository.delete(savedToken);
            log.warn("refresh token 재사용 감지 - userId: {}, 세션 강제 무효화", userId);
            throw new TokenReuseDetectedException();
        }
        if (savedToken.isExpired(LocalDateTime.now())) {
            throw new InvalidTokenException();
        }
        return issueTokens(user);
    }
    private TokenPairDto issueTokens(User user) {
        String accessToken = jwtTokenProvider.createToken(user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        String refreshTokenHash = jwtTokenProvider.hashToken(refreshToken);
        LocalDateTime expiresAt = jwtTokenProvider.getRefreshTokenExpiresAt();

        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        token -> token.updateToken(refreshTokenHash, expiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .user(user)
                                        .tokenHash(refreshTokenHash)
                                        .expiresAt(expiresAt)
                                        .build()
                        )
                );
        return new TokenPairDto(accessToken, refreshToken);
    }
    public UserResponseDto getUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException());

        return UserResponseDto.from(user);
    }
    public User getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(()->new UserNotFoundException());
    }
    @Transactional
    public void logout(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException());
        refreshTokenRepository.deleteByUser(user);
    }
}
