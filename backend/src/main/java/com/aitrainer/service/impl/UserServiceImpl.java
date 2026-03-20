package com.aitrainer.service.impl;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.AccountAlreadyExistsException;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.common.exception.LoginFailedException;
import com.aitrainer.dto.RegisterRequestDTO;
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
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 使用卫语句处理密码不匹配的情况
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("登录失败：账号 {} 密码错误", request.username());
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
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
            throw new AccountAlreadyExistsException(MessageConstant.USERNAME_ALREADY_EXISTS);
        }

        // 3. 校验邮箱是否已存在
        if (checkEmailExists(request.email())) {
            throw new AccountAlreadyExistsException(MessageConstant.EMAIL_ALREADY_EXISTS);
        }

        // 4. 创建用户
        final User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .isFirstLogin(true) // 新注册用户默认为首次登录
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);
        log.info("用户 {} 注册成功，ID: {}", request.username(), user.getId());

        // 注册成功后，消耗验证码
        verificationService.consumeCode(request.email());
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }

    @Override
    public boolean checkEmailExists(String email) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0;
    }
}
