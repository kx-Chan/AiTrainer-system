package com.aitrainer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注销请求 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeactivateAccountDTO {

    /**
     * 用户密码（用于验证身份）。
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}