package com.exerciting.Exerciting.Domain.user.dto.request;

import com.exerciting.Exerciting.Domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserSignUpRequestDto(
        @NotBlank(message="아이디를 입력해주세요")
        @Size(min=4,max=20,message="아이디는 4~20자여야 합니다")
        String userId,

        @Pattern(
                regexp="^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$",
                message = "패스워드는 영문자, 숫자, 특수기호를 포함하여 8자 이상이어야합니다."
        )
        String pw,
        @NotBlank(message = "닉네임을 입력해주세요")
        @Size(min = 2, max = 10, message = "닉네임은 2~10자여야 합니다")
        String nickname,
        @NotBlank(message = "이름을 입력해주세요")
        String name,
        @Email(message = "이메일 형식이 올바르지 않습니다")
        @NotBlank(message = "이메일을 입력해주세요")
        String email
) {
    public User toEntity(String encodedPw) {
        return User.builder()
                .userId(this.userId())
                .pw(encodedPw)
                .nickname(this.nickname())
                .name(this.name())
                .email(this.email())
                .build();
    }   
}