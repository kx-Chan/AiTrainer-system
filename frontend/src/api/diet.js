import request from "@/utils/request";

export const dietApi = {
  /** 获取指定日期的饮食汇总 */
  getSummary: (date) => request.get("/meals/summary", { params: { date } }),

  /** 添加饮食记录 */
  addMeal: (data) => request.post("/meals", data),

  /** 编辑饮食记录 */
  updateMeal: (mealId, data) => request.put(`/meals/${mealId}`, data),

  /** 删除饮食记录（逻辑删除） */
  deleteMeal: (mealId) => request.delete(`/meals/${mealId}`),

  /** 添加额外运动消耗 */
  addExtraExercise: (data) => request.post("/meals/extra-exercise", data),

  /** 编辑额外运动消耗 */
  updateExtraExercise: (exerciseId, data) =>
    request.put(`/meals/extra-exercise/${exerciseId}`, data),

  /** 删除额外运动消耗 */
  deleteExtraExercise: (exerciseId) =>
    request.delete(`/meals/extra-exercise/${exerciseId}`),

  /** AI 智能估算食物热量和营养成分（AI调用较慢，超时设为60秒） */
  analyzeFood: (data) => request.post("/meals/analyze", data, { timeout: 60000 }),
};
