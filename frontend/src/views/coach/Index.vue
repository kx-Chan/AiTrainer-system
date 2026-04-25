<template>
  <div class="agent-container">
    <el-row :gutter="24" class="layout-row">

      <el-col :span="12" class="col-panel">
        <el-card shadow="never" class="chat-card glass-panel">
          <template #header>
            <div class="chat-header">
              <div class="agent-title">
                <div class="ai-avatar-wrapper">
                  <el-avatar :size="36" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                  <div class="online-dot"></div>
                </div>
                <div class="title-text">
                  <span class="gradient-text">AI 综合私教</span>
                  <span class="sub-text">AiTrainer 智能分析系统</span>
                </div>
              </div>
              <el-button plain round size="small" @click="resetChat"><el-icon>
                  <Refresh />
                </el-icon> 新对话</el-button>
            </div>
          </template>

          <!-- 分析类型选择 -->
          <div class="analysis-options">
            <div class="option-section">
              <div class="section-title">
                <el-icon><Setting /></el-icon>
                分析模式
              </div>
              <el-radio-group v-model="analysisType" size="default" class="analysis-type-group">
                <el-radio-button label="training">
                  <el-icon><Trophy /></el-icon> 主分析训练
                </el-radio-button>
                <el-radio-button label="diet">
                  <el-icon><IceTea /></el-icon> 主分析饮食
                </el-radio-button>
                <el-radio-button label="comprehensive">
                  <el-icon><DataAnalysis /></el-icon> 综合分析
                </el-radio-button>
              </el-radio-group>
            </div>

            <div class="option-section">
              <div class="section-title">
                <el-icon><Clock /></el-icon>
                数据范围
              </div>
              <div class="data-options">
                <div class="data-option-item">
                  <el-checkbox v-model="includeTrainingData" :disabled="analysisType === 'diet'">
                    训练数据
                  </el-checkbox>
                  <el-radio-group v-model="trainingDays" size="small" :disabled="!includeTrainingData || analysisType === 'diet'">
                    <el-radio-button :label="7">近7天</el-radio-button>
                    <el-radio-button :label="30">近30天</el-radio-button>
                  </el-radio-group>
                </div>
                <div class="data-option-item">
                  <el-checkbox v-model="includeDietData" :disabled="analysisType === 'training'">
                    饮食数据
                  </el-checkbox>
                  <el-radio-group v-model="dietDays" size="small" :disabled="!includeDietData || analysisType === 'training'">
                    <el-radio-button :label="7">近7天</el-radio-button>
                    <el-radio-button :label="30">近30天</el-radio-button>
                  </el-radio-group>
                </div>
              </div>
            </div>
          </div>

          <!-- 聊天窗口 -->
          <div class="chat-window" ref="chatWindowRef">
            <div v-for="(msg, index) in messageList" :key="index" :class="['message-item', msg.role]">
              <div class="message-avatar" v-if="msg.role === 'ai'">
                <el-avatar :size="40" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              </div>
              <div class="message-bubble-wrapper">
                <div v-if="msg.type === 'thinking'" class="thinking-box">
                  <el-icon class="is-loading">
                    <Loading />
                  </el-icon> {{ msg.content }}
                </div>
                <div v-else class="message-bubble" v-html="msg.content"></div>
              </div>
            </div>
          </div>

          <!-- 快捷提问 -->
          <div class="quick-prompts" v-if="messageList.length === 1">
            <div class="prompt-card" @click="sendQuickPrompt('请分析我最近的训练表现，给出改进建议')">
              <el-icon size="20" color="#409EFF">
                <TrendCharts />
              </el-icon>
              <span>分析训练表现</span>
            </div>
            <div class="prompt-card" @click="sendQuickPrompt('请分析我的饮食习惯，帮我优化营养搭配')">
              <el-icon size="20" color="#67C23A">
                <Food />
              </el-icon>
              <span>优化饮食搭配</span>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input-area">
            <el-input v-model="inputText" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }"
              placeholder="请输入您想咨询的问题..." @keydown.enter.prevent="handleSend"
              :disabled="isAiThinking" class="mac-input" />
            <div class="input-actions">
              <span class="tip-text">
                <el-icon><InfoFilled /></el-icon>
                {{ getAnalysisTypeTip }}
              </span>
              <el-button type="primary" round size="large" :disabled="!inputText || isAiThinking" @click="handleSend"
                class="send-btn" :loading="isAiThinking">
                发送 <el-icon class="el-icon--right" v-if="!isAiThinking">
                  <Position />
                </el-icon>
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="col-panel">
        <el-card shadow="never" class="result-card glass-panel">

          <div v-if="!currentAnalysis && !isAiThinking" class="empty-state">
            <div class="artifact-icon">✨</div>
            <h3>AI 分析结果</h3>
            <p>选择分析模式和数据范围，输入问题后，AI 将为您生成专业分析</p>
          </div>

          <div v-else-if="isAiThinking && !currentAnalysis" class="loading-state">
            <el-icon class="loading-icon" :size="48"><Loading /></el-icon>
            <h3>AI 教练正在深度查阅你最近 {{ getLoadingDaysText }} 的汗水记录</h3>
            <p>请耐心等待，深度分析需要一些时间...</p>
            <div class="loading-progress">
              <el-progress :percentage="loadingProgress" :stroke-width="8" :show-text="false" />
              <span class="progress-text">{{ loadingProgressText }}</span>
            </div>
          </div>

          <div v-else class="analysis-content slide-in">
            <div class="result-header">
              <h2>
                <el-icon :color="getAnalysisTypeColor">
                  <component :is="getAnalysisTypeIcon" />
                </el-icon>
                {{ getAnalysisTypeTitle }}
              </h2>
              <el-tag :type="getAnalysisTypeTag">{{ getAnalysisTypeLabel }}</el-tag>
            </div>

            <!-- 数据摘要 -->
            <el-collapse v-if="dataSummary" class="data-summary-collapse">
              <el-collapse-item title="📊 数据摘要" name="summary">
                <div class="data-summary-content">
                  <div v-if="currentAnalysis?.trainingDataSummary" class="summary-section">
                    <h4><el-icon><Trophy /></el-icon> 训练数据</h4>
                    <pre>{{ currentAnalysis.trainingDataSummary }}</pre>
                  </div>
                  <div v-if="currentAnalysis?.dietDataSummary" class="summary-section">
                    <h4><el-icon><IceTea /></el-icon> 饮食数据</h4>
                    <pre>{{ currentAnalysis.dietDataSummary }}</pre>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>

            <!-- 分析结果 -->
            <div class="analysis-result" v-html="formattedAnalysisResult"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Position, Refresh, Loading, TrendCharts,
  Food, IceTea, Setting, Clock,
  Trophy, DataAnalysis, InfoFilled
} from '@element-plus/icons-vue'
import { aiCoachApi } from '@/api/aiCoach'
import { marked } from 'marked'

// 配置 marked 选项
marked.setOptions({
  breaks: true,
  gfm: true,
})

const chatWindowRef = ref(null)

// 分析选项
const analysisType = ref('comprehensive')
const includeTrainingData = ref(true)
const trainingDays = ref(7)
const includeDietData = ref(true)
const dietDays = ref(7)

// 聊天状态
const inputText = ref('')
const isAiThinking = ref(false)
const messageList = ref([
  { role: 'ai', type: 'text', content: '你好！我是你的专属 AI 综合私教。我可以帮你分析训练和饮食数据，给出专业建议。请选择分析模式和数据范围，然后输入你的问题。' }
])

// 会话 ID（用于保持上下文）
const currentSessionId = ref(null)

// 当前分析结果
const currentAnalysis = ref(null)

// Loading 进度状态
const loadingProgress = ref(0)
const loadingProgressText = ref('正在准备分析...')

// 计算显示的天数文本
const getLoadingDaysText = computed(() => {
  const days = []
  if (includeTrainingData.value && trainingDays.value) {
    days.push(`${trainingDays.value}天训练`)
  }
  if (includeDietData.value && dietDays.value) {
    days.push(`${dietDays.value}天饮食`)
  }
  return days.join('和')
})

// 开始 Loading 动画
let loadingTimer = null
const startLoadingAnimation = () => {
  loadingProgress.value = 0
  loadingProgressText.value = '正在调取您的数据...'
  
  loadingTimer = setInterval(() => {
    if (loadingProgress.value < 85) {
      loadingProgress.value += Math.random() * 15
      if (loadingProgress.value > 85) loadingProgress.value = 85
      
      if (loadingProgress.value < 30) {
        loadingProgressText.value = '正在调取您的数据...'
      } else if (loadingProgress.value < 60) {
        loadingProgressText.value = '正在分析训练数据...'
      } else if (loadingProgress.value < 80) {
        loadingProgressText.value = '正在分析饮食数据...'
      } else {
        loadingProgressText.value = '正在生成分析报告...'
      }
    }
  }, 1000)
}

// 停止 Loading 动画
const stopLoadingAnimation = () => {
  if (loadingTimer) {
    clearInterval(loadingTimer)
    loadingTimer = null
  }
  loadingProgress.value = 100
  loadingProgressText.value = '分析完成！'
}

// 计算属性
const getAnalysisTypeTip = computed(() => {
  const tips = {
    training: '训练分析模式：专注于分析您的训练数据',
    diet: '饮食分析模式：专注于分析您的饮食数据',
    comprehensive: '综合分析模式：全面分析训练和饮食数据'
  }
  return tips[analysisType.value]
})

const getAnalysisTypeColor = computed(() => {
  const colors = {
    training: '#409EFF',
    diet: '#67C23A',
    comprehensive: '#E6A23C'
  }
  return colors[analysisType.value]
})

const getAnalysisTypeIcon = computed(() => {
  const icons = {
    training: Trophy,
    diet: IceTea,
    comprehensive: DataAnalysis
  }
  return icons[analysisType.value]
})

const getAnalysisTypeTitle = computed(() => {
  const titles = {
    training: '训练分析报告',
    diet: '饮食分析报告',
    comprehensive: '综合分析报告'
  }
  return titles[analysisType.value]
})

const getAnalysisTypeLabel = computed(() => {
  const labels = {
    training: '训练分析',
    diet: '饮食分析',
    comprehensive: '综合分析'
  }
  return labels[analysisType.value]
})

const getAnalysisTypeTag = computed(() => {
  const tags = {
    training: '',
    diet: 'success',
    comprehensive: 'warning'
  }
  return tags[analysisType.value]
})

const dataSummary = computed(() => {
  return currentAnalysis.value?.trainingDataSummary || currentAnalysis.value?.dietDataSummary
})

const formattedAnalysisResult = computed(() => {
  if (!currentAnalysis.value?.analysisResult) return ''
  // 使用 marked 正确渲染 Markdown
  return marked.parse(currentAnalysis.value.analysisResult)
})

// 监听分析类型变化，自动调整数据选择
watch(analysisType, (newType) => {
  if (newType === 'training') {
    includeTrainingData.value = true
    includeDietData.value = false
  } else if (newType === 'diet') {
    includeTrainingData.value = false
    includeDietData.value = true
  } else {
    includeTrainingData.value = true
    includeDietData.value = true
  }
})

const scrollToBottom = async () => {
  await nextTick()
  if (chatWindowRef.value) {
    chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight
  }
}

const sendQuickPrompt = (text) => {
  inputText.value = text
  handleSend()
}

const handleSend = async () => {
  if (!inputText.value.trim() || isAiThinking.value) return

  const userText = inputText.value
  messageList.value.push({ role: 'user', type: 'text', content: userText })
  inputText.value = ''
  isAiThinking.value = true
  currentAnalysis.value = null
  scrollToBottom()

  // 添加思考中消息
  messageList.value.push({ role: 'ai', type: 'thinking', content: '正在调取您的数据...' })
  scrollToBottom()

  // 开始 Loading 动画
  startLoadingAnimation()

  try {
    // 调用 API，传入 sessionId 以保持上下文
    const response = await aiCoachApi.analyze({
      analysisType: analysisType.value,
      question: userText,
      includeTrainingData: includeTrainingData.value,
      trainingDays: trainingDays.value,
      includeDietData: includeDietData.value,
      dietDays: dietDays.value,
      sessionId: currentSessionId.value // 传入当前会话 ID
    })

    // 停止 Loading 动画
    stopLoadingAnimation()

    // 移除思考中消息
    messageList.value.pop()

    // 添加 AI 回复
    messageList.value.push({
      role: 'ai',
      type: 'text',
      content: '分析完成！请查看右侧面板获取详细分析结果。'
    })

    // 保存分析结果和会话 ID
    currentAnalysis.value = response
    if (response.sessionId) {
      currentSessionId.value = response.sessionId
    }

    ElMessage.success('分析完成')
  } catch (error) {
    console.error('AI 分析失败:', error)
    stopLoadingAnimation()
    messageList.value.pop()
    messageList.value.push({
      role: 'ai',
      type: 'text',
      content: '抱歉，分析过程中出现了一些问题。请稍后再试。'
    })
    ElMessage.error('分析失败，请重试')
  } finally {
    isAiThinking.value = false
    scrollToBottom()
  }
}

const resetChat = () => {
  // 重置会话 ID，开启新对话
  currentSessionId.value = null
  messageList.value = [
    { role: 'ai', type: 'text', content: '你好！我是你的专属 AI 综合私教。我可以帮你分析训练和饮食数据，给出专业建议。请选择分析模式和数据范围，然后输入你的问题。' }
  ]
  currentAnalysis.value = null
  ElMessage.success('已开启新对话')
}
</script>

<style scoped>
.agent-container {
  max-width: 1400px;
  margin: 0 auto;
  height: calc(100vh - 100px);
  padding-bottom: 20px;
}

.layout-row {
  height: 100%;
  display: flex;
  flex-wrap: nowrap;
  margin-left: -12px;
  margin-right: -12px;
}

.col-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding-left: 12px;
  padding-right: 12px;
}

.glass-panel {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.05);
}

.chat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
}

.chat-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f2f5;
}

.ai-avatar-wrapper {
  position: relative;
}

.online-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background-color: #67C23A;
  border-radius: 50%;
  border: 2px solid #fff;
}

.agent-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-text {
  display: flex;
  flex-direction: column;
}

.gradient-text {
  font-size: 16px;
  font-weight: 900;
  background: linear-gradient(45deg, #409EFF, #8a2be2);
  -webkit-background-clip: text;
  color: transparent;
  transition: all 0.3s;
}

.sub-text {
  font-size: 11px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 分析选项样式 */
.analysis-options {
  padding: 16px 20px;
  background-color: #fafbfc;
  border-bottom: 1px solid #f0f2f5;
}

.option-section {
  margin-bottom: 16px;
}

.option-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 10px;
}

.analysis-type-group {
  width: 100%;
}

.analysis-type-group :deep(.el-radio-button__inner) {
  width: 100%;
  border-radius: 8px !important;
  margin: 0 4px;
}

.analysis-type-group :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 8px !important;
}

.data-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.data-option-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.chat-window {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background-color: #fafbfc;
}

.message-item {
  display: flex;
  gap: 16px;
  margin-bottom: 30px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-bubble-wrapper {
  max-width: 80%;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.message-item.user .message-bubble-wrapper {
  align-items: flex-end;
}

.message-bubble {
  padding: 14px 20px;
  border-radius: 16px;
  font-size: 15px;
  line-height: 1.6;
}

.message-item.ai .message-bubble {
  background-color: #ffffff;
  border: 1px solid #ebeef5;
  border-top-left-radius: 4px;
  color: #303133;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02);
  border-left: 4px solid #409EFF;
}

.message-item.user .message-bubble {
  background-color: #409EFF;
  color: #ffffff;
  border-top-right-radius: 4px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.thinking-box {
  padding: 12px 16px;
  background-color: #fffaf0;
  border-radius: 8px;
  border-left: 4px solid #E6A23C;
  color: #E6A23C;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-prompts {
  display: flex;
  gap: 12px;
  padding: 0 24px 20px;
  background-color: #fafbfc;
}

.prompt-card {
  flex: 1;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.prompt-card:hover {
  border-color: #409EFF;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.prompt-card span {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.chat-input-area {
  padding: 20px 24px;
  background-color: #ffffff;
  border-top: 1px solid #ebeef5;
}

.mac-input :deep(.el-textarea__inner) {
  background-color: #f5f7fa;
  border: none;
  border-radius: 12px;
  padding: 16px;
  font-size: 15px;
  resize: none;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.tip-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.result-card {
  flex: 1;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
}

.result-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
}

.result-card :deep(.el-collapse) {
  border: none;
}

.result-card :deep(.el-collapse-item__header) {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px 16px;
  font-weight: 600;
  color: #409EFF;
}

.result-card :deep(.el-collapse-item__wrap) {
  border: none;
}

.result-card :deep(.el-collapse-item__content) {
  padding: 12px 0;
}

.empty-state, .loading-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
}

.artifact-icon {
  font-size: 48px;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

.loading-icon {
  color: #409EFF;
  margin-bottom: 16px;
  animation: spin 1s linear infinite;
}

.loading-progress {
  width: 80%;
  max-width: 300px;
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.loading-progress :deep(.el-progress__text) {
  display: none;
}

.progress-text {
  font-size: 13px;
  color: #909399;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.analysis-content {
  padding: 24px;
  min-height: calc(100vh - 180px);
  overflow-y: auto;
}

.slide-in {
  animation: slideInUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.result-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.data-summary-collapse {
  margin-bottom: 20px;
}

.data-summary-content {
  max-height: 300px;
  overflow-y: auto;
}

.summary-section {
  margin-bottom: 16px;
}

.summary-section:last-child {
  margin-bottom: 0;
}

.summary-section h4 {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
}

.summary-section pre {
  margin: 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.analysis-result {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  padding-bottom: 40px;
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
}

/* Markdown 标题样式 */
.analysis-result :deep(h1) {
  font-size: 20px;
  font-weight: 700;
  margin: 20px 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #409EFF;
  color: #303133;
}

.analysis-result :deep(h2) {
  font-size: 18px;
  font-weight: 600;
  margin: 18px 0 14px 0;
  padding-left: 12px;
  color: #409EFF;
  border-left: 4px solid #409EFF;
}

.analysis-result :deep(h3) {
  font-size: 16px;
  font-weight: 600;
  margin: 16px 0 12px 0;
  color: #606266;
}

/* 列表样式 */
.analysis-result :deep(ul), .analysis-result :deep(ol) {
  padding-left: 24px;
  margin: 12px 0;
}

.analysis-result :deep(li) {
  margin: 8px 0;
  line-height: 1.6;
}

.analysis-result :deep(li::marker) {
  color: #409EFF;
}

/* 段落样式 */
.analysis-result :deep(p) {
  margin: 12px 0;
  line-height: 1.8;
}

/* 强调样式 */
.analysis-result :deep(strong) {
  color: #409EFF;
  font-weight: 600;
}

/* 表格样式 */
.analysis-result :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
  border-radius: 8px;
  overflow: hidden;
}

.analysis-result :deep(th), .analysis-result :deep(td) {
  border: 1px solid #ebeef5;
  padding: 10px 14px;
  text-align: left;
}

.analysis-result :deep(th) {
  background: #f5f7fa;
  font-weight: 600;
  color: #606266;
}

.analysis-result :deep(tr:nth-child(even)) {
  background: #fafafa;
}

/* 代码块样式 */
.analysis-result :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  color: #E6A23C;
}

.analysis-result :deep(pre) {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 8px;
  margin: 12px 0;
  overflow-x: auto;
}

/* 引用样式 */
.analysis-result :deep(blockquote) {
  border-left: 4px solid #409EFF;
  padding-left: 16px;
  margin: 12px 0;
  color: #606266;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 12px 16px;
}
</style>
