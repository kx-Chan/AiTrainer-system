import request from '@/utils/request'

/**
 * AI 私教 API
 */
export const aiCoachApi = {
  /**
   * 发送 AI 分析请求
   * @param {Object} data 请求参数
   * @param {string} data.analysisType - 分析类型：training-训练分析, diet-饮食分析, comprehensive-综合分析
   * @param {string} data.question - 用户的问题
   * @param {boolean} data.includeTrainingData - 是否包含训练数据
   * @param {number} data.trainingDays - 训练数据时间范围：7-近7天, 30-近30天
   * @param {boolean} data.includeDietData - 是否包含饮食数据
   * @param {number} data.dietDays - 饮食数据时间范围：7-近7天, 30-近30天
   * @param {string} data.sessionId - 会话 ID，用于保持对话上下文（可选）
   * @returns {Promise} 返回分析结果，包含 sessionId 用于后续对话
   */
  analyze: (data) => request.post('/ai-coach/analyze', data, { timeout: 120000 }),

  /**
   * 获取聊天历史
   * @param {string} sessionId - 会话 ID
   * @param {number} limit - 限制数量，默认 20
   * @returns {Promise} 返回聊天历史列表
   */
  getChatHistory: (sessionId, limit = 20) => 
    request.get('/ai-coach/history', { params: { sessionId, limit } }),

  /**
   * 获取用户的所有会话列表
   * @returns {Promise} 返回会话 ID 列表
   */
  getSessions: () => request.get('/ai-coach/sessions'),

  /**
   * 获取用户的所有会话详情列表
   * @param {number} limit - 限制数量，默认 20
   * @returns {Promise} 返回会话详情列表
   */
  getSessionDetails: (limit = 20) => 
    request.get('/ai-coach/sessions/detail', { params: { limit } }),

  /**
   * 删除指定会话
   * @param {string} sessionId - 会话 ID
   * @returns {Promise} 返回是否成功
   */
  deleteSession: (sessionId) => request.delete(`/ai-coach/sessions/${sessionId}`),

  /**
   * 根据提问 ID 获取对应的 AI 回复
   * 实现消息锚定功能：点击左侧提问，右侧展示对应的 AI 分析结果
   * @param {string} sessionId - 会话 ID
   * @param {number} questionId - 提问消息的 ID
   * @returns {Promise} 返回 AI 回复消息
   */
  getAssistantReply: (sessionId, questionId) => 
    request.get(`/ai-coach/question/${questionId}/reply`, { params: { sessionId } })
}
