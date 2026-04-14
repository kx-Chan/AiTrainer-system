package com.aitrainer.controller;

import com.aitrainer.common.constant.ResultCode;
import com.aitrainer.common.result.Result;
import com.aitrainer.service.WorkoutService;
import com.aitrainer.vo.WorkoutVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 处理健身项目（Workouts）相关的控制器。
 */
@Tag(name = "健身项目管理", description = "获取可用的训练项目、难度分级及项目详情")
@Slf4j
@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public final class WorkoutController {

    private final WorkoutService workoutService;

    @Operation(summary = "获取健身项目列表", description = "获取所有未被逻辑删除的训练项目，按难度从易到难排序")
    @GetMapping
    public Result<List<WorkoutVO>> listWorkouts() {
        log.info("正在查询全量健身项目列表");

        final List<WorkoutVO> list = workoutService.listWorkouts();
        return Result.success(list);
    }

    @Operation(summary = "获取项目详情", description = "根据项目唯一 ID（如: squat, lunge）获取详细的训练描述和标签信息")
    @GetMapping("/{id}")
    public Result<WorkoutVO> getWorkoutById(
            @Parameter(description = "项目唯一标识 (ID)") @PathVariable final String id) {
        log.info("正在查询健身项目详情, ID: {}", id);

        final WorkoutVO vo = workoutService.getWorkoutById(id);

        // 如果项目不存在，由业务层或通用逻辑处理，这里保持 Controller 简洁
        if (vo == null) {
            log.warn("健身项目查询失败, 目标 ID [{}] 不存在", id);
            return Result.error(ResultCode.ERROR,"目标健身项目不存在");
        }

        return Result.success(vo);
    }
}