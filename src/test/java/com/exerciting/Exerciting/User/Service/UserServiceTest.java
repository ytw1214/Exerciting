package com.exerciting.Exerciting.User.Service;

import com.exerciting.Exerciting.Domain.user.dto.TokenPairDto;
import com.exerciting.Exerciting.Domain.user.dto.request.UserSignUpRequestDto;
import com.exerciting.Exerciting.Domain.user.entity.RefreshToken;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.RefreshTokenRepository;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import com.exerciting.Exerciting.Exception.DuplicateResourceException;
import com.exerciting.Exerciting.Exception.InvalidTokenException;
import com.exerciting.Exerciting.Exception.LoginFailedException;
import com.exerciting.Exerciting.Exception.TokenReuseDetectedException;
import com.exerciting.Exerciting.Infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("signUp - 회원가입")
    class SignUpTest {

        @Test
        @DisplayName("정상 입력이면 userRepository.save()를 1회 호출한다")
        void signUp_success() {
            UserSignUpRequestDto dto = new UserSignUpRequestDto(
                    "testId", "Password123!", "nick", "name", "test@test.com");

            given(userRepository.existsByUserId(anyString())).willReturn(false);
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(userRepository.existsByNickname(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPw");
            given(userRepository.save(any(User.class))).willReturn(dto.toEntity("encodedPw"));

            userService.signUp(dto);

            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("userId가 중복이면 DuplicateResourceException 발생, save() 미호출")
        void signUp_fail_duplicateUserId() {
            UserSignUpRequestDto dto = new UserSignUpRequestDto(
                    "testId", "Password123!", "nick", "name", "test@test.com");

            given(userRepository.existsByUserId("testId")).willReturn(true);

            assertThatThrownBy(() -> userService.signUp(dto))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("email이 중복이면 DuplicateResourceException 발생")
        void signUp_fail_duplicateEmail() {
            UserSignUpRequestDto dto = new UserSignUpRequestDto(
                    "testId", "Password123!", "nick", "name", "test@test.com");

            given(userRepository.existsByUserId(anyString())).willReturn(false);
            given(userRepository.existsByEmail("test@test.com")).willReturn(true);

            assertThatThrownBy(() -> userService.signUp(dto))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("login - 로그인")
    class LoginTest {

        @Test
        @DisplayName("정상 로그인 시 refresh 토큰은 원문이 아닌 '해시'로 저장된다")
        void login_success_savesHashNotRawToken() {
            String userId = "testId";
            String rawPw = "Password123!";
            User user = User.builder().userId(userId).pw("encodedPw").build();

            given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(rawPw, "encodedPw")).willReturn(true);
            given(jwtTokenProvider.createToken(userId)).willReturn("mock-access-token");
            given(jwtTokenProvider.createRefreshToken(userId)).willReturn("mock-refresh-token");
            given(jwtTokenProvider.hashToken("mock-refresh-token")).willReturn("hashed-refresh");
            given(jwtTokenProvider.getRefreshTokenExpiresAt())
                    .willReturn(LocalDateTime.now().plusDays(14));
            given(refreshTokenRepository.findByUser(user)).willReturn(Optional.empty());

            TokenPairDto result = userService.login(userId, rawPw);

            // 클라이언트에게는 원문을 준다 (쿠키로 전달됨)
            assertThat(result.accessToken()).isEqualTo("mock-access-token");
            assertThat(result.refreshToken()).isEqualTo("mock-refresh-token");

            // DB에는 해시만 저장된다 ← 기존 버그를 다시 막는 테스트
            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            assertThat(captor.getValue().getTokenHash())
                    .isEqualTo("hashed-refresh")
                    .isNotEqualTo("mock-refresh-token");
        }

        @Test
        @DisplayName("존재하지 않는 userId이면 LoginFailedException 발생")
        void login_fail_userNotFound() {
            given(userRepository.findByUserId("noSuchId")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login("noSuchId", "anyPw"))
                    .isInstanceOf(LoginFailedException.class);
        }

        @Test
        @DisplayName("비밀번호가 틀려도 같은 LoginFailedException 발생, 토큰을 만들지 않는다")
        void login_fail_wrongPassword() {
            String userId = "testId";
            User user = User.builder().userId(userId).pw("encodedPw").build();

            given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongPw", "encodedPw")).willReturn(false);

            assertThatThrownBy(() -> userService.login(userId, "wrongPw"))
                    .isInstanceOf(LoginFailedException.class);

            verify(jwtTokenProvider, never()).createToken(anyString());
        }
    }

    @Nested
    @DisplayName("reissue - 토큰 재발급")
    class ReissueTest {

        private final String userId = "testId";
        private final User user = User.builder().userId(userId).pw("encodedPw").build();

        private RefreshToken savedTokenWithHash(String hash) {
            return RefreshToken.builder()
                    .user(user)
                    .tokenHash(hash)
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .build();
        }

        @Test
        @DisplayName("저장된 해시와 일치하면 새 토큰을 발급하고, 저장된 해시를 새 토큰의 해시로 교체한다")
        void reissue_success_rotatesToken() {
            RefreshToken saved = savedTokenWithHash("old-hash");

            given(jwtTokenProvider.validateRefreshToken("old-refresh")).willReturn(true);
            given(jwtTokenProvider.getUserId("old-refresh")).willReturn(userId);
            given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));
            given(refreshTokenRepository.findByUser(user)).willReturn(Optional.of(saved));
            given(jwtTokenProvider.hashToken("old-refresh")).willReturn("old-hash");
            given(jwtTokenProvider.createToken(userId)).willReturn("new-access");
            given(jwtTokenProvider.createRefreshToken(userId)).willReturn("new-refresh");
            given(jwtTokenProvider.hashToken("new-refresh")).willReturn("new-hash");
            given(jwtTokenProvider.getRefreshTokenExpiresAt()).willReturn(LocalDateTime.now().plusDays(14));

            TokenPairDto result = userService.reissue("old-refresh");

            assertThat(result.accessToken()).isEqualTo("new-access");
            assertThat(result.refreshToken()).isEqualTo("new-refresh");
            assertThat(saved.getTokenHash()).isEqualTo("new-hash");
        }

        @Test
        @DisplayName("이미 교체된(옛날) 토큰이 오면 재사용으로 보고 저장된 토큰을 삭제한다")
        void reissue_fail_reuseDetected() {
            RefreshToken saved = savedTokenWithHash("current-hash");

            given(jwtTokenProvider.validateRefreshToken("stolen-refresh")).willReturn(true);
            given(jwtTokenProvider.getUserId("stolen-refresh")).willReturn(userId);
            given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));
            given(refreshTokenRepository.findByUser(user)).willReturn(Optional.of(saved));
            given(jwtTokenProvider.hashToken("stolen-refresh")).willReturn("stolen-hash");

            assertThatThrownBy(() -> userService.reissue("stolen-refresh"))
                    .isInstanceOf(TokenReuseDetectedException.class);

            verify(refreshTokenRepository).delete(saved);
            verify(jwtTokenProvider, never()).createToken(anyString());
        }

        @Test
        @DisplayName("refresh 타입이 아닌 토큰(access 등)은 InvalidTokenException, DB 조회도 하지 않는다")
        void reissue_fail_notRefreshToken() {
            given(jwtTokenProvider.validateRefreshToken("access-token")).willReturn(false);

            assertThatThrownBy(() -> userService.reissue("access-token"))
                    .isInstanceOf(InvalidTokenException.class);

            verify(userRepository, never()).findByUserId(anyString());
        }
    }
}
