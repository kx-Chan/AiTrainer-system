package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据看板 - 详细训练日志 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "详细训练日志数据")
public class DashboardTrainingLogVO {

    @Schema(description = "项目训练日志列表")
    private List<TrainingLogItemVO> workoutLogs;

    @Schema(description = "额外运动日志列表")
    private List<ExtraExerciseLogItemVO> extraExerciseLogs;

    @Schema(description = "饮食日志列表")
    private List<DietLogItemVO> dietLogs;

    @Schema(description = "项目训练总次数")
    private Integer totalWorkoutSessions;

    @Schema(description = "额外运动总次数")
    private Integer totalExtraExerciseSessions;

    @Schema(description = "饮食记录总次数")
    private Integer totalDietSessions;

    /**
     * 项目训练日志项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "项目训练日志项")
    public static class TrainingLogItemVO {

        @Schema(description = "记录 ID")
        private Long id;

        @Schema(description = "项目 ID")
        private String workoutId;

        @Schema(description = "项目名称")
        private String workoutName;

        @Schema(description = "综合评分 (0-100)")
        private Integer score;

        @Schema(description = "评级 (S/A/B/C)")
        private String grade;

        @Schema(description = "有效动作次数")
        private Integer validReps;

        @Schema(description = "训练时长 (秒)")
        private Integer durationSeconds;

        @Schema(description = "消耗热量 (kcal)")
        private Integer caloriesBurned;

        @Schema(description = "AI 点评")
        private String comment;

        @Schema(description = "训练完成时间")
        private String createdAt;
    }

    /**
     * 额外运动日志项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "额外运动日志项")
    public static class ExtraExerciseLogItemVO {

        @Schema(description = "记录 ID")
        private Long id;

        @Schema(description = "运动名称")
        private String exerciseName;

        @Schema(description = "消耗热量 (kcal)")
        private Integer caloriesBurned;

        @Schema(description = "运动时长 (分钟)")
        private Integer durationMinutes;

        @Schema(description = "运动日期")
        private String exerciseDate;

        @Schema(description = "记录时间")
        private String createdAt;
    }

    /**
     * 饮食日志项（按餐次聚合）。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "饮食日志项（按餐次聚合）")
    public static class DietLogItemVO {

        @Schema(description = "餐次类型 (breakfast/lunch/dinner/snack)")
        private String mealType;

        @Schema(description = "餐次中文名")
        private String mealTypeName;

        @Schema(description = "记录日期")
        private String mealDate;

        @Schema(description = "该餐次总热量 (kcal)")
        private Integer totalCalories;

        @Schema(description = "食物明细列表")
        private List<DietFoodDetailVO> foods;
    }

    /**
     * 饮食食物明细。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "饮食食物明细")
    public static class DietFoodDetailVO {

        @Schema(description = "食物名称")
        private String foodName;

        @Schema(description = "热量 (kcal)")
        private Integer calories;

        @Schema(description = "用餐时间")
        private String mealTime;
    }
}
