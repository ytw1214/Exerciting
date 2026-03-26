package com.exerciting.Exerciting.Domain.user.entity;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.user.dto.UserUpdateDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String pw;
    private String nickname;
    private String name;
    private String email;
    /*
    @OneToMany(mappedBy="user")
    private Matching matching;


     */
    @Builder
    public User(String userId, String pw, String nickname, String name, String email) {
        this.userId = userId;
        this.pw = pw;
        this.nickname = nickname;
        this.name = name;
        this.email = email;
    }

    public void update(UserUpdateDto dto) {
        this.pw = dto.pw();
        this.nickname = dto.nickname();
        this.email = dto.email();
    }
}
