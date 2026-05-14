package com.aitrainer.agent;

import com.aitrainer.vo.ExerciseAnalysisVO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * AI 运动分析 Agent。
 * 根据运动名称、时长以及用户身体数据（年龄、身高、体重、性别）估算消耗热量。
 *
 * 当前为 mock 实现，后续接入 LangChain4j AI 服务。
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "customOpenAiChatModel")
public interface ExerciseAgent {

    @SystemMessage("""
        你是一个精准的运动消耗分析助手。
        用户会提供运动名称、运动时长、运动描述以及用户的身体数据（年龄、身高、体重、性别）。
        运动描述是用户对运动细节的补充说明，如跑步的配速、骑行距离、游泳姿势等，描述越精准你估算的运动强度和消耗热量就越准确。
        你需要根据运动名称、运动描述和时长，综合判断运动强度，并估算消耗热量。
        必须严格按照 JSON 格式返回，不要有任何多余的文字说明。
        示例格式：{"caloriesBurned": 300, "intensity": "中等强度"}
        """)
    @UserMessage("运动名称：{{exerciseName}}，时长：{{durationMinutes}}分钟，运动描述：{{description}}，用户信息：{{age}}岁，{{height}}cm，{{weight}}kg，{{gender}}")
    ExerciseAnalysisVO analyze(
            @V("exerciseName") String exerciseName,
            @V("durationMinutes") int durationMinutes,
            @V("description") String description,
            @V("age") int age,
            @V("height") int height,
            @V("weight") double weight,
            @V("gender") String gender
    );
}
