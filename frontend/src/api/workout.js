import request from "@/utils/request";

export const workoutApi = {
  listWorkouts: (params) => request.get("/workouts", { params }),
  getWorkout: (workoutId) => request.get(`/workouts/${workoutId}`),
  startSession: (data) => request.post("/workout/sessions", data),
  getSession: (sessionId) => request.get(`/workout/sessions/${sessionId}`),
  listMySessions: (params) => request.get("/workout/sessions/me", { params }),

  // 点赞逻辑
  likeSession: (id) => request.post(`/workout/sessions/${id}/like`),
  unlikeSession: (id) => request.delete(`/workout/sessions/${id}/like`),
};
