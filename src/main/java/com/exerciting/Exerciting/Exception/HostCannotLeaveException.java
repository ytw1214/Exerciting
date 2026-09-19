package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class HostCannotLeaveException extends BusinessException {
    public HostCannotLeaveException() {
        super(ErrorCode.ALREADY_JOINED);
    }
}
