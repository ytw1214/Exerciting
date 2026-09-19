package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class NotParticipantException extends BusinessException {
    public NotParticipantException() {
        super(ErrorCode.NOT_PARTICIPANT);
    }
}
