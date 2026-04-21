package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "编辑饮食记录参数")
public record UpdateMealDTO(

        @Schema(description = "食物名称", example = "全麦面包, 煮鸡蛋")
        String foodName,

        @Schema(description = "热量 (kcal)", example = "350")
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
        String mealTime
) {
}
