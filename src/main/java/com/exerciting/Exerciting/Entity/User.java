package com.exerciting.Exerciting.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
    private String Id;

    private String pw;
    private String nickname;
    private String name;
    private String email;
    @OneToMany
    private List<Matching> matchingList;

}
