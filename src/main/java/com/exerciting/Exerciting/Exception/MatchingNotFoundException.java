package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MatchingNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public MatchingNotFoundException() {
        super(ErrorCode.MATCHING_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.MATCHING_NOT_FOUND;
    }
}