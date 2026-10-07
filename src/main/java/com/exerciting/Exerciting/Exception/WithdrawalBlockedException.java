package com.exerciting.Exerciting.Exception;

import com.exerciting.Exerciting.Infrastructure.exception.ErrorCode;

public class WithdrawalBlockedException extends BusinessException {

    public WithdrawalBlockedException() {
        super(ErrorCode.WITHDRAWAL_BLOCKED);
    }
}
