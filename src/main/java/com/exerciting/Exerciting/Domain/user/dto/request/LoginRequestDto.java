package com.exerciting.Exerciting.Domain.user.dto.request;

public record LoginRequestDto (
        String userId,
        String password
) {
}
