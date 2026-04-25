package com.aitrainer.service;

import com.aitrainer.vo.AiCoachFeedbackVO;
import com.aitrainer.vo.DashboardCalorieVO;
import com.aitrainer.vo.DashboardNutritionVO;
import com.aitrainer.vo.DashboardTrainingLogVO;

/**
 * 数据看板服务接口。
 */
public interface DashboardService {

    /**
     * 获取近七天卡路里消耗数据。
     *
     * @param userId 用户 ID
     * @return 近七天卡路里消耗 VO
     */
    DashboardCalorieVO getLast7DaysCalories(Long userId);

    /**
     * 获取详细训练日志（支持日期范围筛选）。
     *
     * @param userId 用户 ID
     * @param startDate 开始日期 (yyyy-MM-dd)，不传则默认近7天
     * @param endDate 结束日期 (yyyy-MM-dd)，不传则默认今天
     * @return 训练日志 VO
     */
    DashboardTrainingLogVO getTrainingLogs(Long userId, String startDate, String endDate);

    /**
     * 获取当日营养摄入配比。
     * 营养素标准配比（热量占比）：碳水化合物50%、蛋白质30%、脂肪20%
     * 详细营养素数据由 Agent 计算，目前使用估算值。
     *
     * @param userId 用户 ID
     * @param date   日期字符串 (yyyy-MM-dd)，为 null 则默认今天
     * @return 营养摄入配比 VO
     */
    DashboardNutritionVO getNutritionRatio(Long userId, String date);

    /**
     * 获取 AI 私教每日碎碎念反馈。
     *
     * @param userId 用户 ID
     * @param date   日期字符串 (yyyy-MM-dd)，为 null 则默认今天
     * @return AI 私教反馈 VO
     */
    AiCoachFeedbackVO getAiCoachFeedback(Long userId, String date);
}
