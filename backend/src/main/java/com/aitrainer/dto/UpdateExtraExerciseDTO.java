package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "编辑额外运动消耗参数")
public record UpdateExtraExerciseDTO(

        @Schema(description = "运动名称", example = "跑步")
        String exerciseName,

        @Schema(description = "消耗热量 (kcal)", example = "300")
        Integer caloriesBurned,

        @Schema(description = "运动时长 (分钟)", example = "30")
        Integer durationMinutes
) {
}
