package com.aitrainer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 私教分析请求 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 私教分析请求参数")
public class AiCoachAnalyzeRequestDTO {

    @Schema(description = "分析类型：training-训练分析, diet-饮食分析, comprehensive-综合分析", example = "comprehensive")
    @NotBlank(message = "分析类型不能为空")
    private String analysisType;

    @Schema(description = "用户的提问", example = "请分析我最近一周的训练表现")
    @NotBlank(message = "用户问题不能为空")
    private String question;

    @Schema(description = "是否使用训练数据", example = "true")
    private Boolean includeTrainingData;

    @Schema(description = "训练数据时间范围：7-近7天, 30-近30天", example = "7")
    private Integer trainingDays;

    @Schema(description = "是否使用饮食数据", example = "true")
    private Boolean includeDietData;

    @Schema(description = "饮食数据时间范围：7-近7天, 30-近30天", example = "7")
    private Integer dietDays;

    @Schema(description = "会话 ID，用于标识对话上下文，不传则自动创建新会话", example = "session_20240425_abc123")
    private String sessionId;
}
