package com.aitrainer.common.constant;

/**
 * Result 状态码常量类
 */
public final class ResultCode {

    private ResultCode() {}

    /**
     * 成功状态码
     */
    public static final int SUCCESS = 200;

    /**
     * 服务器内部错误状态码
     */
    public static final int ERROR = 500;

    /**
     * 未授权状态码
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 请求格式等错误
     */
    public static final int BAD_REQUEST = 400;

    /**
     * 禁止访问
     */
    public static final int FORBIDDEN = 403;

    /**
     * 资源未找到
     */
    public static final int NOT_FOUND = 404;

    /**
     * 资源冲突状态码
     */
    public static final int CONFLICT = 409;
}
