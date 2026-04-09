package com.exerciting.Exerciting.Domain.user.dto;

public record LoginRequestDto (
        String userId,
        String password
) {
}
