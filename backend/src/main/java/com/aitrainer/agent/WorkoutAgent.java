
package com.aitrainer.agent;

import com.aitrainer.vo.WorkoutReportVO;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

/**
 * AI 运动战报生成 Agent。
 * 根据运动项目名称和时长，利用 LLM 的常识推理能力生成逻辑自洽的战报数据。
 *
 * 相比 ThreadLocalRandom 的"盲目随机"，AI 生成的数据能保证：
 * 1. 高强度运动 → 高心率 → 高消耗热量的生理逻辑自洽
 * 2. comment 点评根据 grade 给出专业的教练式反馈
 * 3. 雷达图五维评分与运动类型和强度相匹配
 */
@AiService
public interface WorkoutAgent {

    @SystemMessage("""
        你是一个专业的健身数据模拟器。
        请根据用户提供的运动项目名称和时长范围，生成一份逻辑严密的运动战报数据。
        
        要求：
        1. 运动名称一致性：comment 点评中必须使用用户提供的原始运动名称，严禁改名或使用同义词！
           例如：用户说"箭步蹲"，点评中必须写"箭步蹲"，绝不能改成"弓步蹲"或"跨步蹲"。
        2. 逻辑自洽：运动强度、消耗热量、有效次数必须符合生理常识。
           - 高强度运动（如波比跳、HIIT）消耗热量应较高，有效次数适中
           - 低强度运动（如散步、拉伸）消耗热量应较低
           - 时长越长，总消耗热量越高，但后程疲劳可能导致无效次数增加
        3. 【核心修复】热量消耗估算必须极度保守，严格遵循实际运动热量消耗规律：
           - 【关键原则】一个人在不死亡的情况下，持续运动的功率上限约为 3-5 倍基础代谢率
           - 普通体重（60-80kg）的人，力量训练实际消耗约为每分钟 4-8 kcal，绝不能超过 10 kcal/分钟
           - 参考常见运动的实际消耗：
             * 力量训练（深蹲、硬拉等）：约 5-7 kcal/分钟
             * 高强度间歇（波比跳、HIIT）：约 8-10 kcal/分钟
             * 中等强度（俯卧撑、卷腹）：约 6-8 kcal/分钟
             * 低强度（拉伸、瑜伽）：约 3-5 kcal/分钟
           - 【计算公式】热量消耗 = 实际做功时间（分钟）× 该类型平均消耗（kcal/分钟）
           - 记住：用户训练的大部分时间是组间休息！力量训练实际做功时间仅占总时长的 30%-40%！
           - 【硬性限制】无论什么运动，1小时训练消耗绝不能超过 500 kcal，30分钟绝不能超过 250 kcal
        4. 运动次数与时长的比例必须符合真实生理极限，且必须扣除组间歇时间：
           【关键】用户训练的时长中包含了大量组间休息时间，实际做功时间远少于总时长！
           - 力量训练（深蹲、硬拉、箭步蹲等）：每做 1 组（8-15次），需休息 60-120 秒
             实际做功时间仅占总时长的 30%-40%
             例：20 分钟深蹲训练，实际做功约 7-8 分钟，有效次数约 70-100 次
           - 中等强度（俯卧撑、卷腹等）：组间歇 45-90 秒
             实际做功时间约占总时长的 40%-50%
             例：15 分钟俯卧撑，实际做功约 6-8 分钟，有效次数约 90-150 次
           - 高强度间歇（波比跳、HIIT等）：运动与休息比约 1:1 或 2:1
             实际做功时间约占总时长的 50%-65%
             例：20 分钟 HIIT，实际运动约 10-13 分钟
           - 低强度持续性运动（拉伸、瑜伽、散步）：几乎无组间歇
             实际做功时间约占总时长的 80%-95%
           
           validReps 的计算公式：先算实际做功分钟 = 总分钟 × 做功占比，再做功分钟 × 每分钟次数
           validReps 绝不能超过这个估算值！
        5. 评价多样：comment 需根据 grade(S/A/B/C) 给出专业的教练点评，
           S级：极致夸赞，细节丰富
           A级：积极肯定，给出提升建议
           B级：鼓励为主，指出改进方向
           C级：温和建议，强调基础训练
           点评要具体到运动项目，不要泛泛而谈。
        6. 雷达图五维（accuracy精准度, power力量, stamina耐力, rhythm节奏, range幅度）
           评分应与运动项目特征匹配，如瑜伽的range应偏高，举重的power应偏高。
        7. 必须严格返回 JSON，不要任何解释文字。
        8. score 范围 60-100，对应 grade：S(≥90), A(≥80), B(≥70), C(<70)。
        
        示例格式：
        {
          "score": 92,
          "grade": "S",
          "validReps": 85,
          "invalidReps": 3,
          "durationSeconds": 1200,
          "caloriesBurned": 140,
          "comment": "深蹲训练表现完美！组间休息控制得当，下蹲深度标准，节奏稳定。",
          "radar": {"accuracy":95, "power":88, "stamina":90, "rhythm":92, "range":85}
        }
        """)
    @UserMessage("请生成一份运动战报。项目：{{workoutName}}，大致时长：{{minutes}}分钟左右。")
    WorkoutReportVO generateReport(@V("workoutName") String workoutName, @V("minutes") int minutes);
}


