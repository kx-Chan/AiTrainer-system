package com.aitrainer.handler;

import com.aitrainer.common.constant.ResultCode;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 应用程序的全局异常处理器。
 */
@Slf4j
@RestControllerAdvice
public final class GlobalExceptionHandler {

    /**
     * 处理 BusinessException。
     *
     * @param ex 异常对象。
     * @return 统一响应载体。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(final BusinessException ex) {
        log.error("业务异常: {}", ex.getMessage(), ex);
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理其他异常。
     *
     * @param ex 异常对象。
     * @return 统一响应载体。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleGeneralException(final Exception ex) {
        log.error("发生未捕获的系统异常", ex);
        return Result.error(ResultCode.ERROR, "发生意外错误");
    }
}
