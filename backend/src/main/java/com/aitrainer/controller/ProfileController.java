package com.aitrainer.controller;

import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.dto.OnboardingProfileDTO;
import com.aitrainer.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户资料控制器。
 */
@Tag(name = "用户资料管理", description = "处理用户个人资料的接口")
@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 提交首次引导页资料。
     *
     * @param authentication 当前登录用户的认证信息。
     * @param dto            用户资料数据。
     * @return 统一响应载体。
     */
    @Operation(summary = "提交首次引导页资料", description = "保存用户初始资料并更新首次登录状态")
    @PostMapping("/onboarding")
    public Result<Void> submitOnboardingProfile(
            Authentication authentication,
            @Validated @RequestBody OnboardingProfileDTO dto) {
        
        CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("收到用户 {} (ID: {}) 的首次引导页资料提交请求", user.getUsername(), user.getId());
        
        profileService.saveOnboardingProfile(user.getId(), dto);
        
        return Result.success();
    }
}
