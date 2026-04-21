package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "添加饮食记录参数")
public record AddMealDTO(

        @Schema(description = "餐次类型", example = "breakfast")
        @NotBlank(message = "餐次类型不能为空")
        String mealType,

        @Schema(description = "食物名称", example = "全麦面包, 煮鸡蛋")
        @NotBlank(message = "食物名称不能为空")
        String foodName,

        @Schema(description = "热量 (kcal)", example = "350")
        @NotNull(message = "热量不能为空")
        Integer calories,

        @Schema(description = "蛋白质 (g)", example = "20")
        Integer protein,

        @Schema(description = "脂肪 (g)", example = "10")
        Integer fat,

        @Schema(description = "碳水化合物 (g)", example = "40")
        Integer carbs,

        @Schema(description = "食物重量 (g)", example = "200")
        Integer weight,

        @Schema(description = "进餐时间 (HH:mm)", example = "08:00")
        @NotBlank(message = "进餐时间不能为空")
        String mealTime,

        @Schema(description = "日期 (yyyy-MM-dd)，不传则默认今天", example = "2026-04-10")
        String date,

        @Schema(description = "额外消耗热量 (kcal)，用户自行添加的其他运动消耗", example = "0")
        Integer extraBurnedCalories
) {
}
