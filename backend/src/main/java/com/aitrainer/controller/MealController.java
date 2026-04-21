package com.aitrainer.controller;

import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.dto.AddExtraExerciseDTO;
import com.aitrainer.dto.AddMealDTO;
import com.aitrainer.dto.AnalyzeFoodDTO;
import com.aitrainer.dto.UpdateExtraExerciseDTO;
import com.aitrainer.dto.UpdateMealDTO;
import com.aitrainer.vo.FoodAnalysisVO;
import com.aitrainer.service.MealService;
import com.aitrainer.vo.DietSummaryVO;
import com.aitrainer.vo.ExtraExerciseVO;
import com.aitrainer.vo.MealVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Meal", description = "营养膳食接口")
@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
@Slf4j
public class MealController {

    private final MealService mealService;

    /**
     * 获取指定日期的饮食汇总（含热量可视化数据）。
     *
     * @param authentication 登录信息
     * @param date           日期 (yyyy-MM-dd)，不传则默认今天
     * @return 饮食汇总 VO
     */
    @Operation(summary = "获取饮食汇总")
    @GetMapping("/summary")
    public Result<DietSummaryVO> getDietSummary(
            final Authentication authentication,
            @RequestParam(required = false) final String date) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(mealService.getDietSummary(user.getId(), date));
    }

    /**
     * 添加一条饮食记录。
     *
     * @param authentication 登录信息
     * @param dto            添加参数
     * @return 新增记录 VO
     */
    @Operation(summary = "添加饮食记录")
    @PostMapping
    public Result<MealVO> addMeal(
            final Authentication authentication,
            @Valid @RequestBody final AddMealDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(mealService.addMeal(user.getId(), dto));
    }

    /**
     * 编辑一条饮食记录。
     *
     * @param authentication 登录信息
     * @param mealId         记录 ID
     * @param dto            编辑参数
     * @return 更新后的记录 VO
     */
    @Operation(summary = "编辑饮食记录")
    @PutMapping("/{mealId}")
    public Result<MealVO> updateMeal(
            final Authentication authentication,
            @PathVariable final Long mealId,
            @Valid @RequestBody final UpdateMealDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(mealService.updateMeal(user.getId(), mealId, dto));
    }

    /**
     * 逻辑删除一条饮食记录。
     *
     * @param authentication 登录信息
     * @param mealId         记录 ID
     * @return 操作结果
     */
    @Operation(summary = "删除饮食记录")
    @DeleteMapping("/{mealId}")
    public Result<Void> deleteMeal(
            final Authentication authentication,
            @PathVariable final Long mealId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        mealService.deleteMeal(user.getId(), mealId);
        return Result.success();
    }

    /**
     * 添加一条额外运动消耗记录。
     *
     * @param authentication 登录信息
     * @param dto            添加参数
     * @return 新增记录 VO
     */
    @Operation(summary = "添加额外运动消耗")
    @PostMapping("/extra-exercise")
    public Result<ExtraExerciseVO> addExtraExercise(
            final Authentication authentication,
            @Valid @RequestBody final AddExtraExerciseDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(mealService.addExtraExercise(user.getId(), dto));
    }

    /**
     * 编辑一条额外运动消耗记录。
     *
     * @param authentication 登录信息
     * @param exerciseId     记录 ID
     * @param dto            编辑参数
     * @return 更新后的记录 VO
     */
    @Operation(summary = "编辑额外运动消耗")
    @PutMapping("/extra-exercise/{exerciseId}")
    public Result<ExtraExerciseVO> updateExtraExercise(
            final Authentication authentication,
            @PathVariable final Long exerciseId,
            @Valid @RequestBody final UpdateExtraExerciseDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(mealService.updateExtraExercise(user.getId(), exerciseId, dto));
    }

    /**
     * 逻辑删除一条额外运动消耗记录。
     *
     * @param authentication 登录信息
     * @param exerciseId     记录 ID
     * @return 操作结果
     */
    @Operation(summary = "删除额外运动消耗")
    @DeleteMapping("/extra-exercise/{exerciseId}")
    public Result<Void> deleteExtraExercise(
            final Authentication authentication,
            @PathVariable final Long exerciseId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        mealService.deleteExtraExercise(user.getId(), exerciseId);
        return Result.success();
    }

    /**
     * AI 智能分析食物营养成分（当前为固定数据模拟）。
     *
     * @param authentication 登录信息
     * @param dto            分析请求参数
     * @return 食物营养分析结果
     */
    @Operation(summary = "AI 智能分析食物营养")
    @PostMapping("/analyze")
    public Result<FoodAnalysisVO> analyzeFood(
            final Authentication authentication,
            @RequestBody final AnalyzeFoodDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(mealService.analyzeFood(user.getId(), dto));
    }
}
