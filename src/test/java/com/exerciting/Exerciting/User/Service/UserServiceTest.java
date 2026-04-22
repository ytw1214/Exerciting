package com.exerciting.Exerciting.User.Service;
import com.exerciting.Exerciting.Domain.user.dto.UserRequestDto;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Domain.user.service.UserService;
import com.exerciting.Exerciting.Infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

// 아래 정적 임포트들이 반드시 있어야 합니다!
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUpSuccess() {
        // given
        UserRequestDto dto = new UserRequestDto("testId", "Password123!", "nick", "name", "test@test.com");

        when(userRepository.existsByUserId(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPw");

        // userService.signUp 내부에서 save를 호출하므로 그 결과를 mocking
        User user = dto.toEntity("encodedPw");
        // 실제 UserService 코드에서 save().getId()를 호출하므로 id값이 필요할 수 있음 (Reflection 등으로 주입 필요)
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        userService.signUp(dto);

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공 및 토큰 반환 테스트")
    void loginSuccess() {
        // given
        String userId = "testId";
        String rawPw = "Password123!";
        User user = User.builder().userId(userId).pw("encodedPw").build();

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPw, "encodedPw")).thenReturn(true);
        when(jwtTokenProvider.createToken(userId)).thenReturn("mock-token");

        // when
        String token = userService.login(userId, rawPw);

        // then
        assertThat(token).isEqualTo("mock-token");
    }
}