package com.exerciting.Exerciting.Domain.user.dto;

public record TokenPairDto(
        String accessToken,
        String refreshToken
) {
}
