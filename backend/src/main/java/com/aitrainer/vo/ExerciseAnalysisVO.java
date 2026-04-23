package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 运动分析结果 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 运动分析结果")
public class ExerciseAnalysisVO {

    @Schema(description = "消耗热量 (kcal)")
    private Integer caloriesBurned;

    @Schema(description = "运动强度描述", example = "中等强度")
    private String intensity;
}
