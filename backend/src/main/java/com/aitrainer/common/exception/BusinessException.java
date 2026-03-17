package com.aitrainer.common.exception;

import lombok.Getter;

/**
 * 业务异常基类。
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码。
     */
    private final int code;

    /**
     * BusinessException 构造函数。
     *
     * @param code    错误码。
     * @param message 错误消息。
     */
    public BusinessException(final int code, final String message) {
        super(message);
        this.code = code;
    }

    /**
     * BusinessException 构造函数。
     *
     * @param message 错误消息。
     */
    public BusinessException(final String message) {
        this(500, message);
    }
}
