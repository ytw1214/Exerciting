package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}