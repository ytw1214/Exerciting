package com.exerciting.Exerciting.Domain.user.dto;

public record TokenReissuePairDto(
        String accessToken,
        String refreshToken
) {
}
