package com.exerciting.Exerciting.Domain.user.service;

import com.exerciting.Exerciting.Domain.user.dto.UserRequestDto;
import com.exerciting.Exerciting.Domain.user.dto.UserUpdateDto;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Exception.UserNotFoundException;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
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
    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));

        userRepository.delete(user);
    }
    @Transactional
    public void updateUserDetail(String userId, UserUpdateDto dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));
        user.update(dto);
    }

}
