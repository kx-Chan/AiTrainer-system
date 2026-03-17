package com.aitrainer.service.impl;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.AccountAlreadyExistsException;
import com.aitrainer.common.exception.LoginFailedException;
import com.aitrainer.utils.JwtUtils;
import com.aitrainer.dto.LoginRequestDTO;
import com.aitrainer.mapper.UserMapper;
import com.aitrainer.entity.User;
import com.aitrainer.service.UserService;
import com.aitrainer.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * UserService 的实现类。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * 验证用户身份并返回登录视图对象。
     *
     * @param request 包含用户名和密码的登录请求。
     * @return 包含 JWT 令牌和首次登录标志的视图对象。
     */
    @Override
    @Transactional(readOnly = true)
    public LoginVO login(final LoginRequestDTO request) {
        log.info("开始处理用户登录请求: {}", request.username());

        final User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.username()));

        // 使用卫语句处理用户不存在的情况
        if (user == null) {
            log.warn("登录失败：用户 {} 不存在", request.username());
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 使用卫语句处理密码不匹配的情况
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("登录失败：用户 {} 密码错误", request.username());
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        final String token = jwtUtils.generateToken(user.getUsername());
        
        log.info("用户 {} 登录成功", request.username());
        return LoginVO.builder()
                .token(token)
                .firstLogin(user.isFirstLogin())
                .build();
    }

    /**
     * 创建新用户。
     *
     * @param username 用户名。
     * @param email    电子邮件。
     * @param password 原始密码。
     * @param isFirstLogin 是否为首次登录。
     * @return 创建的用户实体。
     */
    @Override
    @Transactional
    public User createUser(final String username, final String email, final String password, final boolean isFirstLogin) {
        log.info("正在创建新用户: {}", username);

        // 检查用户名是否已存在
        final Long countUsername = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (countUsername > 0) {
            log.warn("创建用户失败：用户名 {} 已存在", username);
            throw new AccountAlreadyExistsException(MessageConstant.USERNAME_ALREADY_EXISTS);
        }

        // 检查电子邮件是否已存在
        final Long countEmail = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        if (countEmail > 0) {
            log.warn("创建用户失败：电子邮件 {} 已存在", email);
            throw new AccountAlreadyExistsException(MessageConstant.EMAIL_ALREADY_EXISTS);
        }

        final User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .isFirstLogin(isFirstLogin)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);
        log.info("用户 {} 创建成功，ID: {}", username, user.getId());
        return user;
    }
}
