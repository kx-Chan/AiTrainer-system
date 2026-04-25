package com.aitrainer.controller;

import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.service.WorkoutSessionService;
import com.aitrainer.vo.LikeStatusVO;
import com.aitrainer.vo.WorkoutSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "WorkoutSession", description = "训练战报接口")
@RestController
@RequestMapping("/api/workout/sessions")
@RequiredArgsConstructor
@Slf4j
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    /**
     * 点赞战报。
     *
     * @param authentication 登录信息
     * @param sessionId       战报 ID
     * @return 点赞状态
     */
    @Operation(summary = "点赞战报")
    @PostMapping("/{sessionId}/like")
    public Result<LikeStatusVO> like(
            final Authentication authentication,
            @PathVariable final Long sessionId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(workoutSessionService.likeWorkoutSession(user.getId(), sessionId));
    }

    /**
     * 取消点赞战报。
     *
     * @param authentication 登录信息
     * @param sessionId       战报 ID
     * @return 点赞状态
     */
    @Operation(summary = "取消点赞战报")
    @DeleteMapping("/{sessionId}/like")
    public Result<LikeStatusVO> unlike(
            final Authentication authentication,
            @PathVariable final Long sessionId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(workoutSessionService.unlikeWorkoutSession(user.getId(), sessionId));
    }

    @Operation(summary = "模拟生成战报", description = "随机生成一条 AI 训练结果并入库")
    @PostMapping
    public Result<Long> createSession(
            final Authentication authentication,
            @RequestBody final Map<String, String> params) {

        final CustomUser user = (CustomUser) authentication.getPrincipal();
        final String workoutId = params.get("workoutId");

        log.info("用户 {} 请求生成项目 {} 的随机战报", user.getId(), workoutId);
        final Long sessionId = workoutSessionService.createWorkoutSession(user.getId(), workoutId);
        return Result.success(sessionId);
    }

    @Operation(summary = "获取战报详情")
    @GetMapping("/{id}")
    public Result<WorkoutSessionVO> getDetail(
            final Authentication authentication,
            @PathVariable final Long id) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(workoutSessionService.getWorkoutSessionDetail(id, user.getId()));
    }

    @Operation(summary = "获取我的历史战报")
    @GetMapping("/me")
    public Result<List<WorkoutSessionVO>> getMySessions(final Authentication authentication) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(workoutSessionService.listMyWorkoutSessions(user.getId()));
    }

    @Operation(summary = "获取用户打卡日历")
    @GetMapping("/checkin-dates")
    public Result<List<LocalDate>> getCheckInDates(
            final Authentication authentication,
            @RequestParam final int year,
            @RequestParam final int month) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(workoutSessionService.getCheckInDates(user.getId(), year, month));
    }
}

