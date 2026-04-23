package com.aitrainer.service;

import com.aitrainer.dto.AddExtraExerciseDTO;
import com.aitrainer.dto.AddMealDTO;
import com.aitrainer.dto.AnalyzeExerciseDTO;
import com.aitrainer.dto.AnalyzeFoodDTO;
import com.aitrainer.dto.UpdateExtraExerciseDTO;
import com.aitrainer.dto.UpdateMealDTO;
import com.aitrainer.vo.DietSummaryVO;
import com.aitrainer.vo.ExerciseAnalysisVO;
import com.aitrainer.vo.ExtraExerciseVO;
import com.aitrainer.vo.FoodAnalysisVO;
import com.aitrainer.vo.MealVO;

/**
 * 饮食记录服务接口。
 */
public interface MealService {

    /**
     * 获取指定日期的饮食汇总（含热量统计）。
     *
     * @param userId 用户 ID
     * @param date   日期字符串 (yyyy-MM-dd)，为 null 则默认今天
     * @return 饮食汇总 VO
     */
    DietSummaryVO getDietSummary(Long userId, String date);

    /**
     * 添加一条饮食记录。
     *
     * @param userId 用户 ID
     * @param dto    添加参数
     * @return 新增记录 VO
     */
    MealVO addMeal(Long userId, AddMealDTO dto);

    /**
     * 编辑一条饮食记录。
     *
     * @param userId 用户 ID（用于鉴权）
     * @param mealId 记录 ID
     * @param dto    编辑参数
     * @return 更新后的记录 VO
     */
    MealVO updateMeal(Long userId, Long mealId, UpdateMealDTO dto);

    /**
     * 逻辑删除一条饮食记录。
     *
     * @param userId 用户 ID（用于鉴权）
     * @param mealId 记录 ID
     */
    void deleteMeal(Long userId, Long mealId);

    /**
     * 添加一条额外运动消耗记录。
     *
     * @param userId 用户 ID
     * @param dto    添加参数
     * @return 新增记录 VO
     */
    ExtraExerciseVO addExtraExercise(Long userId, AddExtraExerciseDTO dto);

    /**
     * 编辑一条额外运动消耗记录。
     *
     * @param userId     用户 ID（用于鉴权）
     * @param exerciseId 记录 ID
     * @param dto        编辑参数
     * @return 更新后的记录 VO
     */
    ExtraExerciseVO updateExtraExercise(Long userId, Long exerciseId, UpdateExtraExerciseDTO dto);

    /**
     * 逻辑删除一条额外运动消耗记录。
     *
     * @param userId     用户 ID（用于鉴权）
     * @param exerciseId 记录 ID
     */
    void deleteExtraExercise(Long userId, Long exerciseId);

    /**
     * AI 智能分析食物营养成分（当前为固定数据模拟，后续可接入 AI）。
     *
     * @param userId 用户 ID
     * @param dto    分析请求参数
     * @return 食物营养分析结果
     */
    FoodAnalysisVO analyzeFood(Long userId, AnalyzeFoodDTO dto);

    /**
     * AI 智能分析运动消耗（当前为 mock 数据，后续接入 AI Agent）。
     * 根据运动名称、时长以及用户身体数据估算消耗热量。
     *
     * @param userId 用户 ID（用于获取用户身体数据）
     * @param dto    分析请求参数
     * @return 运动消耗分析结果
     */
    ExerciseAnalysisVO analyzeExercise(Long userId, AnalyzeExerciseDTO dto);
}
