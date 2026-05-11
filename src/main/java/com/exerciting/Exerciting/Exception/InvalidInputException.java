package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidInputException extends RuntimeException {
    private final ErrorCode errorCode;

    public InvalidInputException() {
        super(ErrorCode.INVALID_INPUT.getMessage());
        this.errorCode = ErrorCode.INVALID_INPUT;
    }
}