package com.aitrainer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(
        @NotBlank(message = "当前密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 50, message = "新密码长度需在 6-50 位之间") String newPassword
) {}
