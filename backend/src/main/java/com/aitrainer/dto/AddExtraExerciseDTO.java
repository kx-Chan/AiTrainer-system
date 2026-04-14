package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "添加额外运动消耗参数")
public record AddExtraExerciseDTO(

        @Schema(description = "运动名称", example = "跑步")
        @NotBlank(message = "运动名称不能为空")
        String exerciseName,

        @Schema(description = "消耗热量 (kcal)", example = "300")
        @NotNull(message = "消耗热量不能为空")
        Integer caloriesBurned,

        @Schema(description = "运动时长 (分钟)", example = "30")
        Integer durationMinutes,

        @Schema(description = "运动日期 (yyyy-MM-dd)，不传则默认今天", example = "2026-04-10")
        String date
) {
}
