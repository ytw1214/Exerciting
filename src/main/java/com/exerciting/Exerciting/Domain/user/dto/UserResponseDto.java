package com.exerciting.Exerciting.Domain.user.dto;

public record UserResponseDto(
        Long id,
        String userId,
        String nickname,
        String email
) {}