package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;
import lombok.Getter;

@Getter
public class UnauthorizedUserException extends RuntimeException {
    private final ErrorCode errorCode;

    public UnauthorizedUserException() {
        super(ErrorCode.UNAUTHORIZED_USER.getMessage());
        this.errorCode = ErrorCode.UNAUTHORIZED_USER;
    }
}