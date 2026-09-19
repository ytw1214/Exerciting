package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class InvalidCapacityException extends BusinessException {
    public InvalidCapacityException() {
        super(ErrorCode.INVALID_CAPACITY);
    }
}
