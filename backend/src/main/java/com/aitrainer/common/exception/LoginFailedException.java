package com.aitrainer.common.exception;

import com.aitrainer.common.constant.ResultCode;

/**
 * 登录失败异常
 */
public class LoginFailedException extends BusinessException {

    public LoginFailedException(String message) {
        super(ResultCode.UNAUTHORIZED, message);
    }
}
