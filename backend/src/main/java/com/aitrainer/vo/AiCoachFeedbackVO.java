package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 私教每日碎碎念反馈 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 私教每日碎碎念反馈")
public class AiCoachFeedbackVO {

    @Schema(description = "运动点评")
    private String workoutFeedback;

    @Schema(description = "营养提醒")
    private String nutritionFeedback;
}
