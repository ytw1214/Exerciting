package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;
import lombok.Getter;

@Getter
public class DisMatchedSizeException extends RuntimeException {
    private final ErrorCode errorCode;

    public DisMatchedSizeException() {
        super(ErrorCode.DISMATCHED_SIZE.getMessage());
        this.errorCode = ErrorCode.DISMATCHED_SIZE;
    }
}