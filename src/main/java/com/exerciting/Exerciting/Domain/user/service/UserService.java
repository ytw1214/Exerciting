package com.exerciting.Exerciting.Domain.user.service;

import com.exerciting.Exerciting.Domain.user.dto.UserRequestDto;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Exception.UserNotFoundException;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    //데이터 조회
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()->new UserNotFoundException("id 못찾음"));
    }
    @Transactional
    public Long join(UserRequestDto dto) {
        User user = User.builder()
        .userId(dto.userId())
        .pw(dto.pw())
        .nickname(dto.nickname())
        .name(dto.name())
        .email(dto.email())
        .build();

        return userRepository.save(user).getId();
    }
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
        return userRepository.save(dto.toEntity()).getId();
    }

}
