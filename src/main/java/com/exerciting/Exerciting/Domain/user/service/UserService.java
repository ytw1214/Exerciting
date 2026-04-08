package com.exerciting.Exerciting.Domain.user.service;

import com.exerciting.Exerciting.Domain.user.dto.UserRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.UserUpdateDto;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Exception.UserNotFoundException;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    //데이터 조회
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()->new UserNotFoundException("해당하는 아이디를 찾을 수 없습니다."));
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
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));

        log.info("{} 회원 탈퇴 완료",user.getUserId());
        userRepository.delete(user);
        return user.getId();
    }
    @Transactional
    public void updateUserDetail(String userId, UserUpdateDto dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));
        String encodedPw = null;
        if (dto.pw() != null && !dto.pw().isEmpty()) {
            encodedPw = passwordEncoder.encode(dto.pw());
        }
        user.update(dto, encodedPw);
    }

    public String login(String userId, String pw) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));

        if(!passwordEncoder.matches(pw,user.getPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return jwtTokenProvider.createToken(user.getUserId());
    }
}
