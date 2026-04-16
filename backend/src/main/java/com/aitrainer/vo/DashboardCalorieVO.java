package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据看板 - 近七天卡路里消耗 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "近七天卡路里消耗数据")
public class DashboardCalorieVO {

    @Schema(description = "日期列表 (yyyy-MM-dd)")
    private List<String> dates;

    @Schema(description = "项目训练每日消耗热量列表 (kcal)")
    private List<Integer> workoutCalories;

    @Schema(description = "额外运动每日消耗热量列表 (kcal)")
    private List<Integer> extraExerciseCalories;

    @Schema(description = "每日总消耗热量列表 (kcal)")
    private List<Integer> totalCalories;

    @Schema(description = "近七天项目训练总消耗 (kcal)")
    private Integer totalWorkoutCalories;

    @Schema(description = "近七天额外运动总消耗 (kcal)")
    private Integer totalExtraExerciseCalories;

    @Schema(description = "近七天总消耗 (kcal)")
    private Integer totalCalories7Days;
}
