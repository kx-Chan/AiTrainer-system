package com.aitrainer.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * AI 意图分类 Agent。
 * 用于判断用户输入是闲聊还是需要数据分析。
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "customOpenAiChatModel")
public interface IntentClassifierAgent {

    /**
     * 判断用户意图。
     * 
     * @param userMessage 用户消息
     * @param hasData 是否有可用的训练/饮食数据
     * @return 意图类型：chitchat-闲聊, analysis-分析请求, greeting-问候
     */
    @SystemMessage("""
        你是一个专业的健身助手，你的任务是判断用户的消息意图。
        
        请根据用户的消息内容，判断其意图类型：
        
        1. **chitchat（闲聊）**：用户只是在打招呼、问候、闲聊、或询问与健身数据分析无关的内容。
           - 例如："你好"、"今天天气不错"、"你是谁"等
           - 例如："谢谢"、"好的"、"知道了"等确认类消息
           - 例如："给我讲个笑话"、"今天心情不好"等无关话题
        
        2. **analysis（分析请求）**：用户提供了具体的健身数据或请求分析。
           - 例如："分析一下我今天的训练"
           - 例如："看看我最近的饮食情况"
           - 例如："我今天跑步了30分钟，消耗了300卡路里"
           - 例如："我吃了鸡胸肉、米饭、西兰花"等具体食物描述
           - 例如：询问训练计划、饮食建议等
        
        3. **greeting（问候）**：用户只是打招呼或询问AI状态。
           - 例如："嗨"、"你好啊"、"在吗"、"教练好"
        
        返回格式要求：
        - 只需返回一个单词：chitchat 或 analysis 或 greeting
        - 不要添加任何解释或其他内容
        - 严格返回其中一个选项
        """)
    @UserMessage("用户消息：{{message}}\n\n可用数据状态：{{hasData}}")
    String classifyIntent(@V("message") String userMessage, @V("hasData") String hasData);
}
