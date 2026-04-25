package com.aitrainer.mapper;

import com.aitrainer.entity.AiCoachChatHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 教练聊天历史 Mapper。
 */
@Mapper
public interface AiCoachChatHistoryMapper extends BaseMapper<AiCoachChatHistory> {

    /**
     * 查询指定会话的聊天历史。
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @param limit     限制数量
     * @return 聊天历史列表
     */
    @Select("""
            SELECT * FROM ai_coach_chat_history
            WHERE user_id = #{userId} AND session_id = #{sessionId}
            ORDER BY created_at ASC
            LIMIT #{limit}
            """)
    List<AiCoachChatHistory> findBySessionId(
            @Param("userId") Long userId,
            @Param("sessionId") String sessionId,
            @Param("limit") int limit);

    /**
     * 查询用户的所有会话 ID 列表（按最后消息时间排序）。
     *
     * @param userId 用户 ID
     * @return 会话 ID 列表
     */
    @Select("""
            SELECT session_id FROM ai_coach_chat_history
            WHERE user_id = #{userId}
            GROUP BY session_id
            ORDER BY MAX(created_at) DESC
            """)
    List<String> findSessionIdsByUserId(@Param("userId") Long userId);

    /**
     * 获取会话的第一条用户消息（用于作为会话标题）。
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @return 第一条用户消息
     */
    @Select("""
            SELECT content FROM ai_coach_chat_history
            WHERE user_id = #{userId} AND session_id = #{sessionId} AND role = 'user'
            ORDER BY created_at ASC
            LIMIT 1
            """)
    String getFirstUserMessage(@Param("userId") Long userId, @Param("sessionId") String sessionId);

    /**
     * 获取会话的最后消息时间。
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @return 最后消息时间
     */
    @Select("""
            SELECT MAX(created_at) FROM ai_coach_chat_history
            WHERE user_id = #{userId} AND session_id = #{sessionId}
            """)
    LocalDateTime getLastMessageTime(@Param("userId") Long userId, @Param("sessionId") String sessionId);

    /**
     * 获取会话的消息数量。
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @return 消息数量
     */
    @Select("""
            SELECT COUNT(*) FROM ai_coach_chat_history
            WHERE user_id = #{userId} AND session_id = #{sessionId}
            """)
    int getMessageCount(@Param("userId") Long userId, @Param("sessionId") String sessionId);

    /**
     * 删除指定会话的所有聊天历史。
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @return 删除数量
     */
    @Select("""
            DELETE FROM ai_coach_chat_history
            WHERE user_id = #{userId} AND session_id = #{sessionId}
            """)
    int deleteBySessionId(@Param("userId") Long userId, @Param("sessionId") String sessionId);

    /**
     * 根据提问 ID 获取对应的 AI 回复（找邻居法）。
     * 找到提问之后的第一条 assistant 角色消息。
     *
     * @param userId     用户 ID
     * @param sessionId  会话 ID
     * @param questionId 提问消息的 ID
     * @return AI 回复消息，如果没有找到返回 null
     */
    @Select("""
            SELECT * FROM ai_coach_chat_history
            WHERE user_id = #{userId}
              AND session_id = #{sessionId}
              AND id > #{questionId}
              AND role = 'assistant'
            ORDER BY id ASC
            LIMIT 1
            """)
    AiCoachChatHistory findAssistantReplyByQuestionId(
            @Param("userId") Long userId,
            @Param("sessionId") String sessionId,
            @Param("questionId") Long questionId);
}
