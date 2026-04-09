package com.aitrainer.mapper;

import com.aitrainer.entity.WorkoutSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WorkoutSessionMapper extends BaseMapper<WorkoutSession> {

    @Update("UPDATE workout_sessions SET like_count = like_count + 1 WHERE id = #{sessionId}")
    int incrementLikeCount(@Param("sessionId") Long sessionId);

    @Update("UPDATE workout_sessions SET like_count = like_count - 1 WHERE id = #{sessionId} AND like_count > 0")
    int decrementLikeCount(@Param("sessionId") Long sessionId);
}
