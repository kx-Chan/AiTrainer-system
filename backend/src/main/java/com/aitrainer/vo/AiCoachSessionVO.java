package com.aitrainer.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 教练会话信息 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCoachSessionVO {

    /**
     * 会话 ID。
     */
    private String sessionId;

    /**
     * 会话标题（第一条用户消息的摘要）。
     */
    private String title;

    /**
     * 分析类型：training-训练分析, diet-饮食分析, comprehensive-综合分析, chat-闲聊。
     */
    private String analysisType;

    /**
     * 消息数量。
     */
    private int messageCount;

    /**
     * 最后消息时间。
     */
    private LocalDateTime lastMessageTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
