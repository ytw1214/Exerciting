package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;
import lombok.Getter;

@Getter
public class UnsupportedYearCrawlException extends RuntimeException {
    private final ErrorCode errorCode;

    public UnsupportedYearCrawlException() {
        super(ErrorCode.UNSUPPORTEDYEAR_ERROR.getMessage());
        this.errorCode = ErrorCode.UNSUPPORTEDYEAR_ERROR;
    }
}
