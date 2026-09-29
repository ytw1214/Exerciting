package com.exerciting.Exerciting.Domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 바꿀 값만 보낸다(null이면 그대로).
 * 비밀번호나 이메일을 바꿀 때는 currentPw가 반드시 필요하다.
 */
public record UserUpdateRequestDto(
        String currentPw,
        @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
        String pw,
        @Size(min = 2, max = 10, message = "닉네임은 2~10자여야 합니다")
        String nickname,
        @Email(message = "이메일 형식이 올바르지 않습니다")
        String email
) {
}
