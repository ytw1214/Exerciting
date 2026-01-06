package com.exerciting.Exerciting.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long Id;

    private String pw;
    private String nickname;
    private String name;
    private String email;
    @OneToMany
    private List<Matching> matchingList;

}
