package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;
import lombok.Getter;

@Getter
public class CrawlingException extends RuntimeException {
    private final ErrorCode errorCode;

    public CrawlingException() {
        super(ErrorCode.CRAWLING_ERROR.getMessage());
        this.errorCode = ErrorCode.CRAWLING_ERROR;
    }
}
