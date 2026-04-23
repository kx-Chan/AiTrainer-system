package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 运动分析请求参数。
 */
@Schema(description = "AI 运动分析请求参数")
public record AnalyzeExerciseDTO(

        @Schema(description = "运动名称", example = "跑步")
        String exerciseName,

        @Schema(description = "运动描述（如配速、距离等），描述越精准AI估算越准确", example = "配速5'30\"/km，跑了5公里")
        String description,

        @Schema(description = "运动时长 (分钟)", example = "30")
        Integer durationMinutes
) {
}
