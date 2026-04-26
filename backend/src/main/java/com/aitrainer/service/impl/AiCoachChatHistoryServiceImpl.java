package com.aitrainer.service.impl;

import com.aitrainer.entity.AiCoachChatHistory;
import com.aitrainer.mapper.AiCoachChatHistoryMapper;
import com.aitrainer.service.AiCoachChatHistoryService;
import com.aitrainer.vo.AiCoachSessionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 教练聊天历史服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiCoachChatHistoryServiceImpl implements AiCoachChatHistoryService {

    private final AiCoachChatHistoryMapper chatHistoryMapper;

    /**
     * 默认保留的历史消息数量。
     */
    private static final int DEFAULT_HISTORY_LIMIT = 20;

    @Override
    public AiCoachChatHistory saveMessage(final Long userId, final String sessionId, 
                                          final String role, final String content, 
                                          final String analysisType, final Long replyTo) {
        final AiCoachChatHistory message = AiCoachChatHistory.builder()
                .userId(userId)
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .analysisType(analysisType)
                .replyTo(replyTo)
                .createdAt(LocalDateTime.now())
                .build();

        chatHistoryMapper.insert(message);
        log.debug("保存聊天消息: userId={}, sessionId={}, role={}, replyTo={}", userId, sessionId, role, replyTo);
        return message;
    }

    @Override
    public List<AiCoachChatHistory> getChatHistory(final Long userId, final String sessionId, final int limit) {
        return chatHistoryMapper.findBySessionId(userId, sessionId, limit);
    }

    @Override
    public String formatChatHistory(final Long userId, final String sessionId, final int limit) {
        final List<AiCoachChatHistory> history = getChatHistory(userId, sessionId, limit);
        
        if (history.isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("【历史对话记录】\n");
        
        for (final AiCoachChatHistory message : history) {
            if ("user".equals(message.getRole())) {
                sb.append("用户：").append(message.getContent()).append("\n");
            } else {
                // AI 回复可能很长，截取摘要
                final String summary = summarizeAiResponse(message.getContent());
                sb.append("AI教练：").append(summary).append("\n");
            }
        }
        
        return sb.toString();
    }

    @Override
    public List<String> getUserSessionIds(final Long userId) {
        return chatHistoryMapper.findSessionIdsByUserId(userId);
    }

    @Override
    public List<AiCoachSessionVO> getUserSessions(final Long userId, final int limit) {
        // 获取所有会话 ID
        final List<String> sessionIds = chatHistoryMapper.findSessionIdsByUserId(userId);
        
        if (sessionIds.isEmpty()) {
            return List.of();
        }

        // 限制数量
        final List<String> limitedSessionIds = sessionIds.stream()
                .limit(limit)
                .collect(Collectors.toList());

        // 构建会话详情
        return limitedSessionIds.stream()
                .map(sessionId -> {
                    final String firstMessage = chatHistoryMapper.getFirstUserMessage(userId, sessionId);
                    final LocalDateTime lastTime = chatHistoryMapper.getLastMessageTime(userId, sessionId);
                    final int messageCount = chatHistoryMapper.getMessageCount(userId, sessionId);
                    
                    // 获取第一条消息的分析类型
                    final List<AiCoachChatHistory> history = chatHistoryMapper.findBySessionId(userId, sessionId, 1);
                    final String analysisType = history.isEmpty() ? "comprehensive" : history.get(0).getAnalysisType();

                    return AiCoachSessionVO.builder()
                            .sessionId(sessionId)
                            .title(truncateTitle(firstMessage, 50))
                            .analysisType(analysisType != null ? analysisType : "comprehensive")
                            .messageCount(messageCount)
                            .lastMessageTime(lastTime)
                            .createdAt(lastTime) // 使用最后消息时间作为创建时间的近似
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteSession(final Long userId, final String sessionId) {
        final int deleted = chatHistoryMapper.delete(new LambdaQueryWrapper<AiCoachChatHistory>()
                .eq(AiCoachChatHistory::getUserId, userId)
                .eq(AiCoachChatHistory::getSessionId, sessionId));
        
        log.info("删除会话: userId={}, sessionId={}, deleted={}", userId, sessionId, deleted);
        return deleted > 0;
    }

    @Override
    public AiCoachChatHistory getAssistantReplyByQuestionId(final Long userId, final String sessionId, final Long questionId) {
        return chatHistoryMapper.findAssistantReplyByQuestionId(userId, sessionId, questionId);
    }

    /**
     * 对 AI 回复进行摘要，保留关键信息。
     */
    private String summarizeAiResponse(final String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // 如果内容较短，直接返回
        if (content.length() <= 200) {
            return content;
        }
        
        // 截取前200个字符作为摘要
        return content.substring(0, 200) + "...";
    }

    /**
     * 截取标题，保留指定长度。
     */
    private String truncateTitle(final String title, final int maxLength) {
        if (title == null || title.isEmpty()) {
            return "新对话";
        }
        
        if (title.length() <= maxLength) {
            return title;
        }
        
        return title.substring(0, maxLength) + "...";
    }
}
