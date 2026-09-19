package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class InvalidStateTransitionException extends BusinessException {
    public InvalidStateTransitionException() {
        super(ErrorCode.INVALID_STATE_TRANSITION);
    }
}
