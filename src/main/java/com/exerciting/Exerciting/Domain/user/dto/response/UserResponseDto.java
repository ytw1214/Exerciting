package com.exerciting.Exerciting.Domain.user.dto.response;

import com.exerciting.Exerciting.Domain.user.entity.User;

public record UserResponseDto(
        Long id,
        String userId,
        String nickname,
        String email
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUserId(),
                user.getNickname(),
                user.getEmail()
        );
    }
}