package com.aitrainer.mapper;

import com.aitrainer.entity.WorkoutSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface WorkoutSessionMapper extends BaseMapper<WorkoutSession> {

    @Update("UPDATE workout_sessions SET like_count = like_count + 1 WHERE id = #{sessionId}")
    int incrementLikeCount(@Param("sessionId") Long sessionId);

    @Update("UPDATE workout_sessions SET like_count = like_count - 1 WHERE id = #{sessionId} AND like_count > 0")
    int decrementLikeCount(@Param("sessionId") Long sessionId);

    /**
     * 获取用户某年某月的打卡日期列表
     */
    @Select("SELECT DISTINCT DATE(created_at) FROM workout_sessions " +
            "WHERE user_id = #{userId} " +
            "AND YEAR(created_at) = #{year} " +
            "AND MONTH(created_at) = #{month} " +
            "AND is_deleted = 0 " +
            "ORDER BY 1")
    List<LocalDate> getCheckInDates(@Param("userId") Long userId, 
                                    @Param("year") int year, 
                                    @Param("month") int month);
}
