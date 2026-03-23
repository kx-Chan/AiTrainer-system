package com.aitrainer.common.exception;

import com.aitrainer.common.constant.ResultCode;
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
        this(ResultCode.ERROR, message);
    }

    /**
     * 错误的请求格式等，某些请求参数欠缺
     * @param message
     * @return
     */
    public static BusinessException badRequest(final String message) {
        return new BusinessException(ResultCode.BAD_REQUEST, message);
    }

    /**
     * 未授权
     * @param message
     * @return
     */
    public static BusinessException unauthorized(final String message) {
        return new BusinessException(ResultCode.UNAUTHORIZED, message);
    }

    /**
     * 请求被禁止
     * @param message
     * @return
     */
    public static BusinessException forbidden(final String message) {
        return new BusinessException(ResultCode.FORBIDDEN, message);
    }

    /**
     * 资源未找到
     * @param message
     * @return
     */
    public static BusinessException notFound(final String message) {
        return new BusinessException(ResultCode.NOT_FOUND, message);
    }

    /**
     * 资源冲突，与数据库资源不匹配
     * @param message
     * @return
     */
    public static BusinessException conflict(final String message) {
        return new BusinessException(ResultCode.CONFLICT, message);
    }
}
