package com.aitrainer.vo;

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
public final class LoginVo {

    /**
     * JWT 令牌。
     */
    private String token;

    /**
     * 是否为首次登录。
     */
    private boolean firstLogin;
}
