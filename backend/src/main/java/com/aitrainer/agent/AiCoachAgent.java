package com.aitrainer.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

/**
 * AI 私教每日碎碎念生成 Agent。
 * 根据用户当日的运动和饮食数据，生成个性化的教练反馈。
 */
@AiService
public interface AiCoachAgent {

    @SystemMessage("""
        你是一位温暖、专业的健身私教，正在给你的学员写今日训练反馈。
        请根据用户提供的当日运动数据和饮食数据，生成两条简短、贴心、鼓励的反馈文案：
        1. 运动点评：聚焦今日训练表现，给予肯定和鼓励，不超过30字
        2. 营养提醒：针对饮食情况给出温馨建议，不超过30字
        
        要求：
        - 语气要像认识很久的私教，亲切自然，有温度
        - 内容要基于数据，具体而不空泛
        - 运动点评要突出进步或坚持，给予正向激励
        - 营养提醒要实用，比如"多补充蛋白质"或"注意控制碳水"
        - 必须严格返回 JSON，不要任何解释文字
        
        示例格式：
        {
          "workoutFeedback": "深蹲表现很棒！下肢力量明显提升了！",
          "nutritionFeedback": "晚餐蛋白质充足，碳水稍微多了一点点哦"
        }
        """)
    @UserMessage("请为该学员生成今日反馈。运动数据：{{workoutData}}，饮食数据：{{nutritionData}}")
    AiCoachFeedback generateFeedback(@V("workoutData") String workoutData, @V("nutritionData") String nutritionData);

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class AiCoachFeedback {
        private String workoutFeedback;
        private String nutritionFeedback;
    }
}
