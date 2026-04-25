package com.aitrainer.controller;

import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.service.DashboardService;
import com.aitrainer.vo.AiCoachFeedbackVO;
import com.aitrainer.vo.DashboardCalorieVO;
import com.aitrainer.vo.DashboardNutritionVO;
import com.aitrainer.vo.DashboardTrainingLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据看板控制器。
 */
@Tag(name = "Dashboard", description = "数据看板接口")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取近七天卡路里消耗数据。
     *
     * @param authentication 登录信息
     * @return 近七天卡路里消耗数据
     */
    @Operation(summary = "获取近七天卡路里消耗")
    @GetMapping("/calories")
    public Result<DashboardCalorieVO> getLast7DaysCalories(final Authentication authentication) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(dashboardService.getLast7DaysCalories(user.getId()));
    }

    /**
     * 获取近七天详细训练日志。
     *
     * @param authentication 登录信息
     * @return 训练日志数据
     */
    @Operation(summary = "获取详细训练日志（支持日期范围筛选）")
    @GetMapping("/training-logs")
    public Result<DashboardTrainingLogVO> getTrainingLogs(
            final Authentication authentication,
            @RequestParam(required = false) final String startDate,
            @RequestParam(required = false) final String endDate) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(dashboardService.getTrainingLogs(user.getId(), startDate, endDate));
    }

    /**
     * 获取当日营养摄入配比。
     *
     * @param authentication 登录信息
     * @param date            日期字符串 (yyyy-MM-dd)，不传则默认今天
     * @return 营养摄入配比数据
     */
    @Operation(summary = "获取营养摄入配比")
    @GetMapping("/nutrition")
    public Result<DashboardNutritionVO> getNutritionRatio(
            final Authentication authentication,
            @RequestParam(required = false) final String date) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(dashboardService.getNutritionRatio(user.getId(), date));
    }

    /**
     * 获取 AI 私教每日碎碎念反馈。
     *
     * @param authentication 登录信息
     * @param date          日期字符串 (yyyy-MM-dd)，不传则默认今天
     * @return AI 私教反馈数据
     */
    @Operation(summary = "获取 AI 私教每日碎碎念")
    @GetMapping("/ai-coach-feedback")
    public Result<AiCoachFeedbackVO> getAiCoachFeedback(
            final Authentication authentication,
            @RequestParam(required = false) final String date) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(dashboardService.getAiCoachFeedback(user.getId(), date));
    }
}
