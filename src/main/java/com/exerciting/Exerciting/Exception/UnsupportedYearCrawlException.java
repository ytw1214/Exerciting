package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;
import lombok.Getter;

@Getter
public class UnsupportedYearCrawlException extends BusinessException {
    public UnsupportedYearCrawlException() {
        super(ErrorCode.UNSUPPORTEDYEAR_ERROR);
    }
}
