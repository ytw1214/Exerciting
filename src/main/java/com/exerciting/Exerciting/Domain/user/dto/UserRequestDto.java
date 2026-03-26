package com.exerciting.Exerciting.Domain.user.dto;

import com.exerciting.Exerciting.Domain.user.entity.User;
import jakarta.validation.constraints.Pattern;

public record UserRequestDto(
        String userId,
        @Pattern(
                regexp="^(?=.*[A-Za-z])(?=.*\\\\d)(?=.*[!@#$%^&*])[A-Za-z\\\\d!@#$%^&*]{8,}$",
                message = "패스워드는 영문자, 숫자, 특수기호를 포함하여 8자 이상이어야합니다."
        )
        String pw,
        String nickname,
        String name,
        String email
) {
    public User toEntity() {
        return User.builder()
                .userId(this.userId())
                .pw(this.pw())
                .nickname(this.nickname())
                .name(this.name())
                .email(this.email())
                .build();
    }
}