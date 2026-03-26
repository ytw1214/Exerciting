package com.exerciting.Exerciting.Domain.user.dto;

public record UserUpdateDto(
        String pw,
        String nickname,
        String email
) {
}
