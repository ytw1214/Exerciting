package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(ErrorCode errorCode) {
        super(errorCode);
    }
}
