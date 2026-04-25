package com.aitrainer.mapper;

import com.aitrainer.entity.AiCoachChatHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
     * 查询用户的所有会话 ID 列表。
     *
     * @param userId 用户 ID
     * @return 会话 ID 列表
     */
    @Select("""
            SELECT DISTINCT session_id FROM ai_coach_chat_history
            WHERE user_id = #{userId}
            ORDER BY session_id DESC
            """)
    List<String> findSessionIdsByUserId(@Param("userId") Long userId);

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
}
