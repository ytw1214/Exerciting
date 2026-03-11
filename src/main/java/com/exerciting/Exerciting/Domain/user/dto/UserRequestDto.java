package com.exerciting.Exerciting.Domain.user.dto;

public record UserRequestDto(
        String userId,
        String pw,
        String nickname,
        String name,
        String email
) {}