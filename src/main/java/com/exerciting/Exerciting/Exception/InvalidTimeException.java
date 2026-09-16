package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class InvalidTimeException extends BusinessException {

    public InvalidTimeException() {
        super(ErrorCode.INVALID_TIME);
    }
}