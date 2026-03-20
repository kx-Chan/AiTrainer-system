package com.aitrainer.service;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.AccountAlreadyExistsException;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.common.exception.LoginFailedException;
import com.aitrainer.utils.JwtUtils;
import com.aitrainer.dto.LoginRequestDTO;
import com.aitrainer.dto.RegisterRequestDTO;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

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

    @Mock
    private VerificationService verificationService;

    private UserService userService;

    /**
     * 设置测试环境。
     */
    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, passwordEncoder, jwtUtils, verificationService);
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

    /**
     * 测试注册成功场景。
     */
    @Test
    @DisplayName("注册成功 - 应当成功调用 Mapper 保存用户")
    void register_Success() {
        // given
        final RegisterRequestDTO request = new RegisterRequestDTO("test@example.com", "newuser", "123456", "123456");
        given(verificationService.verifyCode(anyString(), anyString())).willReturn(true);
        given(userMapper.selectCount(any())).willReturn(0L);
        given(passwordEncoder.encode(anyString())).willReturn("hashedPassword");

        // when
        userService.register(request);

        // then
        then(verificationService).should().verifyCode("test@example.com", "123456");
        then(userMapper).should(times(2)).selectCount(any(LambdaQueryWrapper.class));
        then(userMapper).should().insert(any(User.class));
        then(passwordEncoder).should().encode("123456");
        then(verificationService).should().consumeCode("test@example.com");
    }

    /**
     * 测试注册失败 - 验证码错误。
     */
    @Test
    @DisplayName("注册失败 - 验证码不匹配或已失效")
    void register_InvalidCode_ThrowsException() {
        // given
        final RegisterRequestDTO request = new RegisterRequestDTO("test@example.com", "newuser", "123456", "wrongcode");
        given(verificationService.verifyCode("test@example.com", "wrongcode")).willReturn(false);

        // when & then
        final BusinessException exception = assertThrows(BusinessException.class, () -> userService.register(request));
        assertEquals(MessageConstant.VERIFY_CODE_ERROR, exception.getMessage());
        then(verificationService).should().verifyCode("test@example.com", "wrongcode");
        then(userMapper).should(never()).selectCount(any());
        then(userMapper).should(never()).insert(any());
        then(verificationService).should(never()).consumeCode(anyString());
    }

    /**
     * 测试注册失败 - 邮箱已存在。
     */
    @Test
    @DisplayName("注册失败 - 邮箱已存在时应抛出异常")
    void register_EmailExists_ThrowsException() {
        // given
        final RegisterRequestDTO request = new RegisterRequestDTO("test@example.com", "newuser", "123456", "123456");
        given(verificationService.verifyCode(anyString(), anyString())).willReturn(true);
        given(userMapper.selectCount(any())).willReturn(0L).willReturn(1L);

        // when & then
        assertThrows(AccountAlreadyExistsException.class, () -> userService.register(request));
        then(verificationService).should().verifyCode("test@example.com", "123456");
        then(userMapper).should(times(2)).selectCount(any(LambdaQueryWrapper.class));
        then(userMapper).should(never()).insert(any());
        then(verificationService).should(never()).consumeCode(anyString());
    }
}
