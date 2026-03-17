package com.aitrainer.common.exception;

import com.aitrainer.common.constant.ResultCode;

/**
 * 账号已存在异常
 */
public class AccountAlreadyExistsException extends BusinessException {

    public AccountAlreadyExistsException(String message) {
        super(ResultCode.CONFLICT, message);
    }
}
