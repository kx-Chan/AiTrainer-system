package com.aitrainer.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局统一响应载体。
 *
 * @param <T> 数据类型。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Result<T> {

    /**
     * 状态码。
     */
    private int code;

    /**
     * 消息。
     */
    private String message;

    /**
     * 数据。
     */
    private T data;

    /**
     * 成功响应。
     *
     * @param data 数据。
     * @param <T>  数据类型。
     * @return Result 对象。
     */
    public static <T> Result<T> success(final T data) {
        return Result.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .build();
    }

    /**
     * 成功响应。
     *
     * @param <T> 数据类型。
     * @return Result 对象。
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败响应。
     *
     * @param code    状态码。
     * @param message 消息。
     * @param <T>     数据类型。
     * @return Result 对象。
     */
    public static <T> Result<T> error(final int code, final String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
