package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidTimeException extends RuntimeException {
    private final ErrorCode errorCode;

    public InvalidTimeException() {
        super(ErrorCode.INVALID_TIME.getMessage());
        this.errorCode = ErrorCode.INVALID_TIME;
    }
}