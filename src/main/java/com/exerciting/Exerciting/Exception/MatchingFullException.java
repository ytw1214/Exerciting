package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class MatchingFullException extends BusinessException {
    public MatchingFullException() {
        super(ErrorCode.MATCHING_FULL);
    }
}
