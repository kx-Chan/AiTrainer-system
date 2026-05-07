package com.aitrainer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 私教分析响应 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 私教分析结果")
public class AiCoachAnalyzeResponseVO {

    @Schema(description = "响应类型：chat-闲聊回复, analysis-分析报告")
    private String responseType;

    @Schema(description = "分析类型：training-训练分析, diet-饮食分析, comprehensive-综合分析")
    private String analysisType;

    @Schema(description = "分析结果（Markdown格式）")
    private String analysisResult;

    @Schema(description = "使用的训练数据摘要")
    private String trainingDataSummary;

    @Schema(description = "使用的饮食数据摘要")
    private String dietDataSummary;

    @Schema(description = "使用的用户数据摘要")
    private String profileDataSummary;

    @Schema(description = "会话 ID，用于后续对话保持上下文")
    private String sessionId;

    @Schema(description = "本次用户提问消息的 ID，用于后续查询对应的 AI 回复")
    private Long questionId;
}
