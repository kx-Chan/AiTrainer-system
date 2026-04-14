package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "编辑饮食记录参数")
public record UpdateMealDTO(

        @Schema(description = "食物名称", example = "全麦面包, 煮鸡蛋")
        String foodName,

        @Schema(description = "热量 (kcal)", example = "350")
        Integer calories,

        @Schema(description = "食物重量 (g)", example = "200")
        Integer weight,

        @Schema(description = "进餐时间 (HH:mm)", example = "08:00")
        String mealTime
) {
}
