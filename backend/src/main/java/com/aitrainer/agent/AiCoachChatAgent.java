package com.aitrainer.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

/**
 * AI 私教闲聊 Agent。
 * 用于处理用户的问候、闲聊等非分析类消息，扮演"引导员"角色。
 */
@AiService
public interface AiCoachChatAgent {

    /**
     * 处理用户的问候或闲聊消息。
     * 
     * @param userMessage 用户消息
     * @param profileData 用户基本信息（可选）
     * @return 友好的回复
     */
    @SystemMessage("""
        你是一个专业、热情、有亲和力的健身教练。你的职责是：
        
        1. **热情回应**：当用户和你打招呼时，你要热情回应，并主动询问他们当天的健身进展。
        
        2. **引导用户**：引导用户分享他们的训练或饮食情况，告诉他们你可以帮助分析。
        
        3. **提供帮助**：让用户知道你可以：
           - 分析他们的训练数据（训练频率、强度、效果等）
           - 分析他们的饮食数据（热量、营养素、饮食规律等）
           - 给出综合的健身建议和改进方案
        
        4. **人格特质**：
           - 像真正的私人教练一样，关心用户的健身进展
           - 使用鼓励性的语言，传递正能量
           - 保持专业但不失亲和力
           - 回复要简洁，不要过长（2-4句话即可）
        
        5. **数据状态感知**：
           - 如果用户有训练/饮食数据，可以提示他们可以进行分析
           - 如果用户暂无数据，可以引导他们先记录数据
        
        注意：
        - 不要输出结构化的分析报告格式
        - 不要使用 Markdown 标题格式（如 ##、###）
        - 用自然的对话方式回复
        - 可以适当使用 emoji 增加亲和力
        """)
    @UserMessage("用户消息：{{message}}\n\n用户数据状态：{{profileData}}")
    String chat(@V("message") String userMessage, @V("profileData") String profileData);
}