package com.exerciting.Exerciting.Infrastructure.security;

import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 평소 API 요청에서는 쓰지 않는다 (토큰만으로 출입증을 만들기 때문).
 * 테스트의 @WithUserDetails처럼 "아이디로 사용자를 불러와야 하는" 경우에만 사용된다.
 */
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public LoginUser loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다."));
        return LoginUser.from(user);
    }
}
