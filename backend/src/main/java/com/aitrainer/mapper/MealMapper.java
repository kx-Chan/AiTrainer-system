package com.aitrainer.mapper;

import com.aitrainer.entity.Meal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 饮食记录 Mapper 接口。
 */
@Mapper
public interface MealMapper extends BaseMapper<Meal> {

    /**
     * 统计用户某天通过训练消耗的总热量。
     *
     * @param userId 用户 ID
     * @param date   日期字符串 (yyyy-MM-dd)
     * @return 训练消耗总热量 (kcal)
     */
    @Select("SELECT COALESCE(SUM(calories_burned), 0) FROM workout_sessions " +
            "WHERE user_id = #{userId} AND DATE(created_at) = #{date} AND is_deleted = 0")
    Integer sumWorkoutCaloriesByDate(@Param("userId") Long userId, @Param("date") String date);

    /**
     * 查询用户某天的训练消耗明细（项目名称 + 消耗热量 + 训练时长 + 创建时间）。
     *
     * @param userId 用户 ID
     * @param date   日期字符串 (yyyy-MM-dd)
     * @return 训练明细列表
     */
    @Select("SELECT w.name AS workoutName, ws.calories_burned AS caloriesBurned, " +
            "ws.duration_minutes AS durationMinutes, ws.created_at AS createdAt " +
            "FROM workout_sessions ws " +
            "LEFT JOIN workouts w ON ws.workout_id = w.id " +
            "WHERE ws.user_id = #{userId} AND DATE(ws.created_at) = #{date} AND ws.is_deleted = 0 " +
            "ORDER BY ws.created_at ASC")
    List<Map<String, Object>> selectWorkoutDetailsByDate(@Param("userId") Long userId, @Param("date") String date);
}
