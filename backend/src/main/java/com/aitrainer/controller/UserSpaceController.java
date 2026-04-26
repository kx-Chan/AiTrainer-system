package com.aitrainer.controller;

import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.service.FollowService;
import com.aitrainer.service.PrivacyService;
import com.aitrainer.service.UserDynamicsService;
import com.aitrainer.service.UserService;
import com.aitrainer.service.UserSpaceService;
import com.aitrainer.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户空间与社交控制器
 */
@Tag(name = "用户空间", description = "用户个人空间资料展示、他人空间访问及关注功能")
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public final class UserSpaceController {

    private final UserSpaceService userSpaceService;
    private final FollowService followService;
    private final UserDynamicsService userDynamicsService;
    private final PrivacyService  privacyService;
    private final UserService userService;

    /**
     * 检查目标用户是否已注销，如果已注销则抛出异常。
     *
     * @param userId 用户 ID
     */
    private void checkUserNotDeactivated(final Long userId) {
        if (userService.isDeactivated(userId)) {
            throw BusinessException.badRequest(MessageConstant.USER_DEACTIVATED);
        }
    }

    @Operation(summary = "获取用户空间资料", description = "访问他人或自己的空间主页资料，包含关注状态判断")
    @GetMapping("/{userId}/profile")
    public Result<UserSpaceVO> getUserProfile(
            final Authentication authentication,
            @Parameter(description = "目标用户ID") @PathVariable final Long userId) {

        final CustomUser currentUser = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 正在访问用户 {} 的空间", currentUser.getId(), userId);

        // 检查目标用户是否已注销，如果已注销则返回特殊标记
        final boolean isDeactivated = userService.isDeactivated(userId);
        
        final UserSpaceVO vo = userSpaceService.getSpaceProfile(currentUser.getId(), userId);
        
        // 如果目标用户已注销，设置特殊标记让前端显示空白状态
        if (isDeactivated) {
            vo.setDeactivated(true);
        }
        
        return Result.success(vo);
    }

    @Operation(summary = "获取他人的公开收藏夹", description = "查询指定用户的收藏夹列表，后端强制过滤仅返回公开状态的文件夹")
    @GetMapping("/{userId}/collection/folders")
    public Result<List<FolderVO>> getUserPublicFolders(
            @Parameter(description = "目标用户ID") @PathVariable final Long userId) {

        log.info("查询用户 {} 的公开收藏夹列表", userId);
        final List<FolderVO> list = userSpaceService.listPublicFolders(userId);
        return Result.success(list);
    }

    @Operation(summary = "关注用户", description = "建立关注关系")
    @PostMapping("/follow/{userId}")
    public Result<Void> follow(
            final Authentication authentication,
            @Parameter(description = "要关注的目标用户ID") @PathVariable final Long userId) {

        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 请求关注用户 {}", user.getId(), userId);

        followService.follow(user.getId(), userId);
        return Result.success();
    }

    @Operation(summary = "取消关注", description = "解除关注关系")
    @DeleteMapping("/follow/{userId}")
    public Result<Void> unfollow(
            final Authentication authentication,
            @Parameter(description = "要取消关注的目标用户ID") @PathVariable final Long userId) {

        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 请求取消关注用户 {}", user.getId(), userId);

        followService.unfollow(user.getId(), userId);
        return Result.success();
    }

    @Operation(summary = "获取用户动态列表", description = "聚合查询推文与 AI 战报，支持分类过滤与分页")
    @GetMapping("/{userId}/dynamics")
    public Result<PageResultVO<DynamicVO>> listUserDynamics(
            final Authentication authentication,
            @Parameter(description = "目标用户ID") @PathVariable final Long userId,
            @Parameter(description = "分类: all(全部), post(推文), workout_report(战报)")
            @RequestParam(defaultValue = "all") final String category,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") final long page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") final long size) {

        final CustomUser currentUser = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 正在请求用户 {} 的动态列表, 分类: {}", currentUser.getId(), userId, category);

        final PageResultVO<DynamicVO> result = userDynamicsService.listUserDynamics(currentUser.getId(), userId, category, page, size);
        return Result.success(result);
    }

    @Operation(summary = "获取用户隐私设置", description = "供空间主页判断是否展示战报等敏感内容")
    @GetMapping("/{userId}/privacy")
    public Result<PrivacySettingsVO> getUserPrivacy(
            @Parameter(description = "目标用户ID") @PathVariable final Long userId) {
        log.info("查询用户 {} 的隐私设置", userId);
        // 这里的隐私服务你之前应该已经写过类似的了
        return Result.success(privacyService.getUserSettings(userId));
    }
}