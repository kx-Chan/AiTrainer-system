package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 食物分析请求参数。
 */
@Schema(description = "AI 食物分析请求参数")
public record AnalyzeFoodDTO(

        @Schema(description = "食物名称", example = "两个鸡蛋, 红烧肉")
        String foodName,

        @Schema(description = "食物重量 (g)", example = "100")
        Integer weight
) {
}
