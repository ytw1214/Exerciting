package com.exerciting.Exerciting.Domain.user.service;

import com.exerciting.Exerciting.Domain.user.dto.*;
import com.exerciting.Exerciting.Domain.user.entity.RefreshToken;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.RefreshTokenRepository;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Exception.UserNotFoundException;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    //데이터 조회
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()->new UserNotFoundException());
    }
    @Transactional
    public Long signUp(UserRequestDto dto) {
        if(userRepository.existsByUserId(dto.userId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디 입니다.");
        }
        if(userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("이미 사용중인 이메일 입니다.");
        }
        if(userRepository.existsByNickname(dto.nickname())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임 입니다.");
        }
        String encodedPw = passwordEncoder.encode(dto.pw());
        return userRepository.save(dto.toEntity(encodedPw)).getId();
    }
    @Transactional
    public Long deleteUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException());

        log.info("{} 회원 탈퇴 완료",user.getUserId());
        userRepository.delete(user);
        return user.getId();
    }
    @Transactional
    public Long updateUserDetail(String userId, UserUpdateDto dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException());
        String encodedPw = null;
        if (dto.pw() != null && !dto.pw().isEmpty()) {
            encodedPw = passwordEncoder.encode(dto.pw());
        }
        user.update(dto, encodedPw);
        log.info("{} 유저 정보 변경 완료", userId);
        return user.getId();
    }
    @Transactional
    public TokenResponseDto login(String userId, String pw) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException());

        if (!passwordEncoder.matches(pw, user.getPw())) {
            throw new InvalidInputException();
        }
        String accessToken = jwtTokenProvider.createToken(user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken, jwtTokenProvider.getRefreshTokenExpiresAt()),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .user(user)
                                        .tokenHash(refreshToken)
                                        .expiresAt(jwtTokenProvider.getRefreshTokenExpiresAt())
                                        .build()
                        )
                );
        return new TokenResponseDto(accessToken, refreshToken);
    }
    @Transactional
    public TokenPairDto reissue(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidInputException();
        }
        String userId = jwtTokenProvider.getUserId(refreshToken);
        String incomingHash = jwtTokenProvider.hashToken(refreshToken);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException());

        RefreshToken savedToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new InvalidInputException());
        if (!savedToken.getTokenHash().equals(incomingHash)) {
            refreshTokenRepository.deleteByUser(user);
            log.warn("{} refresh token 재사용 감지 - 세션 강제 무효화", userId);
            throw new InvalidInputException();
        }

        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException();
        }
        String newAccessToken = jwtTokenProvider.createToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
        String newRefreshTokenHash = jwtTokenProvider.hashToken(newRefreshToken);

        savedToken.updateToken(newRefreshTokenHash, jwtTokenProvider.getRefreshTokenExpiresAt());

        return new TokenPairDto(newAccessToken, newRefreshToken);
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
