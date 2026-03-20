package com.aitrainer.service;

import com.aitrainer.dto.LoginRequestDTO;
import com.aitrainer.dto.RegisterRequestDTO;
import com.aitrainer.vo.LoginVO;

/**
 * 处理用户相关操作的服务接口。
 */
public interface UserService {

    /**
     * 验证用户身份并返回登录视图对象。
     *
     * @param request 包含用户名或邮箱和密码的登录请求。
     * @return 包含 JWT 令牌和首次登录标志的视图对象。
     */
    LoginVO login(final LoginRequestDTO request);

    /**
     * 用户注册。
     *
     * @param request 包含用户名、邮箱和密码的注册请求。
     */
    void register(final RegisterRequestDTO request);

    /**
     * 检查用户名是否已存在。
     *
     * @param username 用户名。
     * @return 如果存在返回 true。
     */
    boolean checkUsernameExists(String username);

    /**
     * 检查邮箱是否已存在。
     *
     * @param email 邮箱。
     * @return 如果存在返回 true。
     */
    boolean checkEmailExists(String email);
}
