package com.exerciting.Exerciting.Domain.user.dto.response;

public record UserUpdateResponseDto(
        Long id
) {
    public static UserUpdateResponseDto of(Long id) {
        return new UserUpdateResponseDto(id);
    }
}
