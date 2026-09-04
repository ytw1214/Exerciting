package com.exerciting.Exerciting.Domain.user.entity;

import com.exerciting.Exerciting.Domain.user.dto.request.UserUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    private String userId;
    private String pw;
    @Column(unique=true)
    private String nickname;
    private String name;
    @Column(unique=true)
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

    public void update(UserUpdateRequestDto dto, String encodedPw) {
        if(encodedPw != null) {
            this.pw =encodedPw;
        }
        if (dto.nickname() != null && !dto.nickname().isEmpty()) {
            this.nickname = dto.nickname();
        }
        if (dto.email() != null && !dto.email().isEmpty()) {
            this.email = dto.email();
        }
    }
}
