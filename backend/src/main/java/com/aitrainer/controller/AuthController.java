package com.aitrainer.controller;

import com.aitrainer.dto.LoginRequest;
import com.aitrainer.service.UserService;
import com.aitrainer.vo.LoginVo;
import com.aitrainer.vo.Result;
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
    @PostMapping("/login")
    public Result<LoginVo> login(@Validated @RequestBody final LoginRequest request) {
        log.info("控制器收到登录请求：{}", request.username());
        final LoginVo loginVo = userService.login(request);
        return Result.success(loginVo);
    }
}
