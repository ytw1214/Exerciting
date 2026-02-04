package com.exerciting.Exerciting.Domain.User.entity;

import com.exerciting.Exerciting.Domain.matching.entity.Matching;
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

    private String username;
    private String pw;
    private String nickname;
    private String name;
    private String email;
    @OneToMany(mappedBy="user")
    private List<Matching> matchingList = new ArrayList<>();

    @Builder
    public User(String username, String pw, String nickname, String name, String email, List<Matching> matchingList) {
        this.username = username;
        this.pw = pw;
        this.nickname = nickname;
        this.name = name;
        this.email = email;
        this.matchingList = matchingList;
    }
}
