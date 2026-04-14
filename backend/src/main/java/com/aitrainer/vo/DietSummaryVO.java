package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 饮食汇总 VO，包含当日热量统计与饮食记录列表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "饮食汇总数据")
public class DietSummaryVO {

    @Schema(description = "查询日期 (yyyy-MM-dd)")
    private String date;

    @Schema(description = "饮食记录列表")
    private List<MealVO> meals;

    @Schema(description = "当日摄入总热量 (kcal)")
    private Integer totalIntakeCalories;

    @Schema(description = "基础代谢热量 BMR (kcal)")
    private Integer bmrCalories;

    @Schema(description = "训练消耗热量 (kcal)")
    private Integer workoutBurnedCalories;

    @Schema(description = "用户自填额外消耗热量 (kcal)")
    private Integer extraBurnedCalories;

    @Schema(description = "每日目标热量 (kcal)，根据健身目标科学计算")
    private Integer targetCalories;

    @Schema(description = "剩余可摄入热量 (kcal)，负数表示超标")
    private Integer remainingCalories;

    @Schema(description = "健身目标 (lose/gain/maintain)")
    private String goal;

    @Schema(description = "当日已使用的餐次类型列表")
    private Set<String> usedMealTypes;

    @Schema(description = "额外运动消耗列表")
    private List<ExtraExerciseVO> extraExercises;

    @Schema(description = "当日项目训练消耗明细")
    private List<WorkoutBurnedDetailVO> workoutDetails;

    /**
     * 项目训练消耗明细内部类。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "训练消耗明细")
    public static class WorkoutBurnedDetailVO {

        @Schema(description = "训练项目名称")
        private String workoutName;

        @Schema(description = "消耗热量 (kcal)")
        private Integer caloriesBurned;

        @Schema(description = "训练时长 (分钟)")
        private Integer durationMinutes;

        @Schema(description = "训练时间")
        private String createdAt;
    }
}
