package com.aitrainer.service;

import com.aitrainer.vo.WorkoutVO;
import java.util.List;

public interface WorkoutService {
    /**
     * 获取所有可用的健身项目列表
     */
    List<WorkoutVO> listWorkouts();

    /**
     * 根据ID获取特定健身项目详情
     */
    WorkoutVO getWorkoutById(String id);
}