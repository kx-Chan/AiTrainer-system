package com.aitrainer.controller;

import com.aitrainer.dto.LoginRequestDTO;
import com.aitrainer.dto.RegisterRequestDTO;
import com.aitrainer.service.UserService;
import com.aitrainer.service.VerificationService;
import com.aitrainer.vo.LoginVO;
import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 处理身份验证相关端点的控制器。
 */
@Tag(name = "认证管理", description = "用户登录与授权接口")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public final class AuthController {

    private final UserService userService;
    private final VerificationService verificationService;

    /**
     * 用户登录端点。
     *
     * @param request 登录请求。
     * @return 统一响应载体，包含 LoginVo。
     */
    @Operation(summary = "用户登录", description = "通过用户名/邮箱和密码进行身份验证，并返回访问令牌")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody final LoginRequestDTO request) {
        log.info("控制器收到登录请求：{}", request.username());
        final LoginVO loginVo = userService.login(request);
        return Result.success(loginVo);
    }

    /**
     * 发送注册验证码。
     *
     * @param email 电子邮箱。
     * @return 统一响应载体。
     */
    @Operation(summary = "发送注册验证码", description = "向指定邮箱发送 6 位注册验证码")
    @PostMapping("/code")
    public Result<String> sendCode(
            @Parameter(description = "电子邮箱", example = "user@example.com")
            @RequestParam final String email) {
        log.info("控制器收到发送验证码请求：{}", email);
        verificationService.sendCode(email);
        return Result.success(MessageConstant.VERIFY_CODE_SENT);
    }

    /**
     * 用户注册端点。
     *
     * @param request 注册请求。
     * @return 统一响应载体。
     */
    @Operation(summary = "用户注册", description = "提供电子邮箱、用户名、密码和验证码进行新用户注册")
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody final RegisterRequestDTO request) {
        log.info("控制器收到注册请求：{}, 邮箱: {}", request.username(), request.email());
        userService.register(request);
        return Result.success(MessageConstant.REGISTER_SUCCESS);
    }

    /**
     * 检查用户名是否可用。
     *
     * @param username 用户名。
     * @return 统一响应载体。
     */
    @Operation(summary = "检查用户名是否可用", description = "检查该用户名是否已被注册")
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        return Result.success(userService.checkUsernameExists(username));
    }

    /**
     * 检查邮箱是否可用。
     *
     * @param email 电子邮箱。
     * @return 统一响应载体。
     */
    @Operation(summary = "检查邮箱是否可用", description = "检查该邮箱是否已被注册")
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        return Result.success(userService.checkEmailExists(email));
    }
}
