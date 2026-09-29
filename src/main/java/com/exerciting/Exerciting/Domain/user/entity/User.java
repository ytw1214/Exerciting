package com.exerciting.Exerciting.Domain.user.entity;

import com.exerciting.Exerciting.Domain.user.dto.request.UserUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users")
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
    // 탈퇴 시각. null이면 활동 중인 회원
    private LocalDateTime deletedAt;

    // 기존 행에도 USER가 채워지도록 DB 기본값을 둔다 (ddl-auto=update로 컬럼이 추가될 때)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ColumnDefault("'USER'")
    private Role role = Role.USER;
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

    public boolean isWithdrawn() {
        return deletedAt != null;
    }

    /**
     * 회원 탈퇴. 행을 지우면 매칭·참가·채팅이 FK로 물려 있어 삭제가 실패하므로(기존 500의 원인)
     * 행은 남기고 개인정보만 복구할 수 없게 지운다. 로그인 아이디·이메일은 다른 사람이 다시 쓸 수 있게 비운다.
     */
    public void withdraw(LocalDateTime now) {
        this.userId = "withdrawn_" + id;
        this.email = "withdrawn_" + id + "@withdrawn.invalid";
        this.nickname = "탈퇴회원#" + id;
        this.name = null;
        this.pw = "{withdrawn}"; // BCrypt 형식이 아니라 어떤 비밀번호로도 로그인되지 않는다
        this.deletedAt = now;
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
