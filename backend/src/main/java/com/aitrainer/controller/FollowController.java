package com.aitrainer.controller;

import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.service.FollowService;
import com.aitrainer.vo.FollowUserVO;
import com.aitrainer.vo.PageResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "关注与粉丝", description = "关注/取关与粉丝列表接口")
@Validated
@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "分页获取关注列表")
    @GetMapping("/following")
    public Result<PageResultVO<FollowUserVO>> getFollowing(
            final Authentication authentication,
            @RequestParam(defaultValue = "1") final long page,
            @RequestParam(defaultValue = "10") final long size) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(followService.getFollowing(user.getId(), page, size));
    }

    @Operation(summary = "分页获取粉丝列表")
    @GetMapping("/followers")
    public Result<PageResultVO<FollowUserVO>> getFollowers(
            final Authentication authentication,
            @RequestParam(defaultValue = "1") final long page,
            @RequestParam(defaultValue = "10") final long size) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(followService.getFollowers(user.getId(), page, size));
    }

    @Operation(summary = "关注用户")
    @PostMapping("/{targetUserId}")
    public Result<Void> follow(final Authentication authentication, @PathVariable final Long targetUserId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        followService.follow(user.getId(), targetUserId);
        return Result.success();
    }

    @Operation(summary = "取消关注用户")
    @DeleteMapping("/{targetUserId}")
    public Result<Void> unfollow(final Authentication authentication, @PathVariable final Long targetUserId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        followService.unfollow(user.getId(), targetUserId);
        return Result.success();
    }
}

