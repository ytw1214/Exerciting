package com.exerciting.Exerciting.User.Service;

import com.exerciting.Exerciting.Domain.user.dto.request.UserSignUpRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.response.TokenResponseDto;
import com.exerciting.Exerciting.Domain.user.entity.RefreshToken;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.RefreshTokenRepository;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import com.exerciting.Exerciting.Exception.DuplicateResourceException;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Exception.UserNotFoundException;
import com.exerciting.Exerciting.Infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        @DisplayName("정상 로그인 시 accessToken을 반환하고 refreshToken을 저장한다")
        void login_success() {
            String userId = "testId";
            String rawPw = "Password123!";
            User user = User.builder().userId(userId).pw("encodedPw").build();

            given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(rawPw, "encodedPw")).willReturn(true);
            given(jwtTokenProvider.createToken(userId)).willReturn("mock-access-token");
            given(jwtTokenProvider.createRefreshToken(userId)).willReturn("mock-refresh-token");
            given(jwtTokenProvider.getRefreshTokenExpiresAt())
                    .willReturn(LocalDateTime.now().plusDays(14));
            given(refreshTokenRepository.findByUser(user)).willReturn(Optional.empty());

            TokenResponseDto result = userService.login(userId, rawPw);

            assertThat(result.accessToken()).isEqualTo("mock-access-token");
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("존재하지 않는 userId이면 UserNotFoundException 발생")
        void login_fail_userNotFound() {
            given(userRepository.findByUserId("noSuchId")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login("noSuchId", "anyPw"))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("비밀번호가 틀리면 InvalidInputException 발생, 토큰을 만들지 않는다")
        void login_fail_wrongPassword() {
            String userId = "testId";
            User user = User.builder().userId(userId).pw("encodedPw").build();

            given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongPw", "encodedPw")).willReturn(false);

            assertThatThrownBy(() -> userService.login(userId, "wrongPw"))
                    .isInstanceOf(InvalidInputException.class);

            verify(jwtTokenProvider, never()).createToken(anyString());
        }
    }
}