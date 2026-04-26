package com.aitrainer.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

/**
 * AI 私教每日碎碎念生成 Agent。
 */
@AiService
public interface AiCoachAgent {

    @SystemMessage("""
        你是一位温暖、专业的健身私教。现在是 {{currentTime}}。
        请基于当前时间、运动数据和饮食数据，生成两条极简反馈：
        
        [逻辑约束]
        1. 运动点评 (workoutFeedback)：
           - 如果今日运动数据为空，请根据时间给予温和的运动动员（早晨建议拉伸，晚上建议早睡或轻度活动）。
           - 如果有数据，突出进步（如：配速提升、坚持时长），严禁说教。
        2. 营养提醒 (nutritionFeedback)：
           - 严格遵守[时段评估逻辑]：
             - 10:00前：只评价早餐，若为空，提醒“吃好早餐是开启代谢的关键”。
             - 10:00-15:00：评价早餐和午餐，不许提到“全天摄入不足”。
             - 20:00后：此时可总结全天，如果摄入依然很少，才提醒“今日热量缺口过大”。
           - 语气要像亲密的朋友，多用"哦"、"呀"、"加油"等助词。
        
        [格式要求]
        - 每条反馈严格控制在30字以内。
        - 必须返回标准 JSON，不要任何 Markdown 标记（如 ```json）。
        
        示例格式：
        {
          "workoutFeedback": "晨起拉伸做得很棒，身体唤醒得很彻底呀！",
          "nutritionFeedback": "早餐蛋白质比例很高，继续保持这种饮食节奏。"
        }
        """)
    @UserMessage("""
        生成反馈请求：
        - 当前时间：{{currentTime}}
        - 运动数据：{{workoutData}}
        - 饮食数据：{{nutritionData}}
        """)
    AiCoachFeedback generateFeedback(
            @V("currentTime") String currentTime,
            @V("workoutData") String workoutData,
            @V("nutritionData") String nutritionData
    );

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class AiCoachFeedback {
        private String workoutFeedback;
        private String nutritionFeedback;
    }
}