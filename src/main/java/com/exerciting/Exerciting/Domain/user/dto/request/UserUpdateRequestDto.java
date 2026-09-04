package com.exerciting.Exerciting.Domain.user.dto.request;

public record UserUpdateRequestDto(
        String pw,
        String nickname,
        String email
) {
}
