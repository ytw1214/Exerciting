package com.exerciting.Exerciting.Domain.user.dto.response;

public record UserDeleteResponseDto(
        Long id
) {
    public static UserDeleteResponseDto of(Long id) {
        return new UserDeleteResponseDto(id);
    }
}
