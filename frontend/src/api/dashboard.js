import request from "@/utils/request";

/**
 * 获取近七天卡路里消耗数据
 */
export function getLast7DaysCalories() {
  return request({
    url: "/dashboard/calories",
    method: "get",
  });
}

/**
 * 获取详细训练日志（支持日期范围筛选）
 * @param {string} startDate 开始日期 (yyyy-MM-dd)
 * @param {string} endDate 结束日期 (yyyy-MM-dd)
 */
export function getTrainingLogs(startDate, endDate) {
  return request({
    url: "/dashboard/training-logs",
    method: "get",
    params: { startDate, endDate },
  });
}

/**
 * 获取营养摄入配比
 * @param {string} date 日期 (yyyy-MM-dd)，不传则默认今天
 */
export function getNutritionRatio(date) {
    return request({
        url: "/dashboard/nutrition",
        method: "get",
        params: date ? { date } : {},
    });
}

/**
 * 获取 AI 私教每日碎碎念
 * @param {string} date 日期 (yyyy-MM-dd)，不传则默认今天
 */
export function getAiCoachFeedback(date) {
    return request({
        url: "/dashboard/ai-coach-feedback",
        method: "get",
        params: date ? { date } : {},
    });
}
