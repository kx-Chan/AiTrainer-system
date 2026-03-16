package com.aitrainer.controller;

import com.aitrainer.dto.LoginRequestDTO;
import com.aitrainer.service.UserService;
import com.aitrainer.vo.LoginVO;
import com.aitrainer.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 用户登录端点。
     *
     * @param request 登录请求。
     * @return 统一响应载体，包含 LoginVo。
     */
    @Operation(summary = "用户登录", description = "通过用户名和密码进行身份验证，并返回访问令牌")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody final LoginRequestDTO request) {
        log.info("控制器收到登录请求：{}", request.username());
        final LoginVO loginVo = userService.login(request);
        return Result.success(loginVo);
    }
}
