package com.aitrainer.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.Map;

/**
 * 自定义 AI 模型配置。
 * <p>
 * 覆盖 langchain4j-open-ai-spring-boot-starter 的自动配置，
 * 解决部分 OpenAI 兼容代理（如 ai.sandboxai.top）返回 SSE 流式格式
 * 而非标准 JSON 的问题。
 * <p>
 * 核心修复：通过设置 Accept: application/json 请求头，
 * 明确告知代理服务器返回标准 JSON 响应而非 SSE 流式格式。
 */
@Configuration
public class AiModelConfig {

    private static final Logger log = LoggerFactory.getLogger(AiModelConfig.class);

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-5.4}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.base-url:https://ai.sandboxai.top/v1/}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.timeout:120s}")
    private Duration timeout;

    @Value("${langchain4j.open-ai.chat-model.max-tokens:2000}")
    private int maxTokens;

    @Value("${langchain4j.open-ai.chat-model.max-retries:1}")
    private int maxRetries;

    @Value("${langchain4j.open-ai.chat-model.log-requests:true}")
    private boolean logRequests;

    @Value("${langchain4j.open-ai.chat-model.log-responses:true}")
    private boolean logResponses;

    /**
     * 自定义 ChatLanguageModel Bean，覆盖 starter 自动配置。
     * <p>
     * 使用 {@code @Primary} 确保优先级高于自动配置的 Bean。
     * <p>
     * 关键修复：设置 Accept: application/json 请求头，
     * 告知代理服务器返回标准 JSON 而非 SSE 流式格式（data: {...}）。
     */
    @Bean
    @Primary
    public ChatLanguageModel chatLanguageModel() {
        log.info("初始化自定义 OpenAiChatModel: baseUrl={}, model={}, maxTokens={}, timeout={}s",
                baseUrl, modelName, maxTokens, timeout.getSeconds());

        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .timeout(timeout)
                .maxTokens(maxTokens)
                .maxRetries(maxRetries)
                .logRequests(logRequests)
                .logResponses(logResponses)
                // 关键：显式设置 Accept 头，告知代理返回标准 JSON 而非 SSE 流式格式
                .customHeaders(Map.of("Accept", "application/json"))
                .build();
    }
}
