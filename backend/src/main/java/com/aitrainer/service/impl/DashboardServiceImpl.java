package com.aitrainer.service.impl;

import com.aitrainer.entity.ExtraExercise;
import com.aitrainer.entity.Meal;
import com.aitrainer.entity.WorkoutSession;
import com.aitrainer.entity.Workout;
import com.aitrainer.mapper.ExtraExerciseMapper;
import com.aitrainer.mapper.MealMapper;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // 营养素热量系数 (kcal/g)
    private static final double CARBS_CALORIES_PER_GRAM = 4.0;
    private static final double PROTEIN_CALORIES_PER_GRAM = 4.0;
    private static final double FAT_CALORIES_PER_GRAM = 9.0;
    
    // 标准营养素配比（热量占比）
    private static final int CARBS_TARGET_PERCENT = 50;
    private static final int PROTEIN_TARGET_PERCENT = 30;
    private static final int FAT_TARGET_PERCENT = 20;

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

        // 1. 查询当日饮食记录
        final List<Meal> meals = mealMapper.selectList(
                new LambdaQueryWrapper<Meal>()
                        .eq(Meal::getUserId, userId)
                        .ge(Meal::getMealTime, dayStart)
                        .le(Meal::getMealTime, dayEnd)
                        .orderByAsc(Meal::getMealTime)
        );

        // 2. 计算总热量
        final int totalCalories = meals.stream()
                .mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0)
                .sum();

        // 3. 估算营养素（基于热量的百分比分配）
        // 详细营养素数据由 Agent 计算，目前使用估算值
        // 碳水化合物: 50% 热量, 蛋白质: 30% 热量, 脂肪: 20% 热量
        int carbsCalories = 0;
        int proteinCalories = 0;
        int fatCalories = 0;
        int carbsGrams = 0;
        int proteinGrams = 0;
        int fatGrams = 0;

        if (totalCalories > 0) {
            carbsCalories = (int) (totalCalories * CARBS_TARGET_PERCENT / 100.0);
            proteinCalories = (int) (totalCalories * PROTEIN_TARGET_PERCENT / 100.0);
            fatCalories = totalCalories - carbsCalories - proteinCalories; // 确保总和等于总热量

            carbsGrams = (int) Math.round(carbsCalories / CARBS_CALORIES_PER_GRAM);
            proteinGrams = (int) Math.round(proteinCalories / PROTEIN_CALORIES_PER_GRAM);
            fatGrams = (int) Math.round(fatCalories / FAT_CALORIES_PER_GRAM);
        }

        // 4. 计算实际占比
        int carbsPercent = 0;
        int proteinPercent = 0;
        int fatPercent = 0;
        if (totalCalories > 0) {
            carbsPercent = (int) Math.round((double) carbsCalories / totalCalories * 100);
            proteinPercent = (int) Math.round((double) proteinCalories / totalCalories * 100);
            fatPercent = 100 - carbsPercent - proteinPercent;
        }

        // 5. 构建营养详情列表（每条饮食记录的营养素估算）
        final List<DashboardNutritionVO.NutritionDetailVO> details = meals.stream()
                .map(meal -> {
                    final int mealCal = meal.getCalories() != null ? meal.getCalories() : 0;
                    final int mealCarbsCal = mealCal > 0 ? (int) (mealCal * CARBS_TARGET_PERCENT / 100.0) : 0;
                    final int mealProteinCal = mealCal > 0 ? (int) (mealCal * PROTEIN_TARGET_PERCENT / 100.0) : 0;
                    final int mealFatCal = mealCal - mealCarbsCal - mealProteinCal;

                    final int mealCarbs = (int) Math.round(mealCarbsCal / CARBS_CALORIES_PER_GRAM);
                    final int mealProtein = (int) Math.round(mealProteinCal / PROTEIN_CALORIES_PER_GRAM);
                    final int mealFat = (int) Math.round(mealFatCal / FAT_CALORIES_PER_GRAM);

                    final int mealCarbsPct = mealCal > 0 ? (int) Math.round((double) mealCarbsCal / mealCal * 100) : 0;
                    final int mealProteinPct = mealCal > 0 ? (int) Math.round((double) mealProteinCal / mealCal * 100) : 0;
                    final int mealFatPct = 100 - mealCarbsPct - mealProteinPct;

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
                .carbsGrams(carbsGrams)
                .proteinGrams(proteinGrams)
                .fatGrams(fatGrams)
                .carbsCalories(carbsCalories)
                .proteinCalories(proteinCalories)
                .fatCalories(fatCalories)
                .carbsPercent(carbsPercent)
                .proteinPercent(proteinPercent)
                .fatPercent(fatPercent)
                .carbsTargetPercent(CARBS_TARGET_PERCENT)
                .proteinTargetPercent(PROTEIN_TARGET_PERCENT)
                .fatTargetPercent(FAT_TARGET_PERCENT)
                .details(details)
                .build();
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
