package com.aitrainer.service.impl;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.dto.RegisterRequestDTO;
import com.aitrainer.service.CollectionFolderService;
import com.aitrainer.utils.JwtUtils;
import com.aitrainer.dto.LoginRequestDTO;
import com.aitrainer.mapper.UserMapper;
import com.aitrainer.entity.User;
import com.aitrainer.service.UserService;
import com.aitrainer.service.VerificationService;
import com.aitrainer.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * UserServiceImpl 的实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final VerificationService verificationService;
    private final CollectionFolderService collectionFolderService;

    /**
     * 验证用户身份并返回登录视图对象。
     *
     * @param request 包含用户名或邮箱和密码的登录请求。
     * @return 包含 JWT 令牌和首次登录标志的视图对象。
     */
    @Override
    @Transactional(readOnly = true)
    public LoginVO login(final LoginRequestDTO request) {
        log.info("开始处理用户登录请求: {}", request.username());

        // 同时支持用户名或邮箱登录
        final User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.username())
                .or()
                .eq(User::getEmail, request.username()));

        // 使用卫语句处理用户不存在的情况
        if (user == null) {
            log.warn("登录失败：账号 {} 不存在", request.username());
            throw BusinessException.conflict(MessageConstant.LOGIN_FAILED);
        }

        // 使用卫语句处理密码不匹配的情况
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("登录失败：账号 {} 密码错误", request.username());
            throw BusinessException.conflict(MessageConstant.LOGIN_FAILED);
        }

        final String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        
        log.info("用户 {} 登录成功", user.getUsername());
        return LoginVO.builder()
                .token(token)
                .firstLogin(user.isFirstLogin())
                .build();
    }

    /**
     * 用户注册。
     *
     * @param request 包含用户名、邮箱和密码的注册请求。
     */
    @Override
    @Transactional
    public void register(final RegisterRequestDTO request) {
        log.info("开始处理用户注册请求: {}, 邮箱: {}", request.username(), request.email());

        // 1. 校验验证码
        if (!verificationService.verifyCode(request.email(), request.code())) {
            throw new BusinessException(MessageConstant.VERIFY_CODE_ERROR);
        }

        // 2. 校验用户名是否已存在
        if (checkUsernameExists(request.username())) {
            throw BusinessException.conflict(MessageConstant.USERNAME_ALREADY_EXISTS);
        }

        // 3. 校验邮箱是否已存在
        if (checkEmailExists(request.email())) {
            throw BusinessException.conflict(MessageConstant.EMAIL_ALREADY_EXISTS);
        }

        // 4. 创建用户
        final User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .isFirstLogin(true) // 新注册用户默认为首次登录
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .followingCount(0) // 关注数量初始化为0
                .followerCount(0) // 粉丝数量初始化为0
                .build();
        userMapper.insert(user);

        // 5. 创建默认收藏夹
        collectionFolderService.initDefaultFolder(user.getId());

        log.info("用户 {} 注册成功，ID: {}", request.username(), user.getId());

        // 注册成功后，消耗验证码
        verificationService.consumeCode(request.email());
    }

    /**
     * 检查用户名是否存在
     * @param username 用户名。
     * @return
     */
    @Override
    public boolean checkUsernameExists(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }

    /**
     * 检查邮箱是否已注册
     * @param email 邮箱。
     * @return
     */
    @Override
    public boolean checkEmailExists(String email) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0;
    }

    /**
     * 修改密码。
     * @param userId 用户 ID。
     * @param oldPassword 旧密码。
     * @param newPassword 新密码。
     */
    @Override
    @Transactional
    public void changePassword(final Long userId, final String oldPassword, final String newPassword) {
        if (userId == null) {
            throw BusinessException.unauthorized(MessageConstant.USER_NOT_LOGGED_IN);
        }
        final User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound(MessageConstant.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw BusinessException.badRequest(MessageConstant.PASSWORD_INCORRECT);
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw BusinessException.badRequest(MessageConstant.PASSWORD_SAME_AS_OLD);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 重置密码（邮箱验证码）。
     * @param email 邮箱。
     * @param code 验证码。
     * @param newPassword 新密码。
     */
    @Override
    @Transactional
    public void resetPassword(final String email, final String code, final String newPassword) {
        if (!verificationService.verifyCode(email, code)) {
            throw BusinessException.badRequest(MessageConstant.VERIFY_CODE_ERROR);
        }

        final User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw BusinessException.notFound(MessageConstant.EMAIL_NOT_REGISTERED);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        verificationService.consumeCode(email);
    }

    /**
     * 根据 ID 获取用户。
     * @param userId 用户 ID。
     * @return 用户或 null。
     */
    @Override
    @Transactional(readOnly = true)
    public User getById(final Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 批量根据 ID 获取用户。
     * @param ids ID 列表。
     * @return 用户列表。
     */
    @Override
    @Transactional(readOnly = true)
    public java.util.List<User> listByIds(final java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) return java.util.List.of();
        return userMapper.selectBatchIds(ids);
    }

    /**
     * 关注数 +1。
     */
    @Override
    @Transactional
    public void increaseFollowingCount(final Long userId) {
        final User u = userMapper.selectById(userId);
        if (u == null) throw BusinessException.notFound(MessageConstant.USER_NOT_FOUND);
        final int v = (u.getFollowingCount() == null ? 0 : u.getFollowingCount()) + 1;
        u.setFollowingCount(v);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }

    /**
     * 关注数 -1。
     */
    @Override
    @Transactional
    public void decreaseFollowingCount(final Long userId) {
        final User u = userMapper.selectById(userId);
        if (u == null) throw BusinessException.notFound(MessageConstant.USER_NOT_FOUND);
        final int v = Math.max(0, (u.getFollowingCount() == null ? 0 : u.getFollowingCount()) - 1);
        u.setFollowingCount(v);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }

    /**
     * 粉丝数 +1。
     */
    @Override
    @Transactional
    public void increaseFollowerCount(final Long userId) {
        final User u = userMapper.selectById(userId);
        if (u == null) throw BusinessException.notFound(MessageConstant.USER_NOT_FOUND);
        final int v = (u.getFollowerCount() == null ? 0 : u.getFollowerCount()) + 1;
        u.setFollowerCount(v);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }

    /**
     * 粉丝数 -1。
     */
    @Override
    @Transactional
    public void decreaseFollowerCount(final Long userId) {
        final User u = userMapper.selectById(userId);
        if (u == null) throw BusinessException.notFound(MessageConstant.USER_NOT_FOUND);
        final int v = Math.max(0, (u.getFollowerCount() == null ? 0 : u.getFollowerCount()) - 1);
        u.setFollowerCount(v);
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
    }

    /**
     * 根据userId更新user信息
     * @param user
     */
    @Override
    public void updateById(User user) {
        userMapper.updateById(user);
    }
}
