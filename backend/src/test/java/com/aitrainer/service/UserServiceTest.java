package com.aitrainer.service;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.LoginFailedException;
import com.aitrainer.utils.JwtUtils;
import com.aitrainer.dto.LoginRequestDTO;
import com.aitrainer.mapper.UserMapper;
import com.aitrainer.entity.User;
import com.aitrainer.service.impl.UserServiceImpl;
import com.aitrainer.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * UserService 的单元测试。
 */
@ExtendWith(MockitoExtension.class)
public final class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    private UserService userService;

    /**
     * 设置测试环境。
     */
    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, passwordEncoder, jwtUtils);
    }

    /**
     * 测试登录成功场景。
     */
    @Test
    @DisplayName("登录成功 - 应当返回有效的令牌和首次登录标志")
    void login_Success() {
        // given
        final String username = "admin";
        final String password = "password";
        final String hashedPassword = "hashedPassword";
        final String token = "mockToken";
        
        final User user = User.builder()
                .username(username)
                .passwordHash(hashedPassword)
                .isFirstLogin(false)
                .build();

        given(userMapper.selectOne(any())).willReturn(user);
        given(passwordEncoder.matches(password, hashedPassword)).willReturn(true);
        given(jwtUtils.generateToken(username)).willReturn(token);

        final LoginRequestDTO request = new LoginRequestDTO(username, password);

        // when
        final LoginVO response = userService.login(request);

        // then
        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertFalse(response.isFirstLogin());
        then(userMapper).should().selectOne(any(LambdaQueryWrapper.class));
        then(passwordEncoder).should().matches(password, hashedPassword);
        then(jwtUtils).should().generateToken(username);
    }

    /**
     * 测试用户名无效导致的登录失败。
     */
    @Test
    @DisplayName("登录失败 - 用户名不存在时应抛出登录失败异常")
    void login_InvalidUsername_ThrowsException() {
        // given
        final String username = "nonexistent";
        given(userMapper.selectOne(any())).willReturn(null);

        final LoginRequestDTO request = new LoginRequestDTO(username, "password");

        // when & then
        final LoginFailedException exception = assertThrows(LoginFailedException.class, () -> userService.login(request));
        assertEquals(401, exception.getCode());
        assertEquals(MessageConstant.LOGIN_FAILED, exception.getMessage());
        then(userMapper).should().selectOne(any(LambdaQueryWrapper.class));
        then(passwordEncoder).should(never()).matches(any(), any());
    }

    /**
     * 测试密码无效导致的登录失败。
     */
    @Test
    @DisplayName("登录失败 - 密码错误时应抛出登录失败异常")
    void login_InvalidPassword_ThrowsException() {
        // given
        final String username = "admin";
        final String password = "wrongpassword";
        final String hashedPassword = "hashedPassword";

        final User user = User.builder()
                .username(username)
                .passwordHash(hashedPassword)
                .build();

        given(userMapper.selectOne(any())).willReturn(user);
        given(passwordEncoder.matches(password, hashedPassword)).willReturn(false);

        final LoginRequestDTO request = new LoginRequestDTO(username, password);

        // when & then
        final LoginFailedException exception = assertThrows(LoginFailedException.class, () -> userService.login(request));
        assertEquals(401, exception.getCode());
        assertEquals(MessageConstant.LOGIN_FAILED, exception.getMessage());
        then(userMapper).should().selectOne(any(LambdaQueryWrapper.class));
        then(passwordEncoder).should().matches(password, hashedPassword);
        then(jwtUtils).should(never()).generateToken(any());
    }
}
