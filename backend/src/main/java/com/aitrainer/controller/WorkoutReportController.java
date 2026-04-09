package com.aitrainer.controller;

import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.service.WorkoutSessionService;
import com.aitrainer.vo.LikeStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "WorkoutReport", description = "训练战报接口")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class WorkoutReportController {

    private final WorkoutSessionService workoutSessionService;

    /**
     * 点赞战报。
     *
     * @param authentication 登录信息
     * @param reportId       战报 ID
     * @return 点赞状态
     */
    @Operation(summary = "点赞战报")
    @PostMapping("/{reportId}/like")
    public Result<LikeStatusVO> like(
            final Authentication authentication,
            @PathVariable final Long reportId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(workoutSessionService.likeReport(user.getId(), reportId));
    }

    /**
     * 取消点赞战报。
     *
     * @param authentication 登录信息
     * @param reportId       战报 ID
     * @return 点赞状态
     */
    @Operation(summary = "取消点赞战报")
    @DeleteMapping("/{reportId}/like")
    public Result<LikeStatusVO> unlike(
            final Authentication authentication,
            @PathVariable final Long reportId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(workoutSessionService.unlikeReport(user.getId(), reportId));
    }
}

