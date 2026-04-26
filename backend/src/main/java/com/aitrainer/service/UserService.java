package com.aitrainer.service;

import com.aitrainer.dto.LoginRequestDTO;
import com.aitrainer.dto.RegisterRequestDTO;
import com.aitrainer.entity.User;
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

    /**
     * 修改密码。
     *
     * @param userId      当前登录用户 ID。
     * @param oldPassword 旧密码明文。
     * @param newPassword 新密码明文。
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码（邮箱验证码方式）。
     *
     * @param email       邮箱。
     * @param code        6 位验证码。
     * @param newPassword 新密码明文。
     */
    void resetPassword(String email, String code, String newPassword);

    /**
     * 根据 ID 获取用户。
     *
     * @param userId 用户 ID。
     * @return 用户实体，找不到返回 null。
     */
    User getById(Long userId);

    /**
     * 批量根据 ID 获取用户。
     *
     * @param ids 用户 ID 列表。
     * @return 用户实体列表。
     */
    java.util.List<User> listByIds(java.util.List<Long> ids);

    /**
     * 关注数 +1。
     *
     * @param userId 用户 ID。
     */
    void increaseFollowingCount(Long userId);

    /**
     * 关注数 -1（不小于 0）。
     *
     * @param userId 用户 ID。
     */
    void decreaseFollowingCount(Long userId);

    /**
     * 粉丝数 +1。
     *
     * @param userId 用户 ID。
     */
    void increaseFollowerCount(Long userId);

    /**
     * 粉丝数 -1（不小于 0）。
     *
     * @param userId 用户 ID。
     */
    void decreaseFollowerCount(Long userId);

    /**
     * 根据userId更新user信息
     * @param user
     */
    void updateById(User user);

    /**
     * 注销用户账户（敏感数据脱敏 + Token失效）
     *
     * @param userId      用户 ID。
     * @param password    密码（用于验证身份）。
     * @return 注销是否成功。
     */
    void deactivateAccount(Long userId, String password);

    /**
     * 检查用户是否已注销
     *
     * @param userId 用户 ID。
     * @return 已注销返回 true。
     */
    boolean isDeactivated(Long userId);
}
