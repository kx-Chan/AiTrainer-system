package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功的视图展示对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录成功返回结果")
public final class LoginVo {

    /**
     * JWT 令牌。
     */
    @Schema(description = "JWT访问令牌")
    private String token;

    /**
     * 是否为首次登录。
     */
    @Schema(description = "是否为首次登录")
    private boolean firstLogin;
}
