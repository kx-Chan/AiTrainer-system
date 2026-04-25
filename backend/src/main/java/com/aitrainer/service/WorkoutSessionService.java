package com.aitrainer.service;

import com.aitrainer.vo.LikeStatusVO;
import com.aitrainer.vo.WorkoutSessionVO;

import java.time.LocalDate;
import java.util.List;

public interface WorkoutSessionService {
    /**
     * 获取战报详情（包含访客点赞状态）。
     *
     * @param sessionId 战报 ID
     * @param viewerId   访客用户 ID（可为 null）
     * @return 战报详情
     */
    WorkoutSessionVO getWorkoutSessionDetail(Long sessionId, Long viewerId);

    /**
     * 点赞战报。
     *
     * @param userId     当前用户 ID
     * @param sessionId 战报 ID
     * @return 点赞状态
     */
    LikeStatusVO likeWorkoutSession(Long userId, Long sessionId);

    /**
     * 取消点赞战报。
     *
     * @param userId     当前用户 ID
     * @param sessionId 战报 ID
     * @return 点赞状态
     */
    LikeStatusVO unlikeWorkoutSession(Long userId, Long sessionId);

    /**
     * 核心功能：随机生成一条 AI 战报数据
     * @param userId
     * @param workoutId
     * @return
     */
    Long createWorkoutSession(Long userId, String workoutId);


    /**
     * 获取当前用户的历史战报列表
     * @param userId
     * @return
     */
    List<WorkoutSessionVO> listMyWorkoutSessions(Long userId);

    /**
     * 获取用户某年某月的打卡日期列表
     * @param userId 用户ID
     * @param year 年份
     * @param month 月份
     * @return 打卡日期列表
     */
    List<LocalDate> getCheckInDates(Long userId, int year, int month);
}
