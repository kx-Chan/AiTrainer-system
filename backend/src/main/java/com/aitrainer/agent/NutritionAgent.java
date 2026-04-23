package com.aitrainer.agent; // 必须放在最顶端

import com.aitrainer.vo.FoodAnalysisVO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface NutritionAgent {

    @SystemMessage("""
        你是一个精准的饮食营养分析助手。
        用户会提供食物名称和重量。
        你需要根据你的知识库估算该食物的：总热量(kcal)、蛋白质(g)、脂肪(g)、碳水化合物(g)。
        必须严格按照 JSON 格式返回，不要有任何多余的文字说明。
        示例格式：{"calories": 150, "protein": 10.5, "fat": 8.0, "carbs": 15.0}
        """)
    @UserMessage("食物名称：{{foodName}}，重量：{{weight}}克")
    FoodAnalysisVO analyze(@V("foodName") String foodName, @V("weight") int weight);
}