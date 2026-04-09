package com.aitrainer.controller;

import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.dto.UpdatePrivacyDTO;
import com.aitrainer.service.PrivacyService;
import com.aitrainer.vo.PrivacySettingsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 处理用户隐私偏好设置的控制器。
 */
@Tag(name = "隐私管理", description = "用户个人隐私权限、战报可见性控制接口")
@Slf4j
@RestController
@RequestMapping("/api/privacy")
@RequiredArgsConstructor
public final class PrivacyController {

    private final PrivacyService privacyService;

    @Operation(summary = "获取个人隐私设置", description = "获取当前登录用户的各项隐私开关状态")
    @GetMapping("/settings")
    public Result<PrivacySettingsVO> getSettings(final Authentication authentication) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 正在获取隐私设置", user.getId());

        final PrivacySettingsVO vo = privacyService.getUserSettings(user.getId());
        return Result.success(vo);
    }

    @Operation(summary = "更新隐私设置", description = "修改 AI 战报等内容的公开/私密状态")
    @PutMapping("/settings")
    public Result<Void> updateSettings(
            final Authentication authentication,
            @Validated @RequestBody final UpdatePrivacyDTO dto) {

        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 更新隐私设置: publicAiReport={}", user.getId(), dto.publicAiReport());

        privacyService.updateSettings(user.getId(), dto);
        return Result.success();
    }

    @Operation(summary = "查询他人战报可见性", description = "供个人空间加载时判断是否显示该用户的 AI 战报")
    @GetMapping("/user/{userId}/summary")
    public Result<Boolean> getVisibility(
            final Authentication authentication,
            @Parameter(description = "目标用户ID") @PathVariable final Long userId) {

        final CustomUser currentUser = (CustomUser) authentication.getPrincipal();
        // 逻辑：如果是看自己，永远返回 true；如果是看别人，查询对方的设置
        if (currentUser.getId().equals(userId)) {
            return Result.success(true);
        }

        final boolean canView = privacyService.checkReportVisibility(userId);
        return Result.success(canView);
    }
}
