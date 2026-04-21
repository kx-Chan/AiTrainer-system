package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 食物分析结果 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 食物分析结果")
public class FoodAnalysisVO {

    @Schema(description = "热量 (kcal)")
    private Integer calories;

    @Schema(description = "蛋白质 (g)")
    private Integer protein;

    @Schema(description = "脂肪 (g)")
    private Integer fat;

    @Schema(description = "碳水化合物 (g)")
    private Integer carbs;
}
