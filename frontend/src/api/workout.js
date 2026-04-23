import request from "@/utils/request";

export const workoutApi = {
  listWorkouts: (params) => request.get("/workouts", { params }),
  getWorkout: (workoutId) => request.get(`/workouts/${workoutId}`),
  // AI 生成战报需要调用 LLM，耗时较长（10-30秒），单独设置超时为 60 秒
  startSession: (data) => request.post("/workout/sessions", data, { timeout: 60000 }),
  getSession: (sessionId) => request.get(`/workout/sessions/${sessionId}`),
  listMySessions: (params) => request.get("/workout/sessions/me", { params }),

  // 点赞逻辑
  likeSession: (id) => request.post(`/workout/sessions/${id}/like`),
  unlikeSession: (id) => request.delete(`/workout/sessions/${id}/like`),
};
