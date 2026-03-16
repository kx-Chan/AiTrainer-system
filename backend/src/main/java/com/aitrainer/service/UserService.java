package com.aitrainer.service;

import com.aitrainer.dto.LoginRequest;
import com.aitrainer.entity.User;
import com.aitrainer.vo.LoginVo;

/**
 * 处理用户相关操作的服务接口。
 */
public interface UserService {

    /**
     * 验证用户身份并返回登录视图对象。
     *
     * @param request 包含用户名和密码的登录请求。
     * @return 包含 JWT 令牌和首次登录标志的视图对象。
     */
    LoginVo login(final LoginRequest request);

    /**
     * 创建新用户。
     *
     * @param username 用户名。
     * @param email    电子邮件。
     * @param password 原始密码。
     * @param isFirstLogin 是否为首次登录。
     * @return 创建的用户实体。
     */
    User createUser(final String username, final String email, final String password, final boolean isFirstLogin);
}
