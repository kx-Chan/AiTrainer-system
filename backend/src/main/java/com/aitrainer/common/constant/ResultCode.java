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
     * 资源冲突状态码
     */
    public static final int CONFLICT = 409;
}
