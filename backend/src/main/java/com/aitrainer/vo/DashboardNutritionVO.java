package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据看板 - 营养摄入配比 VO。
 * 营养素标准配比（热量占比）：
 * - 碳水化合物: 50%
 * - 蛋白质: 30%
 * - 脂肪: 20%
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "营养摄入配比数据")
public class DashboardNutritionVO {

    @Schema(description = "查询日期 (yyyy-MM-dd)")
    private String date;

    @Schema(description = "当日总摄入热量 (kcal)")
    private Integer totalCalories;

    @Schema(description = "碳水化合物摄入量 (g)")
    private Integer carbsGrams;

    @Schema(description = "蛋白质摄入量 (g)")
    private Integer proteinGrams;

    @Schema(description = "脂肪摄入量 (g)")
    private Integer fatGrams;

    @Schema(description = "碳水化合物热量 (kcal)")
    private Integer carbsCalories;

    @Schema(description = "蛋白质热量 (kcal)")
    private Integer proteinCalories;

    @Schema(description = "脂肪热量 (kcal)")
    private Integer fatCalories;

    @Schema(description = "碳水化合物热量占比 (%)")
    private Integer carbsPercent;

    @Schema(description = "蛋白质热量占比 (%)")
    private Integer proteinPercent;

    @Schema(description = "脂肪热量占比 (%)")
    private Integer fatPercent;

    @Schema(description = "碳水化合物目标占比 (%)，默认50")
    private Integer carbsTargetPercent;

    @Schema(description = "蛋白质目标占比 (%)，默认30")
    private Integer proteinTargetPercent;

    @Schema(description = "脂肪目标占比 (%)，默认20")
    private Integer fatTargetPercent;

    @Schema(description = "目标总热量 (kcal)")
    private Integer targetCalories;

    @Schema(description = "碳水化合物目标克数 (g)")
    private Integer carbsTargetGrams;

    @Schema(description = "蛋白质目标克数 (g)")
    private Integer proteinTargetGrams;

    @Schema(description = "脂肪目标克数 (g)")
    private Integer fatTargetGrams;

    @Schema(description = "营养素摄入详情列表")
    private List<NutritionDetailVO> details;

    /**
     * 营养素摄入详情。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "营养素摄入详情")
    public static class NutritionDetailVO {

        @Schema(description = "食物名称")
        private String foodName;

        @Schema(description = "热量 (kcal)")
        private Integer calories;

        @Schema(description = "碳水化合物 (g)")
        private Integer carbs;

        @Schema(description = "蛋白质 (g)")
        private Integer protein;

        @Schema(description = "脂肪 (g)")
        private Integer fat;

        @Schema(description = "所属餐次 (breakfast/lunch/dinner/snack)")
        private String mealType;

        @Schema(description = "进餐时间")
        private String mealTime;

        @Schema(description = "碳水化合物占比 (%)")
        private Integer carbsPercent;

        @Schema(description = "蛋白质占比 (%)")
        private Integer proteinPercent;

        @Schema(description = "脂肪占比 (%)")
        private Integer fatPercent;
    }
}