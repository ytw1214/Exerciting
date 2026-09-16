package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class UnauthorizedUserException extends BusinessException {

    public UnauthorizedUserException() {
        super(ErrorCode.UNAUTHORIZED_USER);
    }
}