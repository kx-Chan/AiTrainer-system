package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求的数据传输对象。
 *
 * @param username 用户名或邮箱。
 * @param password 密码。
 */
@Schema(description = "登录请求参数")
public record LoginRequestDTO(
        @Schema(description = "用户名或邮箱", example = "admin@example.com")
        @NotBlank(message = "用户名或邮箱不能为空")
        String username,

        @Schema(description = "密码", example = "123456")
        @NotBlank(message = "密码不能为空")
        String password
) {
}
