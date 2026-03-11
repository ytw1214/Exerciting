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
    //C
    /*
    public Long createUserById(User user) {
        if(userRepository.getUserById(getUserById(id).equals(user)) {

        }
    }

     */
}
