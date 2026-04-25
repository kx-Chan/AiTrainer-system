package com.aitrainer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 教练聊天历史实体。
 * 用于存储用户与 AI 教练的对话记录，实现上下文功能。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_coach_chat_history")
public class AiCoachChatHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 会话 ID（用于区分不同的对话会话）。
     */
    private String sessionId;

    /**
     * 消息角色：user-用户消息, assistant-AI回复。
     */
    private String role;

    /**
     * 消息内容。
     */
    private String content;

    /**
     * 分析类型：training-训练分析, diet-饮食分析, comprehensive-综合分析。
     */
    private String analysisType;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
