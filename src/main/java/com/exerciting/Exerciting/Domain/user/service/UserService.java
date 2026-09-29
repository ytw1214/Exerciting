package com.exerciting.Exerciting.Domain.user.service;

import com.exerciting.Exerciting.Domain.matching.matching.entity.MatchingStatus;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.ParticipantStatus;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository.MatchingParticipantRepository;
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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MatchingParticipantRepository matchingParticipantRepository;

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

    /**
     * 회원 탈퇴 = 개인정보 익명화 + 로그인 수단 제거.
     * 행을 지우면 매칭·참가·채팅 이력이 FK로 물려 있어 실패했다(기존: 사실상 항상 500).
     * 진행 중인 매칭에 참가 중이면 남은 참가자와 약속이 깨지므로 먼저 나가거나 취소하게 한다.
     */
    @Transactional
    public UserDeleteResponseDto deleteUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
        if (matchingParticipantRepository.existsByUser_IdAndStatusAndMatching_StatusIn(
                user.getId(), ParticipantStatus.JOINED, MatchingStatus.ACTIVE_STATUSES)) {
            throw new WithdrawalBlockedException();
        }
        refreshTokenRepository.deleteByUser(user);
        user.withdraw(LocalDateTime.now());
        log.info("회원 탈퇴(개인정보 익명화) 완료 - id: {}", user.getId());
        return UserDeleteResponseDto.of(user.getId());
    }

    /**
     * 비밀번호·이메일 변경에는 현재 비밀번호를 요구한다.
     * 예전에는 access 토큰만 있으면 바로 바꿀 수 있어, 토큰 하나가 새면 계정 전체를 빼앗길 수 있었다.
     */
    @Transactional
    public UserUpdateResponseDto updateUserDetail(String userId, UserUpdateRequestDto dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        boolean changesPassword = StringUtils.hasText(dto.pw());
        boolean changesEmail = StringUtils.hasText(dto.email()) && !dto.email().equals(user.getEmail());
        boolean changesNickname = StringUtils.hasText(dto.nickname()) && !dto.nickname().equals(user.getNickname());

        if ((changesPassword || changesEmail)
                && (dto.currentPw() == null || !passwordEncoder.matches(dto.currentPw(), user.getPw()))) {
            throw new InvalidCurrentPasswordException();
        }
        // 유니크 컬럼은 DB 예외(500)로 터지기 전에 409로 알려준다
        if (changesEmail && userRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (changesNickname && userRepository.existsByNickname(dto.nickname())) {
            throw new DuplicateResourceException(ErrorCode.DUPLICATE_NICKNAME);
        }

        String encodedPw = null;
        if (changesPassword) {
            encodedPw = passwordEncoder.encode(dto.pw());
            // 비밀번호가 바뀌면 다른 기기의 로그인 유지(refresh 토큰)를 끊는다
            refreshTokenRepository.deleteByUser(user);
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
        String accessToken = jwtTokenProvider.createToken(user.getUserId(), user.getId());
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
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
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
