package com.aitrainer.service.impl;

import com.aitrainer.agent.AiCoachComprehensiveAgent;
import com.aitrainer.dto.AiCoachAnalyzeRequestDTO;
import com.aitrainer.entity.ExtraExercise;
import com.aitrainer.entity.Meal;
import com.aitrainer.entity.UserProfile;
import com.aitrainer.entity.WorkoutSession;
import com.aitrainer.entity.Workout;
import com.aitrainer.mapper.ExtraExerciseMapper;
import com.aitrainer.mapper.MealMapper;
import com.aitrainer.mapper.UserProfileMapper;
import com.aitrainer.mapper.WorkoutMapper;
import com.aitrainer.mapper.WorkoutSessionMapper;
import com.aitrainer.service.AiCoachChatHistoryService;
import com.aitrainer.service.AiCoachService;
import com.aitrainer.vo.AiCoachAnalyzeResponseVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI 私教服务实现类。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiCoachServiceImpl implements AiCoachService {

    private final AiCoachComprehensiveAgent aiCoachComprehensiveAgent;
    private final WorkoutSessionMapper workoutSessionMapper;
    private final ExtraExerciseMapper extraExerciseMapper;
    private final MealMapper mealMapper;
    private final UserProfileMapper userProfileMapper;
    private final WorkoutMapper workoutMapper;
    private final AiCoachChatHistoryService chatHistoryService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 默认保留的历史消息数量。
     */
    private static final int DEFAULT_HISTORY_LIMIT = 20;

    @Override
    public AiCoachAnalyzeResponseVO analyze(final Long userId, final AiCoachAnalyzeRequestDTO dto) {
        // 0. 处理会话 ID
        String sessionId = dto.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = generateSessionId();
        }

        // 1. 收集用户基本信息
        final UserProfile profile = userProfileMapper.selectById(userId);
        final String profileDataSummary = buildProfileSummary(profile);

        // 2. 收集训练数据（如果需要）
        String trainingDataSummary = "";
        if (Boolean.TRUE.equals(dto.getIncludeTrainingData()) && dto.getTrainingDays() != null) {
            trainingDataSummary = buildTrainingDataSummary(userId, dto.getTrainingDays());
        }

        // 3. 收集饮食数据（如果需要）
        String dietDataSummary = "";
        if (Boolean.TRUE.equals(dto.getIncludeDietData()) && dto.getDietDays() != null) {
            dietDataSummary = buildDietDataSummary(userId, dto.getDietDays());
        }

        // 4. 获取历史对话上下文
        final String chatHistoryContext = chatHistoryService.formatChatHistory(userId, sessionId, DEFAULT_HISTORY_LIMIT);
        
        // 5. 构建完整的问题上下文
        final String fullQuestion = buildFullQuestion(dto.getQuestion(), chatHistoryContext);

        // 6. 根据分析类型调用对应的 Agent 方法
        String analysisResult;
        final String analysisType = dto.getAnalysisType();
        try {
            if ("training".equals(analysisType)) {
                // 只在用户勾选了训练数据时才传入实际数据，否则传空字符串避免强调数据缺失
                final String trainingData = Boolean.TRUE.equals(dto.getIncludeTrainingData()) 
                        ? trainingDataSummary 
                        : "";
                analysisResult = aiCoachComprehensiveAgent.analyzeTraining(
                        fullQuestion,
                        trainingData
                );
            } else if ("diet".equals(analysisType)) {
                // 只在用户勾选了饮食数据时才传入实际数据，否则传空字符串避免强调数据缺失
                final String dietData = Boolean.TRUE.equals(dto.getIncludeDietData()) 
                        ? dietDataSummary 
                        : "";
                analysisResult = aiCoachComprehensiveAgent.analyzeDiet(
                        fullQuestion,
                        dietData
                );
            } else {
                // 默认综合分析
                // 只在用户勾选了对应数据时才传入实际数据，否则传空字符串避免强调数据缺失
                final String trainingData = Boolean.TRUE.equals(dto.getIncludeTrainingData()) 
                        ? trainingDataSummary 
                        : "";
                final String dietData = Boolean.TRUE.equals(dto.getIncludeDietData()) 
                        ? dietDataSummary 
                        : "";
                analysisResult = aiCoachComprehensiveAgent.analyzeComprehensive(
                        fullQuestion,
                        trainingData,
                        dietData,
                        profileDataSummary
                );
            }
        } catch (final Exception e) {
            log.error("AI 分析失败", e);
            analysisResult = "抱歉，分析过程中出现了一些问题。请稍后再试或简化您的问题。";
        }

        // 7. 保存对话历史
        try {
            chatHistoryService.saveMessage(userId, sessionId, "user", dto.getQuestion(), analysisType);
            chatHistoryService.saveMessage(userId, sessionId, "assistant", analysisResult, analysisType);
        } catch (final Exception e) {
            log.error("保存聊天历史失败", e);
        }

        // 8. 构建响应
        return AiCoachAnalyzeResponseVO.builder()
                .analysisType(dto.getAnalysisType())
                .analysisResult(analysisResult)
                .trainingDataSummary(trainingDataSummary)
                .dietDataSummary(dietDataSummary)
                .profileDataSummary(profileDataSummary)
                .sessionId(sessionId)
                .build();
    }

    /**
     * 生成会话 ID。
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 构建完整的问题，包含历史上下文。
     */
    private String buildFullQuestion(final String currentQuestion, final String chatHistoryContext) {
        final StringBuilder sb = new StringBuilder();
        
        if (chatHistoryContext != null && !chatHistoryContext.isEmpty()) {
            sb.append(chatHistoryContext).append("\n\n");
        }
        
        sb.append("【当前问题】\n").append(currentQuestion);
        
        return sb.toString();
    }

    /**
     * 构建用户基本信息摘要。
     */
    private String buildProfileSummary(final UserProfile profile) {
        if (profile == null) {
            return "用户信息：暂无数据";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("用户基本信息：\n");

        if (profile.getHeight() != null) {
            sb.append("- 身高：").append(profile.getHeight()).append("cm\n");
        }
        if (profile.getWeight() != null) {
            sb.append("- 体重：").append(profile.getWeight()).append("kg\n");
        }
        if (profile.getAge() != null) {
            sb.append("- 年龄：").append(profile.getAge()).append("岁\n");
        }
        if (profile.getGender() != null) {
            sb.append("- 性别：").append("male".equals(profile.getGender()) ? "男" : "女").append("\n");
        }
        if (profile.getGoal() != null) {
            final String goalDesc = switch (profile.getGoal()) {
                case "lose" -> "减脂";
                case "gain" -> "增肌";
                case "maintain" -> "保持身材";
                default -> profile.getGoal();
            };
            sb.append("- 健身目标：").append(goalDesc).append("\n");
        }

        return sb.toString();
    }

    /**
     * 构建训练数据摘要。
     */
    private String buildTrainingDataSummary(final Long userId, final int days) {
        final LocalDate today = LocalDate.now();
        final LocalDate startDate = today.minusDays(days - 1);
        final LocalDateTime startDateTime = startDate.atStartOfDay();
        final LocalDateTime endDateTime = today.plusDays(1).atStartOfDay();

        // 1. 查询项目训练记录
        final List<WorkoutSession> workoutSessions = workoutSessionMapper.selectList(
                new LambdaQueryWrapper<WorkoutSession>()
                        .eq(WorkoutSession::getUserId, userId)
                        .ge(WorkoutSession::getCreatedAt, startDateTime)
                        .le(WorkoutSession::getCreatedAt, endDateTime)
                        .orderByDesc(WorkoutSession::getCreatedAt)
        );

        // 获取项目名称映射
        final List<Workout> workouts = workoutMapper.selectList(null);
        final Map<String, String> workoutNameMap = workouts.stream()
                .collect(Collectors.toMap(Workout::getId, Workout::getName, (a, b) -> a));

        // 2. 查询额外运动记录
        final List<ExtraExercise> extraExercises = extraExerciseMapper.selectList(
                new LambdaQueryWrapper<ExtraExercise>()
                        .eq(ExtraExercise::getUserId, userId)
                        .ge(ExtraExercise::getExerciseDate, startDate)
                        .le(ExtraExercise::getExerciseDate, today)
                        .orderByDesc(ExtraExercise::getCreatedAt)
        );

        // 3. 构建摘要
        final StringBuilder sb = new StringBuilder();
        sb.append("训练数据（近").append(days).append("天）：\n\n");

        // 项目训练统计
        sb.append("【项目训练】\n");
        if (workoutSessions.isEmpty()) {
            sb.append("- 暂无项目训练记录\n");
        } else {
            // 按项目分组统计
            final Map<String, List<WorkoutSession>> groupedByWorkout = workoutSessions.stream()
                    .collect(Collectors.groupingBy(WorkoutSession::getWorkoutId));

            final int totalCalories = workoutSessions.stream()
                    .mapToInt(ws -> ws.getCaloriesBurned() != null ? ws.getCaloriesBurned() : 0)
                    .sum();
            final int totalValidReps = workoutSessions.stream()
                    .mapToInt(ws -> ws.getValidReps() != null ? ws.getValidReps() : 0)
                    .sum();
            final int totalDuration = workoutSessions.stream()
                    .mapToInt(ws -> ws.getDurationSeconds() != null ? ws.getDurationSeconds() : 0)
                    .sum();

            sb.append("- 训练总次数：").append(workoutSessions.size()).append("次\n");
            sb.append("- 消耗总热量：").append(totalCalories).append("kcal\n");
            sb.append("- 有效动作总数：").append(totalValidReps).append("次\n");
            sb.append("- 训练总时长：").append(formatDuration(totalDuration)).append("\n\n");

            sb.append("各项目详情：\n");
            for (final Map.Entry<String, List<WorkoutSession>> entry : groupedByWorkout.entrySet()) {
                final String workoutName = workoutNameMap.getOrDefault(entry.getKey(), entry.getKey());
                final List<WorkoutSession> sessions = entry.getValue();
                final int sessionCount = sessions.size();
                final int avgScore = (int) sessions.stream()
                        .filter(ws -> ws.getScore() != null)
                        .mapToInt(WorkoutSession::getScore)
                        .average()
                        .orElse(0);
                final int sessionCalories = sessions.stream()
                        .mapToInt(ws -> ws.getCaloriesBurned() != null ? ws.getCaloriesBurned() : 0)
                        .sum();
                final int sessionReps = sessions.stream()
                        .mapToInt(ws -> ws.getValidReps() != null ? ws.getValidReps() : 0)
                        .sum();

                sb.append("- ").append(workoutName).append("：")
                        .append(sessionCount).append("次，")
                        .append("平均评分").append(avgScore).append("分，")
                        .append("消耗").append(sessionCalories).append("kcal，")
                        .append("有效动作").append(sessionReps).append("次\n");
            }
        }

        // 额外运动统计
        sb.append("\n【额外运动】\n");
        if (extraExercises.isEmpty()) {
            sb.append("- 暂无额外运动记录\n");
        } else {
            final int totalExtraCalories = extraExercises.stream()
                    .mapToInt(ee -> ee.getCaloriesBurned() != null ? ee.getCaloriesBurned() : 0)
                    .sum();
            final int totalExtraDuration = extraExercises.stream()
                    .mapToInt(ee -> ee.getDurationMinutes() != null ? ee.getDurationMinutes() : 0)
                    .sum();

            sb.append("- 运动总次数：").append(extraExercises.size()).append("次\n");
            sb.append("- 消耗总热量：").append(totalExtraCalories).append("kcal\n");
            sb.append("- 运动总时长：").append(totalExtraDuration).append("分钟\n\n");

            sb.append("运动详情：\n");
            for (final ExtraExercise ee : extraExercises) {
                sb.append("- ").append(ee.getExerciseName())
                        .append("（").append(ee.getExerciseDate()).append("）：")
                        .append(ee.getDurationMinutes() != null ? ee.getDurationMinutes() + "分钟" : "")
                        .append("，消耗").append(ee.getCaloriesBurned() != null ? ee.getCaloriesBurned() : 0).append("kcal");
                if (ee.getDescription() != null && !ee.getDescription().isEmpty()) {
                    sb.append("，").append(ee.getDescription());
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 构建饮食数据摘要。
     */
    private String buildDietDataSummary(final Long userId, final int days) {
        final LocalDate today = LocalDate.now();
        final LocalDate startDate = today.minusDays(days - 1);
        final LocalDateTime startDateTime = startDate.atStartOfDay();
        final LocalDateTime endDateTime = today.plusDays(1).atStartOfDay();

        // 查询饮食记录
        final List<Meal> meals = mealMapper.selectList(
                new LambdaQueryWrapper<Meal>()
                        .eq(Meal::getUserId, userId)
                        .ge(Meal::getMealTime, startDateTime)
                        .le(Meal::getMealTime, endDateTime)
                        .orderByAsc(Meal::getMealTime)
        );

        final StringBuilder sb = new StringBuilder();
        sb.append("饮食数据（近").append(days).append("天）：\n\n");

        if (meals.isEmpty()) {
            sb.append("暂无饮食记录\n");
            return sb.toString();
        }

        // 按日期分组
        final Map<LocalDate, List<Meal>> mealsByDate = meals.stream()
                .collect(Collectors.groupingBy(m -> m.getMealTime() != null ? m.getMealTime().toLocalDate() : LocalDate.now()));

        // 总体统计
        final int totalCalories = meals.stream()
                .mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0)
                .sum();
        final int totalCarbs = meals.stream()
                .mapToInt(m -> m.getCarbs() != null ? m.getCarbs() : 0)
                .sum();
        final int totalProtein = meals.stream()
                .mapToInt(m -> m.getProtein() != null ? m.getProtein() : 0)
                .sum();
        final int totalFat = meals.stream()
                .mapToInt(m -> m.getFat() != null ? m.getFat() : 0)
                .sum();

        sb.append("【总体统计】\n");
        sb.append("- 记录天数：").append(mealsByDate.size()).append("天\n");
        sb.append("- 记录餐次：").append(meals.size()).append("餐\n");
        sb.append("- 总热量摄入：").append(totalCalories).append("kcal\n");
        sb.append("- 总碳水化合物：").append(totalCarbs).append("g\n");
        sb.append("- 总蛋白质：").append(totalProtein).append("g\n");
        sb.append("- 总脂肪：").append(totalFat).append("g\n");

        // 计算日均
        final int avgCalories = totalCalories / Math.max(mealsByDate.size(), 1);
        sb.append("- 日均热量：").append(avgCalories).append("kcal\n");

        // 营养素占比
        if (totalCalories > 0) {
            final int carbsCalories = totalCarbs * 4;
            final int proteinCalories = totalProtein * 4;
            final int fatCalories = totalFat * 9;

            sb.append("- 碳水供能比：").append(Math.round((double) carbsCalories / totalCalories * 100)).append("%\n");
            sb.append("- 蛋白质供能比：").append(Math.round((double) proteinCalories / totalCalories * 100)).append("%\n");
            sb.append("- 脂肪供能比：").append(Math.round((double) fatCalories / totalCalories * 100)).append("%\n");
        }

        // 按日期详情
        sb.append("\n【每日详情】\n");
        for (final Map.Entry<LocalDate, List<Meal>> entry : mealsByDate.entrySet()) {
            final LocalDate date = entry.getKey();
            final List<Meal> dayMeals = entry.getValue();

            final int dayCalories = dayMeals.stream()
                    .mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0)
                    .sum();

            sb.append("- ").append(date).append("：")
                    .append(dayMeals.size()).append("餐，")
                    .append("总热量").append(dayCalories).append("kcal\n");

            // 各餐次详情
            final Map<String, List<Meal>> mealsByType = dayMeals.stream()
                    .collect(Collectors.groupingBy(m -> m.getMealType() != null ? m.getMealType() : "other"));

            for (final Map.Entry<String, List<Meal>> typeEntry : mealsByType.entrySet()) {
                final String mealTypeCn = getMealTypeCn(typeEntry.getKey());
                final List<Meal> typeMeals = typeEntry.getValue();
                final int typeCalories = typeMeals.stream()
                        .mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0)
                        .sum();

                sb.append("  - ").append(mealTypeCn).append("：");
                final String foods = typeMeals.stream()
                        .map(m -> m.getFoodName() + "(" + (m.getCalories() != null ? m.getCalories() : 0) + "kcal)")
                        .collect(Collectors.joining("、"));
                sb.append(foods).append("，共").append(typeCalories).append("kcal\n");
            }
        }

        return sb.toString();
    }

    /**
     * 获取餐次中文名。
     */
    private String getMealTypeCn(final String mealType) {
        return switch (mealType) {
            case "breakfast" -> "早餐";
            case "lunch" -> "午餐";
            case "dinner" -> "晚餐";
            case "snack" -> "加餐";
            default -> "其他";
        };
    }

    /**
     * 格式化时长。
     */
    private String formatDuration(final int seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        }
        final int minutes = seconds / 60;
        final int remainingSeconds = seconds % 60;
        if (remainingSeconds == 0) {
            return minutes + "分钟";
        }
        return minutes + "分" + remainingSeconds + "秒";
    }
}