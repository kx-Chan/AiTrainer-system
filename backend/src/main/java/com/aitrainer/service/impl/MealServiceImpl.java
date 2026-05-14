package com.aitrainer.service.impl;

import com.aitrainer.agent.ExerciseAgent;
import com.aitrainer.agent.NutritionAgent;
import com.aitrainer.common.constant.MessageConstant;
import com.aitrainer.common.exception.BusinessException;
import com.aitrainer.dto.AddExtraExerciseDTO;
import com.aitrainer.dto.AddMealDTO;
import com.aitrainer.dto.AnalyzeExerciseDTO;
import com.aitrainer.dto.AnalyzeFoodDTO;
import com.aitrainer.dto.UpdateExtraExerciseDTO;
import com.aitrainer.dto.UpdateMealDTO;
import com.aitrainer.vo.ExerciseAnalysisVO;
import com.aitrainer.vo.FoodAnalysisVO;
import com.aitrainer.entity.ExtraExercise;
import com.aitrainer.entity.Meal;
import com.aitrainer.entity.UserProfile;
import com.aitrainer.mapper.ExtraExerciseMapper;
import com.aitrainer.mapper.MealMapper;
import com.aitrainer.mapper.UserProfileMapper;
import com.aitrainer.service.MealService;
import com.aitrainer.vo.DietSummaryVO;
import com.aitrainer.vo.ExtraExerciseVO;
import com.aitrainer.vo.MealVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    private final MealMapper mealMapper;
    private final UserProfileMapper userProfileMapper;
    private final ExtraExerciseMapper extraExerciseMapper;
    private final NutritionAgent nutritionAgent;
    private final ExerciseAgent exerciseAgent;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FOOD_CACHE_PREFIX = "food_cache:";
    private static final long FOOD_CACHE_TTL_HOURS = 24;

    private static final Map<String, String> MEAL_TYPE_CN = Map.of(
            "breakfast", "早餐",
            "lunch", "午餐",
            "dinner", "晚餐",
            "snack", "加餐"
    );

    private static final Map<String, String> MEAL_TAG_TYPE = Map.of(
            "breakfast", "info",
            "lunch", "success",
            "dinner", "",
            "snack", "warning"
    );

    @Override
    public DietSummaryVO getDietSummary(final Long userId, final String dateStr) {
        final String date = StringUtils.hasText(dateStr) ? dateStr : LocalDate.now().toString();
        final LocalDate localDate = LocalDate.parse(date);

        // 1. 查询当日饮食记录
        final LocalDateTime dayStart = localDate.atStartOfDay();
        final LocalDateTime dayEnd = localDate.atTime(LocalTime.MAX);

        final List<Meal> meals = mealMapper.selectList(
                new LambdaQueryWrapper<Meal>()
                        .eq(Meal::getUserId, userId)
                        .ge(Meal::getMealTime, dayStart)
                        .le(Meal::getMealTime, dayEnd)
                        .orderByAsc(Meal::getMealTime)
        );

        // 2. 转换为 VO
        final List<MealVO> mealVOs = meals.stream().map(this::convertToVO).toList();

        // 3. 计算摄入总热量
        final int totalIntake = meals.stream()
                .mapToInt(m -> m.getCalories() == null ? 0 : m.getCalories())
                .sum();

        // 4. 获取用户资料，计算 BMR
        final UserProfile profile = userProfileMapper.selectById(userId);
        final int bmr = calculateBMR(profile);
        final String goal = profile != null ? profile.getGoal() : "maintain";

        // 5. 获取训练消耗
        final int workoutBurned = mealMapper.sumWorkoutCaloriesByDate(userId, date);

        // 6. 获取用户自填额外消耗
        final List<ExtraExercise> extraExercises = extraExerciseMapper.selectList(
                new LambdaQueryWrapper<ExtraExercise>()
                        .eq(ExtraExercise::getUserId, userId)
                        .eq(ExtraExercise::getExerciseDate, localDate)
                        .orderByAsc(ExtraExercise::getCreatedAt)
        );
        final int extraBurned = extraExercises.stream()
                .mapToInt(e -> e.getCaloriesBurned() == null ? 0 : e.getCaloriesBurned())
                .sum();
        final List<ExtraExerciseVO> extraExerciseVOs = extraExercises.stream()
                .map(this::convertExtraExerciseToVO).toList();

        // 6.1 获取训练消耗明细
        final List<Map<String, Object>> workoutDetailMaps = mealMapper.selectWorkoutDetailsByDate(userId, date);
        final List<DietSummaryVO.WorkoutBurnedDetailVO> workoutDetails = workoutDetailMaps.stream()
                .map(m -> DietSummaryVO.WorkoutBurnedDetailVO.builder()
                        .workoutName(m.get("workoutName") != null ? m.get("workoutName").toString() : "未知项目")
                        .caloriesBurned(m.get("caloriesBurned") != null ? ((Number) m.get("caloriesBurned")).intValue() : 0)
                        .durationMinutes(m.get("durationSeconds") != null ? ((Number) m.get("durationSeconds")).intValue() / 60 : 0)
                        .createdAt(m.get("createdAt") != null ? m.get("createdAt").toString() : "")
                        .build())
                .toList();

        // 7. 计算目标热量 = BMR + 运动消耗 + 目标调整
        final int targetCalories = calculateTargetCalories(bmr, workoutBurned + extraBurned, goal);

        // 8. 剩余可摄入
        final int remaining = targetCalories - totalIntake;

        // 9. 收集当日已使用的餐次类型
        final Set<String> usedMealTypes = meals.stream()
                .map(Meal::getMealType)
                .collect(Collectors.toSet());

        return DietSummaryVO.builder()
                .date(date)
                .meals(mealVOs)
                .totalIntakeCalories(totalIntake)
                .bmrCalories(bmr)
                .workoutBurnedCalories(workoutBurned)
                .extraBurnedCalories(extraBurned)
                .targetCalories(targetCalories)
                .remainingCalories(remaining)
                .goal(goal)
                .usedMealTypes(usedMealTypes)
                .extraExercises(extraExerciseVOs)
                .workoutDetails(workoutDetails)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MealVO addMeal(final Long userId, final AddMealDTO dto) {
        final String date = StringUtils.hasText(dto.date()) ? dto.date() : LocalDate.now().toString();
        final LocalDate localDate = LocalDate.parse(date);
        final LocalTime localTime = LocalTime.parse(dto.mealTime(), DateTimeFormatter.ofPattern("HH:mm"));
        final LocalDateTime mealTime = LocalDateTime.of(localDate, localTime);

        // 允许同一餐次添加多条菜品记录（前端会合并显示）
        final Meal meal = Meal.builder()
                .userId(userId)
                .mealTime(mealTime)
                .mealType(dto.mealType())
                .foodName(dto.foodName())
                .calories(dto.calories())
                .protein(dto.protein())
                .fat(dto.fat())
                .carbs(dto.carbs())
                .weight(dto.weight())
                .tagType(MEAL_TAG_TYPE.getOrDefault(dto.mealType(), "info"))
                .isDeleted(0)
                .createdAt(LocalDateTime.now())
                .build();

        mealMapper.insert(meal);
        log.info("用户 {} 添加饮食记录: {} - {} ({}kcal)", userId, dto.mealType(), dto.foodName(), dto.calories());

        return convertToVO(meal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MealVO updateMeal(final Long userId, final Long mealId, final UpdateMealDTO dto) {
        final Meal meal = mealMapper.selectById(mealId);
        if (meal == null) {
            throw BusinessException.notFound(MessageConstant.MEAL_NOT_FOUND);
        }
        if (!meal.getUserId().equals(userId)) {
            throw BusinessException.forbidden(MessageConstant.MEAL_UPDATE_FORBIDDEN);
        }

        // 按需更新字段
        if (dto.foodName() != null) {
            meal.setFoodName(dto.foodName());
        }
        if (dto.calories() != null) {
            meal.setCalories(dto.calories());
        }
        if (dto.protein() != null) {
            meal.setProtein(dto.protein());
        }
        if (dto.fat() != null) {
            meal.setFat(dto.fat());
        }
        if (dto.carbs() != null) {
            meal.setCarbs(dto.carbs());
        }
        if (dto.weight() != null) {
            meal.setWeight(dto.weight());
        }
        if (dto.mealTime() != null) {
            final LocalTime localTime = LocalTime.parse(dto.mealTime(), DateTimeFormatter.ofPattern("HH:mm"));
            meal.setMealTime(LocalDateTime.of(meal.getMealTime().toLocalDate(), localTime));
        }

        mealMapper.updateById(meal);
        log.info("用户 {} 编辑饮食记录 {}: {} ({}kcal)", userId, mealId, meal.getFoodName(), meal.getCalories());

        return convertToVO(meal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMeal(final Long userId, final Long mealId) {
        final Meal meal = mealMapper.selectById(mealId);
        if (meal == null) {
            throw BusinessException.notFound(MessageConstant.MEAL_NOT_FOUND);
        }
        if (!meal.getUserId().equals(userId)) {
            throw BusinessException.forbidden(MessageConstant.MEAL_DELETE_FORBIDDEN);
        }
        // MyBatis-Plus @TableLogic 会自动执行逻辑删除
        mealMapper.deleteById(mealId);
        log.info("用户 {} 删除饮食记录 {}", userId, mealId);
    }

    /**
     * 计算基础代谢率 BMR (Mifflin-St Jeor 公式)。
     * 男: BMR = 10 × 体重(kg) + 6.25 × 身高(cm) - 5 × 年龄 + 5
     * 女: BMR = 10 × 体重(kg) + 6.25 × 身高(cm) - 5 × 年龄 - 161
     * 简化版（不含年龄）：取中间值
     */
    private int calculateBMR(final UserProfile profile) {
        if (profile == null || profile.getHeight() == null || profile.getWeight() == null) {
            return 1500; // 默认值
        }
        final double weight = profile.getWeight().doubleValue();
        final int height = profile.getHeight();
        final String gender = profile.getGender();
        final int age = (profile.getAge() != null && profile.getAge() > 0) ? profile.getAge() : 25; // 如果未设置年龄则默认 25

        // Mifflin-St Jeor 公式
        if ("female".equals(gender)) {
            return (int) (10 * weight + 6.25 * height - 5 * age - 161);
        }
        return (int) (10 * weight + 6.25 * height - 5 * age + 5);
    }

    /**
     * 根据健身目标计算每日应吃热量（目标摄入）。
     *
     * 核心原理：
     * 1. 基础代谢（BMR）约占无运动情况下总热量消耗的 70%，
     *    因此 无运动总消耗 = BMR / 0.7（包含食物热效应和日常活动消耗）。
     * 2. 理论平衡热量 = BMR / 0.7 + 运动消耗（训练+额外运动）。
     * 3. 研究表明，人们在定量饮食中会不自觉多摄入约 10%~30%（取 20%），
     *    因此实际应吃热量需在理论值基础上乘以 0.8 来抵消这一偏差。
     *
     * 按目标调整：
     * - maintain（保持身材）：应吃 = 平衡热量 × 0.8（抵消无意识多吃 20%）
     * - lose（减脂）：合适热量缺口 10%~30%（取 20%），应吃 = 平衡热量 × 0.80 × 0.80 = 平衡热量 × 0.64
     * - gain（增肌）：干净增肌盈余 5%~10%（取 10%），应吃 = 平衡热量 × 1.10 × 0.80 = 平衡热量 × 0.88
     */
    private int calculateTargetCalories(final int bmr, final int exerciseBurned, final String goal) {
        // 1. 算出基于 BMR 的基础平衡热量（不含当天额外运动的静态平衡）
        final double baseBalance = bmr / 0.7;

        // 2. 根据目标确定系数
        // 这里保留你之前的逻辑：系数 = 目标调整(0.8/1.1/1.0) * 吸收折算(0.8)
        double multiplier = switch (goal != null ? goal : "maintain") {
            case "lose" -> 0.8 * 0.8;      // 减脂目标 80%
            case "gain" -> 1.1 * 0.8;      // 增肌目标 110%
            default -> 1.0 * 0.8;          // 保持目标 100%
        };

        // 3. 核心修正：(基础平衡 * 系数) + 运动损耗
        // 运动损耗 exerciseBurned 不再参与 multiplier 的乘法
        return (int) (baseBalance * multiplier) + exerciseBurned;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExtraExerciseVO addExtraExercise(final Long userId, final AddExtraExerciseDTO dto) {
        final String date = StringUtils.hasText(dto.date()) ? dto.date() : LocalDate.now().toString();
        final LocalDate localDate = LocalDate.parse(date);

        final ExtraExercise exercise = ExtraExercise.builder()
                .userId(userId)
                .exerciseName(dto.exerciseName())
                .description(dto.description())
                .caloriesBurned(dto.caloriesBurned())
                .durationMinutes(dto.durationMinutes())
                .exerciseDate(localDate)
                .createdAt(LocalDateTime.now())
                .build();

        extraExerciseMapper.insert(exercise);
        log.info("用户 {} 添加额外运动: {} ({}kcal)", userId, dto.exerciseName(), dto.caloriesBurned());

        return convertExtraExerciseToVO(exercise);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExtraExerciseVO updateExtraExercise(final Long userId, final Long exerciseId, final UpdateExtraExerciseDTO dto) {
        final ExtraExercise exercise = extraExerciseMapper.selectById(exerciseId);
        if (exercise == null) {
            throw BusinessException.notFound(MessageConstant.EXTRA_EXERCISE_NOT_FOUND);
        }
        if (!exercise.getUserId().equals(userId)) {
            throw BusinessException.forbidden(MessageConstant.EXTRA_EXERCISE_UPDATE_FORBIDDEN);
        }

        if (dto.exerciseName() != null) {
            exercise.setExerciseName(dto.exerciseName());
        }
        if (dto.description() != null) {
            exercise.setDescription(dto.description());
        }
        if (dto.caloriesBurned() != null) {
            exercise.setCaloriesBurned(dto.caloriesBurned());
        }
        if (dto.durationMinutes() != null) {
            exercise.setDurationMinutes(dto.durationMinutes());
        }

        extraExerciseMapper.updateById(exercise);
        log.info("用户 {} 编辑额外运动记录 {}: {} ({}kcal)", userId, exerciseId, exercise.getExerciseName(), exercise.getCaloriesBurned());

        return convertExtraExerciseToVO(exercise);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExtraExercise(final Long userId, final Long exerciseId) {
        final ExtraExercise exercise = extraExerciseMapper.selectById(exerciseId);
        if (exercise == null) {
            throw BusinessException.notFound(MessageConstant.EXTRA_EXERCISE_NOT_FOUND);
        }
        if (!exercise.getUserId().equals(userId)) {
            throw BusinessException.forbidden(MessageConstant.EXTRA_EXERCISE_DELETE_FORBIDDEN);
        }
        extraExerciseMapper.deleteById(exerciseId);
        log.info("用户 {} 删除额外运动记录 {}", userId, exerciseId);
    }

    /**
     * 额外运动实体转 VO。
     */
    private ExtraExerciseVO convertExtraExerciseToVO(final ExtraExercise exercise) {
        return ExtraExerciseVO.builder()
                .id(exercise.getId())
                .exerciseName(exercise.getExerciseName())
                .description(exercise.getDescription())
                .caloriesBurned(exercise.getCaloriesBurned())
                .durationMinutes(exercise.getDurationMinutes())
                .exerciseDate(exercise.getExerciseDate() != null ? exercise.getExerciseDate().toString() : "")
                .createdAt(exercise.getCreatedAt())
                .build();
    }

    @Override
    public FoodAnalysisVO analyzeFood(final Long userId, final AnalyzeFoodDTO dto) {
        final String foodName = dto.foodName();
        final int weight = dto.weight() != null ? dto.weight() : 100;

        if (foodName == null || foodName.trim().isEmpty()) {
            return FoodAnalysisVO.builder()
                    .calories(0)
                    .protein(0)
                    .fat(0)
                    .carbs(0)
                    .build();
        }

        // 1. 构建缓存 key: food_cache:食物名称:重量
        final String cacheKey = FOOD_CACHE_PREFIX + foodName.trim() + ":" + weight;

        // 2. 先查缓存
        try {
            final Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && cached instanceof FoodAnalysisVO cachedVO) {
                log.info("缓存命中: {} -> 热量:{} 蛋白质:{} 脂肪:{} 碳水:{}",
                        foodName, cachedVO.getCalories(), cachedVO.getProtein(),
                        cachedVO.getFat(), cachedVO.getCarbs());
                return cachedVO;
            }
        } catch (Exception e) {
            log.warn("Redis 缓存读取失败，降级到 AI 分析: {}", e.getMessage());
        }

        // 3. 缓存没有，调用 AI Agent 进行分析
        log.info("缓存未命中，调用 AI 分析: {} ({}g)", foodName, weight);
        FoodAnalysisVO result;
        try {
            result = nutritionAgent.analyze(foodName, weight);
        } catch (Exception e) {
            log.error("AI 食物分析失败 (可能是 API 代理返回了流式 SSE 格式而非 JSON)，使用估算值: {}", e.getMessage());
            // 降级：根据常见食物给出粗略估算值
            result = FoodAnalysisVO.builder()
                    .calories((int) (weight * 1.5))
                    .protein((int) (weight * 0.1))
                    .fat((int) (weight * 0.05))
                    .carbs((int) (weight * 0.2))
                    .build();
        }

        // 4. 存入 Redis 缓存，设置 24 小时过期
        try {
            redisTemplate.opsForValue().set(cacheKey, result, FOOD_CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.info("AI 分析结果已缓存: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Redis 缓存写入失败，跳过缓存: {}", e.getMessage());
        }

        return result;
    }

    @Override
    public ExerciseAnalysisVO analyzeExercise(final Long userId, final AnalyzeExerciseDTO dto) {
        final String exerciseName = dto.exerciseName();
        final int durationMinutes = dto.durationMinutes() != null ? dto.durationMinutes() : 30;

        if (exerciseName == null || exerciseName.trim().isEmpty()) {
            return ExerciseAnalysisVO.builder()
                    .caloriesBurned(0)
                    .intensity("未知")
                    .build();
        }

        // 获取用户身体数据，传入 AI Agent
        final UserProfile profile = userProfileMapper.selectById(userId);
        final int age = (profile != null && profile.getAge() != null && profile.getAge() > 0) ? profile.getAge() : 25;
        final int height = (profile != null && profile.getHeight() != null) ? profile.getHeight() : 170;
        final double weight = (profile != null && profile.getWeight() != null) ? profile.getWeight().doubleValue() : 65.0;
        final String gender = (profile != null && profile.getGender() != null) ? profile.getGender() : "male";

        // 获取运动描述，传入 AI Agent 以更精准地评估运动强度
        final String description = StringUtils.hasText(dto.description()) ? dto.description() : "无";

        log.info("调用 ExerciseAgent 分析运动消耗: {} ({}分钟), 描述: {}, 用户信息: {}岁, {}cm, {}kg, {}",
                exerciseName, durationMinutes, description, age, height, weight, gender);

        // 调用 AI Agent 进行运动消耗分析
        ExerciseAnalysisVO result;
        try {
            result = exerciseAgent.analyze(exerciseName, durationMinutes, description, age, height, weight, gender);
        } catch (Exception e) {
            log.error("AI 运动消耗分析失败 (可能是 API 代理返回了流式 SSE 格式而非 JSON)，使用估算值: {}", e.getMessage());
            // 降级：按 MET 粗略估算 (中等强度 ~5 MET)
            int estimatedCalories = (int) (5.0 * weight * durationMinutes / 60.0);
            result = ExerciseAnalysisVO.builder()
                    .caloriesBurned(estimatedCalories)
                    .intensity("中等")
                    .build();
        }

        log.info("AI Agent 分析运动消耗结果: {} -> {}kcal ({})", exerciseName, result.getCaloriesBurned(), result.getIntensity());
        return result;
    }

    /**
     * 饮食实体转 VO。
     */
    private MealVO convertToVO(final Meal meal) {
        final String timeStr = meal.getMealTime() != null
                ? meal.getMealTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "";
        return MealVO.builder()
                .id(meal.getId())
                .time(timeStr)
                .type(MEAL_TYPE_CN.getOrDefault(meal.getMealType(), meal.getMealType()))
                .mealType(meal.getMealType())
                .foodName(meal.getFoodName())
                .calories(meal.getCalories())
                .protein(meal.getProtein())
                .fat(meal.getFat())
                .carbs(meal.getCarbs())
                .weight(meal.getWeight())
                .tagType(meal.getTagType())
                .createdAt(meal.getCreatedAt())
                .build();
    }
}
