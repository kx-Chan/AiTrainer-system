package com.aitrainer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDTO(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确") String email,
        @NotBlank(message = "验证码不能为空")
        @Size(min = 6, max = 6, message = "验证码必须为 6 位") String code,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 50, message = "新密码长度需在 6-50 位之间") String newPassword
) {}