package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求的数据传输对象。
 */
@Schema(description = "注册请求参数")
public record RegisterRequestDTO(
        @Schema(description = "电子邮箱", example = "user@example.com")
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,

        @Schema(description = "用户名", example = "newuser")
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 15, message = "用户名长度需在 3 到 15 个字符之间")
        String username,

        @Schema(description = "密码", example = "123456")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, message = "密码长度不能小于 6 位")
        String password,

        @Schema(description = "验证码", example = "123456")
        @NotBlank(message = "验证码不能为空")
        @Size(min = 6, max = 6, message = "验证码必须为 6 位数字")
        String code
) {
}
