package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class MatchingNotRecruitingException extends BusinessException {
    public MatchingNotRecruitingException() {
        super(ErrorCode.MATCHING_NOT_RECRUITING);
    }
}
