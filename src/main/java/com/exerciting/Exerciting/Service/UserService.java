package com.exerciting.Exerciting.Service;

import com.exerciting.Exerciting.Entity.User;
import com.exerciting.Exerciting.Exception.UserNotFoundException;
import com.exerciting.Exerciting.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    //데이터 조회
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(()->new UserNotFoundException("id 못찾음"));
    }

    //C
    /*
    public Long createUserById(User user) {
        if(userRepository.getUserById(getUserById(id).equals(user)) {

        }
    }

     */
}
