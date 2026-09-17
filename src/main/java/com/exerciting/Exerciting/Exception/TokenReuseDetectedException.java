package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class TokenReuseDetectedException extends BusinessException {
    public TokenReuseDetectedException() {
        super(ErrorCode.TOKEN_REUSE_DETECTED);
    }
}
