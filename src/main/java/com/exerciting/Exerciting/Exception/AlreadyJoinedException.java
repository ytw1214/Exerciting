package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class AlreadyJoinedException extends BusinessException {
    public AlreadyJoinedException() {
        super(ErrorCode.ALREADY_JOINED);    }
}
