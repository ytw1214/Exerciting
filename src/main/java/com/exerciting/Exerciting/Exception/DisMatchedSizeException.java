package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class DisMatchedSizeException extends BusinessException {

    public DisMatchedSizeException() {
        super(ErrorCode.DISMATCHED_SIZE);
    }
}