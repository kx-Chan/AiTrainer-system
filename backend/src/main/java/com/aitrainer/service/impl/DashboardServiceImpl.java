package com.aitrainer.service.impl;

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
import com.aitrainer.service.DashboardService;
import com.aitrainer.vo.DashboardCalorieVO;
import com.aitrainer.vo.DashboardNutritionVO;
import com.aitrainer.vo.DashboardTrainingLogVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据看板服务实现类。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final WorkoutSessionMapper workoutSessionMapper;
    private final ExtraExerciseMapper extraExerciseMapper;
    private final WorkoutMapper workoutMapper;
    private final MealMapper mealMapper;
    private final UserProfileMapper userProfileMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // 营养素热量系数 (kcal/g)
    private static final double CARBS_CALORIES_PER_GRAM = 4.0;
    private static final double PROTEIN_CALORIES_PER_GRAM = 4.0;
    private static final double FAT_CALORIES_PER_GRAM = 9.0;

    // 默认营养素配比（热量占比）- 用于兼容旧逻辑
    private static final int CARBS_TARGET_PERCENT = 50;
    private static final int PROTEIN_TARGET_PERCENT = 30;
    private static final int FAT_TARGET_PERCENT = 20;

    // 根据健身目标的专业营养素配比（碳水:蛋白:脂肪 热量占比）
    private static final Map<String, int[]> GOAL_NUTRITION_RATIOS = Map.of(
        "lose", new int[]{35, 35, 30},   // 减脂: 碳水40% 蛋白40% 脂肪20%
        "gain", new int[]{50, 30, 20},  // 增肌: 碳水50% 蛋白30% 脂肪20%
        "maintain", new int[]{50, 25, 25}       // 保持: 碳水55% 蛋白25% 脂肪20%
    );

    @Override
    public DashboardCalorieVO getLast7DaysCalories(final Long userId) {
        final LocalDate today = LocalDate.now();
        final List<String> dates = new ArrayList<>();
        final List<Integer> workoutCalories = new ArrayList<>();
        final List<Integer> extraExerciseCalories = new ArrayList<>();
        final List<Integer> totalCalories = new ArrayList<>();

        // 生成近七天日期并查询每日数据
        int totalWorkout = 0;
        int totalExtra = 0;

        for (int i = 6; i >= 0; i--) {
            final LocalDate date = today.minusDays(i);
            final String dateStr = date.format(DATE_FORMATTER);
            dates.add(dateStr);

            // 查询项目训练消耗
            final Integer workoutCal = mealMapper.sumWorkoutCaloriesByDate(userId, dateStr);
            final int workoutCalValue = workoutCal != null ? workoutCal : 0;
            workoutCalories.add(workoutCalValue);
            totalWorkout += workoutCalValue;

            // 查询额外运动消耗
            final LocalDate finalDate = date;
            final List<ExtraExercise> extraExercises = extraExerciseMapper.selectList(
                    new LambdaQueryWrapper<ExtraExercise>()
                            .eq(ExtraExercise::getUserId, userId)
                            .eq(ExtraExercise::getExerciseDate, finalDate)
            );
            final int extraCalValue = extraExercises.stream()
                    .mapToInt(e -> e.getCaloriesBurned() != null ? e.getCaloriesBurned() : 0)
                    .sum();
            extraExerciseCalories.add(extraCalValue);
            totalExtra += extraCalValue;

            // 计算每日总消耗
            totalCalories.add(workoutCalValue + extraCalValue);
        }

        return DashboardCalorieVO.builder()
                .dates(dates)
                .workoutCalories(workoutCalories)
                .extraExerciseCalories(extraExerciseCalories)
                .totalCalories(totalCalories)
                .totalWorkoutCalories(totalWorkout)
                .totalExtraExerciseCalories(totalExtra)
                .totalCalories7Days(totalWorkout + totalExtra)
                .build();
    }

    @Override
    public DashboardTrainingLogVO getTrainingLogs(final Long userId, final String startDateStr, final String endDateStr) {
        final LocalDate today = LocalDate.now();
        final LocalDate endDate = StringUtils.hasText(endDateStr) ? LocalDate.parse(endDateStr) : today;
        final LocalDate startDate = StringUtils.hasText(startDateStr) ? LocalDate.parse(startDateStr) : today.minusDays(6);

        // 1. 查询近七天的项目训练记录
        final List<WorkoutSession> workoutSessions = workoutSessionMapper.selectList(
                new LambdaQueryWrapper<WorkoutSession>()
                        .eq(WorkoutSession::getUserId, userId)
                        .ge(WorkoutSession::getCreatedAt, startDate.atStartOfDay())
                        .le(WorkoutSession::getCreatedAt, today.plusDays(1).atStartOfDay())
                        .orderByDesc(WorkoutSession::getCreatedAt)
        );

        // 获取所有项目名称映射
        final List<Workout> workouts = workoutMapper.selectList(null);
        final Map<String, String> workoutNameMap = workouts.stream()
                .collect(Collectors.toMap(Workout::getId, Workout::getName, (a, b) -> a));

        // 转换项目训练日志
        final List<DashboardTrainingLogVO.TrainingLogItemVO> workoutLogs = workoutSessions.stream()
                .map(ws -> DashboardTrainingLogVO.TrainingLogItemVO.builder()
                        .id(ws.getId())
                        .workoutId(ws.getWorkoutId())
                        .workoutName(workoutNameMap.getOrDefault(ws.getWorkoutId(), ws.getWorkoutId()))
                        .score(ws.getScore())
                        .grade(ws.getGrade())
                        .validReps(ws.getValidReps())
                        .durationMinutes(ws.getDurationMinutes())
                        .caloriesBurned(ws.getCaloriesBurned())
                        .comment(ws.getComment())
                        .createdAt(ws.getCreatedAt() != null ? ws.getCreatedAt().format(DATE_FORMATTER) : "")
                        .build())
                .toList();

        // 2. 查询近七天的额外运动记录
        final List<ExtraExercise> extraExercises = extraExerciseMapper.selectList(
                new LambdaQueryWrapper<ExtraExercise>()
                        .eq(ExtraExercise::getUserId, userId)
                        .ge(ExtraExercise::getExerciseDate, startDate)
                        .le(ExtraExercise::getExerciseDate, today)
                        .orderByDesc(ExtraExercise::getCreatedAt)
        );

        final List<DashboardTrainingLogVO.ExtraExerciseLogItemVO> extraExerciseLogs = extraExercises.stream()
                .map(ee -> DashboardTrainingLogVO.ExtraExerciseLogItemVO.builder()
                        .id(ee.getId())
                        .exerciseName(ee.getExerciseName())
                        .caloriesBurned(ee.getCaloriesBurned())
                        .durationMinutes(ee.getDurationMinutes())
                        .exerciseDate(ee.getExerciseDate() != null ? ee.getExerciseDate().format(DATE_FORMATTER) : "")
                        .createdAt(ee.getCreatedAt() != null ? ee.getCreatedAt().format(DATE_FORMATTER) : "")
                        .build())
                .toList();

        // 3. 查询饮食记录并按(日期+餐次)聚合
        final List<Meal> meals = mealMapper.selectList(
                new LambdaQueryWrapper<Meal>()
                        .eq(Meal::getUserId, userId)
                        .ge(Meal::getMealTime, startDate.atStartOfDay())
                        .le(Meal::getMealTime, today.plusDays(1).atStartOfDay())
                        .orderByAsc(Meal::getMealTime)
        );

        // 按日期+餐次分组聚合
        final Map<String, List<Meal>> groupedMeals = meals.stream()
                .collect(Collectors.groupingBy(m -> {
                    final String date = m.getMealTime() != null ? m.getMealTime().format(DATE_FORMATTER) : "";
                    final String mealType = m.getMealType() != null ? m.getMealType() : "";
                    return date + "_" + mealType;
                }));

        final Map<String, String> MEAL_TYPE_CN = Map.of(
                "breakfast", "早餐",
                "lunch", "午餐",
                "dinner", "晚餐",
                "snack", "加餐"
        );

        final List<DashboardTrainingLogVO.DietLogItemVO> dietLogs = groupedMeals.entrySet().stream()
                .map(entry -> {
                    final String[] parts = entry.getKey().split("_", 2);
                    final String mealDate = parts.length > 0 ? parts[0] : "";
                    final String mealType = parts.length > 1 ? parts[1] : "";
                    final List<Meal> mealList = entry.getValue();

                    final int totalCal = mealList.stream()
                            .mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0)
                            .sum();

                    final List<DashboardTrainingLogVO.DietFoodDetailVO> foods = mealList.stream()
                            .map(m -> DashboardTrainingLogVO.DietFoodDetailVO.builder()
                                    .foodName(m.getFoodName())
                                    .calories(m.getCalories())
                                    .mealTime(m.getMealTime() != null ? m.getMealTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "")
                                    .build())
                            .toList();

                    return DashboardTrainingLogVO.DietLogItemVO.builder()
                            .mealType(mealType)
                            .mealTypeName(MEAL_TYPE_CN.getOrDefault(mealType, mealType))
                            .mealDate(mealDate)
                            .totalCalories(totalCal)
                            .foods(foods)
                            .build();
                })
                .sorted((a, b) -> {
                    int dateCompare = b.getMealDate().compareTo(a.getMealDate());
                    if (dateCompare != 0) return dateCompare;
                    final int orderA = getMealOrder(a.getMealType());
                    final int orderB = getMealOrder(b.getMealType());
                    return orderB - orderA;
                })
                .toList();

        return DashboardTrainingLogVO.builder()
                .workoutLogs(workoutLogs)
                .extraExerciseLogs(extraExerciseLogs)
                .dietLogs(dietLogs)
                .totalWorkoutSessions(workoutLogs.size())
                .totalExtraExerciseSessions(extraExerciseLogs.size())
                .totalDietSessions(dietLogs.size())
                .build();
    }

    @Override
    public DashboardNutritionVO getNutritionRatio(final Long userId, final String dateStr) {
        final String date = StringUtils.hasText(dateStr) ? dateStr : LocalDate.now().format(DATE_FORMATTER);
        final LocalDate localDate = LocalDate.parse(date);
        final LocalDateTime dayStart = localDate.atStartOfDay();
        final LocalDateTime dayEnd = localDate.atTime(LocalTime.MAX);

        // 1. 获取用户信息用于计算个性化目标
        final UserProfile profile = userProfileMapper.selectById(userId);
        
        // 计算每日总热量目标和营养素目标克数
        final int[] nutritionRatios = getNutritionRatiosByGoal(profile);
        final int carbsTargetPercent = nutritionRatios[0];
        final int proteinTargetPercent = nutritionRatios[1];
        final int fatTargetPercent = nutritionRatios[2];
        
        // 计算目标总热量 (TDEE)
        final int targetCalories = calculateTargetCalories(profile);
        
        // 计算各营养素目标克数
        final int targetCarbsGrams = (int) (targetCalories * carbsTargetPercent / 100.0 / CARBS_CALORIES_PER_GRAM);
        final int targetProteinGrams = (int) (targetCalories * proteinTargetPercent / 100.0 / PROTEIN_CALORIES_PER_GRAM);
        final int targetFatGrams = (int) (targetCalories * fatTargetPercent / 100.0 / FAT_CALORIES_PER_GRAM);

        // 2. 查询当日饮食记录
        final List<Meal> meals = mealMapper.selectList(
                new LambdaQueryWrapper<Meal>()
                        .eq(Meal::getUserId, userId)
                        .ge(Meal::getMealTime, dayStart)
                        .le(Meal::getMealTime, dayEnd)
                        .orderByAsc(Meal::getMealTime)
        );

        // 3. 计算总热量和营养素（使用数据库中实际存储的数据）
        final int totalCalories = meals.stream()
                .mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0)
                .sum();

        // 从数据库中实际获取营养素数据
        final int totalCarbs = meals.stream()
                .mapToInt(m -> m.getCarbs() != null ? m.getCarbs() : 0)
                .sum();
        final int totalProtein = meals.stream()
                .mapToInt(m -> m.getProtein() != null ? m.getProtein() : 0)
                .sum();
        final int totalFat = meals.stream()
                .mapToInt(m -> m.getFat() != null ? m.getFat() : 0)
                .sum();

        // 计算营养素热量
        final int carbsCalories = (int) (totalCarbs * CARBS_CALORIES_PER_GRAM);
        final int proteinCalories = (int) (totalProtein * PROTEIN_CALORIES_PER_GRAM);
        final int fatCalories = (int) (totalFat * FAT_CALORIES_PER_GRAM);

        // 计算实际占比
        int carbsPercent = 0;
        int proteinPercent = 0;
        int fatPercent = 0;
        if (totalCalories > 0) {
            carbsPercent = (int) Math.round((double) carbsCalories / totalCalories * 100);
            proteinPercent = (int) Math.round((double) proteinCalories / totalCalories * 100);
            fatPercent = 100 - carbsPercent - proteinPercent;
        }

        // 4. 构建营养详情列表（使用每条饮食记录的实际数据）
        final List<DashboardNutritionVO.NutritionDetailVO> details = meals.stream()
                .map(meal -> {
                    final int mealCal = meal.getCalories() != null ? meal.getCalories() : 0;
                    final int mealCarbs = meal.getCarbs() != null ? meal.getCarbs() : 0;
                    final int mealProtein = meal.getProtein() != null ? meal.getProtein() : 0;
                    final int mealFat = meal.getFat() != null ? meal.getFat() : 0;

                    final int mealCarbsCal = (int) (mealCarbs * CARBS_CALORIES_PER_GRAM);
                    final int mealProteinCal = (int) (mealProtein * PROTEIN_CALORIES_PER_GRAM);
                    final int mealFatCal = (int) (mealFat * FAT_CALORIES_PER_GRAM);

                    final int mealCarbsPct = mealCal > 0 ? (int) Math.round((double) mealCarbsCal / mealCal * 100) : 0;
                    final int mealProteinPct = mealCal > 0 ? (int) Math.round((double) mealProteinCal / mealCal * 100) : 0;
                    final int mealFatPct = mealCal > 0 ? (int) Math.round((double) mealFatCal / mealCal * 100) : 0;

                    return DashboardNutritionVO.NutritionDetailVO.builder()
                            .foodName(meal.getFoodName())
                            .calories(mealCal)
                            .carbs(mealCarbs)
                            .protein(mealProtein)
                            .fat(mealFat)
                            .mealType(meal.getMealType())
                            .mealTime(meal.getMealTime() != null ? meal.getMealTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "")
                            .carbsPercent(mealCarbsPct)
                            .proteinPercent(mealProteinPct)
                            .fatPercent(mealFatPct)
                            .build();
                })
                .toList();

        return DashboardNutritionVO.builder()
                .date(date)
                .totalCalories(totalCalories)
                .carbsGrams(totalCarbs)
                .proteinGrams(totalProtein)
                .fatGrams(totalFat)
                .carbsCalories(carbsCalories)
                .proteinCalories(proteinCalories)
                .fatCalories(fatCalories)
                .carbsPercent(carbsPercent)
                .proteinPercent(proteinPercent)
                .fatPercent(fatPercent)
                .carbsTargetPercent(carbsTargetPercent)
                .proteinTargetPercent(proteinTargetPercent)
                .fatTargetPercent(fatTargetPercent)
                .targetCalories(targetCalories)
                .carbsTargetGrams(targetCarbsGrams)
                .proteinTargetGrams(targetProteinGrams)
                .fatTargetGrams(targetFatGrams)
                .details(details)
                .build();
    }

    /**
     * 根据用户健身目标获取营养素配比（热量占比）。
     * 
     * @param profile 用户资料
     * @return 营养素配比 int[]{碳水%, 蛋白%, 脂肪%}
     */
    private int[] getNutritionRatiosByGoal(final UserProfile profile) {
        if (profile == null || profile.getGoal() == null) {
            return new int[]{CARBS_TARGET_PERCENT, PROTEIN_TARGET_PERCENT, FAT_TARGET_PERCENT};
        }
        return GOAL_NUTRITION_RATIOS.getOrDefault(profile.getGoal(), 
            new int[]{CARBS_TARGET_PERCENT, PROTEIN_TARGET_PERCENT, FAT_TARGET_PERCENT});
    }

    /**
     * 根据健身目标计算每日应吃热量（目标摄入）。
     *
     * 核心原理（与 MealServiceImpl 保持一致）：
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
     *
     * @param profile 用户资料
     * @return 每日目标热量 (kcal)
     */
    private int calculateTargetCalories(final UserProfile profile) {
        // 默认值
        final int DEFAULT_TARGET = 1500;

        if (profile == null) {
            return DEFAULT_TARGET;
        }

        // 检查必要字段
        final Integer height = profile.getHeight();
        final BigDecimal weight = profile.getWeight();
        final Integer age = profile.getAge();
        final String gender = profile.getGender();

        if (height == null || height <= 0 || weight == null || weight.doubleValue() <= 0) {
            return DEFAULT_TARGET;
        }

        // Mifflin-St Jeor 公式计算 BMR
        // 男性: BMR = 10 * 体重(kg) + 6.25 * 身高(cm) - 5 * 年龄 + 5
        // 女性: BMR = 10 * 体重(kg) + 6.25 * 身高(cm) - 5 * 年龄 - 161
        final int effectiveAge = (age != null && age > 0) ? age : 25;
        double bmr;
        if ("male".equalsIgnoreCase(gender)) {
            bmr = 10 * weight.doubleValue() + 6.25 * height - 5 * effectiveAge + 5;
        } else {
            bmr = 10 * weight.doubleValue() + 6.25 * height - 5 * effectiveAge - 161;
        }
        final int bmrInt = (int) bmr;

        // 1. 算出基于 BMR 的基础平衡热量（不含当天额外运动的静态平衡）
        final double baseBalance = bmrInt / 0.7;

        // 2. 根据目标确定系数
        double multiplier = switch (profile.getGoal() != null ? profile.getGoal() : "maintain") {
            case "lose" -> 0.8 * 0.8;      // 减脂目标 80%
            case "gain" -> 1.1 * 0.8;      // 增肌目标 110%
            default -> 1.0 * 0.8;          // 保持目标 100%
        };

        // 3. 核心修正：(基础平衡 * 系数)，此处不含运动损耗
        //    营养配比场景下取静态值，与 MealServiceImpl 中动态加运动消耗的逻辑对应
        return (int) (baseBalance * multiplier);
    }

    /**
     * 获取餐次排序顺序。
     */
    private int getMealOrder(final String mealType) {
        return switch (mealType) {
            case "breakfast" -> 1;
            case "lunch" -> 2;
            case "dinner" -> 3;
            case "snack" -> 4;
            default -> 5;
        };
    }
}