package com.aitrainer.controller;

import com.aitrainer.common.result.Result;
import com.aitrainer.common.security.CustomUser;
import com.aitrainer.dto.AiCoachAnalyzeRequestDTO;
import com.aitrainer.entity.AiCoachChatHistory;
import com.aitrainer.service.AiCoachChatHistoryService;
import com.aitrainer.service.AiCoachService;
import com.aitrainer.vo.AiCoachAnalyzeResponseVO;
import com.aitrainer.vo.AiCoachSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 私教控制器。
 */
@Tag(name = "AiCoach", description = "AI 私教接口")
@RestController
@RequestMapping("/api/ai-coach")
@RequiredArgsConstructor
@Slf4j
public class AiCoachController {

    private final AiCoachService aiCoachService;
    private final AiCoachChatHistoryService chatHistoryService;

    /**
     * AI 私教分析接口。
     *
     * @param authentication 登录信息
     * @param dto            分析请求参数
     * @return 分析结果
     */
    @Operation(summary = "AI 私教分析")
    @PostMapping("/analyze")
    public Result<AiCoachAnalyzeResponseVO> analyze(
            final Authentication authentication,
            @Valid @RequestBody final AiCoachAnalyzeRequestDTO dto) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        log.info("用户 {} 请求 AI 分析，类型：{}", user.getId(), dto.getAnalysisType());
        return Result.success(aiCoachService.analyze(user.getId(), dto));
    }

    /**
     * 获取聊天历史。
     *
     * @param authentication 登录信息
     * @param sessionId      会话 ID
     * @param limit          限制数量
     * @return 聊天历史列表
     */
    @Operation(summary = "获取聊天历史")
    @GetMapping("/history")
    public Result<List<AiCoachChatHistory>> getChatHistory(
            final Authentication authentication,
            @RequestParam final String sessionId,
            @RequestParam(defaultValue = "20") final int limit) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(chatHistoryService.getChatHistory(user.getId(), sessionId, limit));
    }

    /**
     * 获取用户的所有会话列表。
     *
     * @param authentication 登录信息
     * @return 会话 ID 列表
     */
    @Operation(summary = "获取会话列表")
    @GetMapping("/sessions")
    public Result<List<String>> getSessions(final Authentication authentication) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(chatHistoryService.getUserSessionIds(user.getId()));
    }

    /**
     * 删除指定会话。
     *
     * @param authentication 登录信息
     * @param sessionId      会话 ID
     * @return 是否成功
     */
    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Boolean> deleteSession(
            final Authentication authentication,
            @PathVariable final String sessionId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(chatHistoryService.deleteSession(user.getId(), sessionId));
    }

    /**
     * 获取用户的所有会话详情列表。
     *
     * @param authentication 登录信息
     * @param limit          限制数量
     * @return 会话详情列表
     */
    @Operation(summary = "获取会话详情列表")
    @GetMapping("/sessions/detail")
    public Result<List<AiCoachSessionVO>> getSessionDetails(
            final Authentication authentication,
            @RequestParam(defaultValue = "20") final int limit) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        return Result.success(chatHistoryService.getUserSessions(user.getId(), limit));
    }

    /**
     * 根据提问 ID 获取对应的 AI 回复。
     * 实现消息锚定功能：点击左侧提问，右侧展示对应的 AI 分析结果。
     *
     * @param authentication 登录信息
     * @param sessionId      会话 ID
     * @param questionId     提问消息的 ID
     * @return AI 回复消息
     */
    @Operation(summary = "获取提问对应的AI回复")
    @GetMapping("/question/{questionId}/reply")
    public Result<AiCoachChatHistory> getAssistantReply(
            final Authentication authentication,
            @RequestParam final String sessionId,
            @PathVariable final Long questionId) {
        final CustomUser user = (CustomUser) authentication.getPrincipal();
        final AiCoachChatHistory reply = chatHistoryService.getAssistantReplyByQuestionId(
                user.getId(), sessionId, questionId);
        
        if (reply == null) {
            log.warn("未找到提问对应的AI回复: questionId={}, sessionId={}", questionId, sessionId);
        }
        
        return Result.success(reply);
    }
}
