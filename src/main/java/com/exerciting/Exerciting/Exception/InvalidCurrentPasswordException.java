package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class InvalidCurrentPasswordException extends BusinessException {

    public InvalidCurrentPasswordException() {
        super(ErrorCode.INVALID_CURRENT_PASSWORD);
    }
}
