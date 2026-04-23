package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI 运动战报生成结果 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 运动战报生成结果")
public class WorkoutReportVO {

    @Schema(description = "综合评分 (0-100)", example = "92")
    private Integer score;

    @Schema(description = "评级 (S/A/B/C)", example = "S")
    private String grade;

    @Schema(description = "有效动作次数", example = "45")
    private Integer validReps;

    @Schema(description = "无效动作次数", example = "2")
    private Integer invalidReps;

    @Schema(description = "训练总时长（秒）", example = "1200")
    private Integer durationSeconds;

    @Schema(description = "消耗热量 (kcal)", example = "280")
    private Integer caloriesBurned;

    @Schema(description = "AI 点评文本", example = "表现完美！节奏控制得非常好。")
    private String comment;

    @Schema(description = "五维雷达图评分")
    private Map<String, Integer> radar;
}
