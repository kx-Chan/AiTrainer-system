package com.aitrainer.service;

import com.aitrainer.entity.AiCoachChatHistory;
import com.aitrainer.vo.AiCoachSessionVO;

import java.util.List;

/**
 * AI 教练聊天历史服务接口。
 */
public interface AiCoachChatHistoryService {

    /**
     * 保存聊天消息。
     *
     * @param userId       用户 ID
     * @param sessionId    会话 ID
     * @param role         消息角色
     * @param content      消息内容
     * @param analysisType 分析类型
     * @param replyTo      关联的提问消息 ID（assistant 消息需要填写对应的 user 消息 ID）
     * @return 保存的消息
     */
    AiCoachChatHistory saveMessage(Long userId, String sessionId, String role, String content, String analysisType, Long replyTo);

    /**
     * 获取会话的聊天历史。
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @param limit     限制数量（保留最近的 N 条记录）
     * @return 聊天历史列表
     */
    List<AiCoachChatHistory> getChatHistory(Long userId, String sessionId, int limit);

    /**
     * 将聊天历史格式化为对话字符串。
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @param limit     限制数量
     * @return 格式化的对话字符串
     */
    String formatChatHistory(Long userId, String sessionId, int limit);

    /**
     * 获取用户的所有会话 ID。
     *
     * @param userId 用户 ID
     * @return 会话 ID 列表
     */
    List<String> getUserSessionIds(Long userId);

    /**
     * 获取用户的所有会话详情列表。
     *
     * @param userId 用户 ID
     * @param limit  限制数量
     * @return 会话详情列表
     */
    List<AiCoachSessionVO> getUserSessions(Long userId, int limit);

    /**
     * 删除指定会话。
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @return 是否成功
     */
    boolean deleteSession(Long userId, String sessionId);

    /**
     * 根据提问 ID 获取对应的 AI 回复。
     *
     * @param userId     用户 ID
     * @param sessionId  会话 ID
     * @param questionId 提问消息的 ID
     * @return AI 回复消息，如果没有找到返回 null
     */
    AiCoachChatHistory getAssistantReplyByQuestionId(Long userId, String sessionId, Long questionId);
}
