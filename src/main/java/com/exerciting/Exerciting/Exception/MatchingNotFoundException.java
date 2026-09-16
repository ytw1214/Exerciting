package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MatchingNotFoundException extends BusinessException {

    public MatchingNotFoundException() {
        super(ErrorCode.MATCHING_NOT_FOUND);
    }
}