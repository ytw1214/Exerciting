package com.exerciting.Exerciting.Domain.user.dto.response;

import com.exerciting.Exerciting.Domain.user.entity.User;

public record UserSignUpResponseDto(
        Long id,
        String userId,
        String nickname
) {
    public static UserSignUpResponseDto of(User user) {
        return new UserSignUpResponseDto(
                user.getId(),
                user.getUserId(),
                user.getNickname()
        );
    }
}
