<template>
  <div class="agent-container">
    <el-row :gutter="24" class="layout-row">

      <!-- 历史会话侧边栏 -->
      <el-col :xs="24" :sm="24" :md="4" :span="4" class="col-panel sidebar-panel">
        <el-card shadow="never" class="history-card glass-panel">
          <template #header>
            <div class="history-header">
              <span class="history-title">对话历史</span>
              <el-button text size="small" @click="loadSessions" :loading="loadingSessions">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="history-list">
            <div v-if="sessionList.length === 0 && !loadingSessions" class="empty-history">
              <p>暂无历史对话</p>
            </div>
            <div 
              v-for="session in sessionList" 
              :key="session.sessionId"
              :class="['session-item', { active: session.sessionId === currentSessionId }]"
              @click="restoreSession(session)"
            >
              <div class="session-info">
                <div class="session-title">{{ session.title }}</div>
                <div class="session-meta">
                  <el-icon size="12"><Clock /></el-icon>
                  {{ formatTime(session.lastMessageTime) }}
                </div>
              </div>
              <el-dropdown trigger="click" @command="(cmd) => handleSessionCommand(cmd, session)">
                <el-icon class="session-action"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="delete">
                      <el-icon><Delete /></el-icon> 删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="10" :span="10" class="col-panel chat-panel">
        <el-card shadow="never" class="chat-card glass-panel">
          <div ref="chatAnchorRef" class="chat-anchor"></div>
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
              <el-button plain round size="small" @click="startNewChat"><el-icon>
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
                <el-radio-button label="chat">
                  <el-icon><ChatLineSquare /></el-icon> 闲聊模式
                </el-radio-button>
              </el-radio-group>
            </div>

            <div class="option-section" v-show="analysisType !== 'chat'">
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
                <div v-else :class="['message-bubble', { 'clickable': msg.role === 'ai' && msg.questionId }]" 
                     v-html="msg.content"
                     @click="msg.role === 'ai' && msg.questionId && handleQuestionClick(msg.questionId)">
                </div>
                <div v-if="msg.role === 'ai' && msg.questionId" class="click-hint">
                  <el-icon size="12"><View /></el-icon> 点击查看回复
                </div>
              </div>
            </div>
          </div>

          <!-- 快捷提问 -->
          <div class="quick-prompts" v-if="messageList.length === 1 && analysisType !== 'chat'">
            <div class="prompt-card" v-if="analysisType !== 'diet'" @click="sendQuickPrompt('请分析我最近的训练表现，给出改进建议')">
              <el-icon size="20" color="#409EFF">
                <TrendCharts />
              </el-icon>
              <span>分析训练表现</span>
            </div>
            <div class="prompt-card" v-if="analysisType !== 'training'" @click="sendQuickPrompt('请分析我的饮食习惯，帮我优化营养搭配')">
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

      <el-col :xs="24" :sm="24" :md="10" :span="10" class="col-panel result-panel">
        <el-card shadow="never" class="result-card glass-panel">
          <div ref="resultAnchorRef" class="result-anchor"></div>

          <div v-if="!currentAnalysis && !isAiThinking" class="empty-state">
            <div class="artifact-icon">🏋️</div>
            <h3>训练看板预览</h3>
            <p class="empty-hint">告诉我你今天的饮食或训练，我将在这里为你生成专业分析。</p>
            <div class="empty-suggestions">
              <div class="suggestion-item" @click="switchToAnalysisMode('training')">
                <el-icon><TrendCharts /></el-icon>
                <span>分析训练表现</span>
              </div>
              <div class="suggestion-item" @click="switchToAnalysisMode('diet')">
                <el-icon><Food /></el-icon>
                <span>查看饮食分析</span>
              </div>
              <div class="suggestion-item" @click="switchToAnalysisMode('comprehensive')">
                <el-icon><Trophy /></el-icon>
                <span>制定训练计划</span>
              </div>
            </div>
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
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Position, Refresh, Loading, TrendCharts,
  Food, IceTea, Setting, Clock,
  Trophy, DataAnalysis, InfoFilled, MoreFilled, Delete, View, ChatLineSquare
} from '@element-plus/icons-vue'
import { aiCoachApi } from '@/api/aiCoach'
import { marked } from 'marked'

// 配置 marked 选项
marked.setOptions({
  breaks: true,
  gfm: true,
})

const chatWindowRef = ref(null)
const resultAnchorRef = ref(null)
const chatAnchorRef = ref(null)

const isMobile = ref(false)
const updateIsMobile = () => {
  isMobile.value = window.matchMedia('(max-width: 768px)').matches
}

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
  { role: 'ai', type: 'text', content: '你好！我是你的专属 AI 健身教练 💪 今天训练了吗？告诉我你的训练或饮食情况，我来帮你做专业分析！' }
])

// 会话 ID（用于保持上下文）
const currentSessionId = ref(null)

// 当前会话的分析类型
const currentSessionAnalysisType = ref('comprehensive')

// 当前分析结果
const currentAnalysis = ref(null)

// localStorage 持久化
const STORAGE_KEY = 'ai_coach_session_state'

// 保存会话状态到 localStorage
const saveSessionState = () => {
  if (currentSessionId.value) {
    const state = {
      sessionId: currentSessionId.value,
      analysisType: analysisType.value,
      analysisResult: currentAnalysis.value
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
  } else {
    localStorage.removeItem(STORAGE_KEY)
  }
}

// 从 localStorage 恢复会话状态
const restoreSessionState = () => {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved) {
    try {
      const state = JSON.parse(saved)
      if (state.sessionId) {
        currentSessionId.value = state.sessionId
        analysisType.value = state.analysisType || 'comprehensive'
        currentSessionAnalysisType.value = state.analysisType || 'comprehensive'
        currentAnalysis.value = state.analysisResult || null
        return true
      }
    } catch (e) {
      console.error('恢复会话状态失败:', e)
    }
  }
  return false
}

// Loading 进度状态
const loadingProgress = ref(0)
const loadingProgressText = ref('正在准备分析...')

// 会话列表
const sessionList = ref([])
const loadingSessions = ref(false)

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
    comprehensive: '综合分析模式：全面分析训练和饮食数据',
    chat: '闲聊模式：轻松聊天，无需数据分析'
  }
  return tips[analysisType.value]
})

// 直接使用后端返回的 analysisType（后端已根据实际数据判断报告类型）
const getAnalysisTypeColor = computed(() => {
  const colors = {
    training: '#409EFF',
    diet: '#67C23A',
    comprehensive: '#E6A23C',
    chat: '#909399'
  }
  return colors[analysisType.value]
})

const getAnalysisTypeIcon = computed(() => {
  const icons = {
    training: Trophy,
    diet: IceTea,
    comprehensive: DataAnalysis,
    chat: ChatLineSquare
  }
  return icons[analysisType.value]
})

const getAnalysisTypeTitle = computed(() => {
  return '分析报告'
})

const getAnalysisTypeLabel = computed(() => {
  const labels = {
    training: '训练分析',
    diet: '饮食分析',
    comprehensive: '综合分析',
    chat: '闲聊模式'
  }
  return labels[analysisType.value]
})

const getAnalysisTypeTag = computed(() => {
  const tags = {
    training: '',
    diet: 'success',
    comprehensive: 'warning',
    chat: 'info'
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

// 加载会话列表
const loadSessions = async () => {
  loadingSessions.value = true
  try {
    const res = await aiCoachApi.getSessionDetails(20)
    sessionList.value = res || []
  } catch (error) {
    console.error('加载会话列表失败:', error)
  } finally {
    loadingSessions.value = false
  }
}

// 恢复会话
const restoreSession = async (session) => {
  if (currentSessionId.value === session.sessionId) {
    if (isMobile.value) {
      await nextTick()
      chatAnchorRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
    return
  }
  
  // 先清空右侧分析结果，确保不会显示旧内容
  currentAnalysis.value = null
  // 清空消息列表
  messageList.value = []
  
  try {
    // 加载该会话的聊天历史
    const history = await aiCoachApi.getChatHistory(session.sessionId, 50)
    
    // 设置当前会话 ID 和分析类型
    currentSessionId.value = session.sessionId
    analysisType.value = session.analysisType || 'comprehensive'
    currentSessionAnalysisType.value = session.analysisType || 'comprehensive'
    
    // 判断是否为闲聊类型：只有当 analysisType === 'chat' 时才是闲聊
    const isChatSession = session.analysisType === 'chat'
    
    // 清空分析结果
    currentAnalysis.value = null
    
    if (history && history.length > 0) {
      for (let i = 0; i < history.length; i++) {
        const msg = history[i]
        if (msg.role === 'user') {
          messageList.value.push({ role: 'user', type: 'text', content: msg.content })
        } else if (msg.role === 'assistant') {
          // 所有类型统一显示"已生成回复"，点击查看详细内容
          // msg.replyTo 是该 AI 回复所对应的用户提问 ID，后端用它来查询 WHERE reply_to = ?
          messageList.value.push({ 
            role: 'ai', 
            type: 'text', 
            content: '💬 已生成回复',
            questionId: msg.replyTo // 使用 replyTo 字段（即用户提问的 ID）
          })
        }
      }
    } else {
      messageList.value.push({ role: 'ai', type: 'text', content: '你好！我是你的专属 AI 综合私教。请选择分析模式和数据范围，然后输入你的问题。' })
    }
    
    // 滚动到最新消息
    scrollToBottom()
    if (isMobile.value) {
      await nextTick()
      chatAnchorRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
    
    ElMessage.success('已恢复对话')
  } catch (error) {
    console.error('恢复会话失败:', error)
    messageList.value = [{ role: 'ai', type: 'text', content: '恢复对话失败，请重试。' }]
    currentAnalysis.value = null
    ElMessage.error('恢复对话失败')
  }
}

// 处理会话操作
const handleSessionCommand = async (command, session) => {
  if (command === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await aiCoachApi.deleteSession(session.sessionId)
      
      // 如果删除的是当前会话，清空
      if (currentSessionId.value === session.sessionId) {
        startNewChat()
      }
      
      // 刷新会话列表
      await loadSessions()
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除会话失败:', error)
        ElMessage.error('删除失败')
      }
    }
  }
}

// 开始新对话
const startNewChat = () => {
  // 重置会话 ID，开启新对话
  currentSessionId.value = null
  messageList.value = [
    { role: 'ai', type: 'text', content: '你好！我是你的专属 AI 综合私教。我可以帮你分析训练和饮食数据，给出专业建议。请选择分析模式和数据范围，然后输入你的问题。' }
  ]
  currentAnalysis.value = null
  analysisType.value = 'comprehensive'
  currentSessionAnalysisType.value = 'comprehensive'
  // 清除 localStorage
  localStorage.removeItem(STORAGE_KEY)
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  
  return date.toLocaleDateString()
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatWindowRef.value) {
    chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight
  }
}

// 切换到指定分析模式并自动发送快捷提示
const switchToAnalysisMode = (mode) => {
  // 切换分析模式
  analysisType.value = mode
  currentSessionAnalysisType.value = mode
  
  // 根据模式设置对应的快捷提示语
  const prompts = {
    training: '请分析我最近的训练表现，给出改进建议',
    diet: '请分析我的饮食习惯，帮我优化营养搭配',
    comprehensive: '给我一个本周的训练计划'
  }
  
  const prompt = prompts[mode]
  if (prompt) {
    sendQuickPrompt(prompt)
  }
}

const sendQuickPrompt = (text) => {
  inputText.value = text
  handleSend()
}

// 点击提问消息，获取对应的 AI 回复并展示在右侧面板
const handleQuestionClick = async (questionId) => {
  if (!currentSessionId.value) {
    ElMessage.warning('当前没有活跃的会话')
    return
  }
  
  // 设置加载状态
  isAiThinking.value = true
  currentAnalysis.value = null
  
  try {
    const reply = await aiCoachApi.getAssistantReply(currentSessionId.value, questionId)
    
    if (reply && reply.content) {
      // 解析 AI 回复内容（JSON 格式）
      try {
        const parsedContent = JSON.parse(reply.content)
        currentAnalysis.value = {
          analysisResult: parsedContent.analysisResult || parsedContent.result || reply.content,
          trainingDataSummary: parsedContent.trainingDataSummary || null,
          dietDataSummary: parsedContent.dietDataSummary || null
        }
        // 更新分析类型显示
        if (reply.analysisType) {
          analysisType.value = reply.analysisType
        }
      } catch (parseError) {
        // 如果不是 JSON，直接作为文本展示
        currentAnalysis.value = {
          analysisResult: reply.content
        }
      }
      ElMessage.success('已加载历史分析结果')
      if (isMobile.value) {
        await nextTick()
        resultAnchorRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }
    } else {
      ElMessage.warning('未找到该提问对应的 AI 分析结果')
      currentAnalysis.value = null
    }
  } catch (error) {
    console.error('获取 AI 回复失败:', error)
    ElMessage.error('获取分析结果失败')
    currentAnalysis.value = null
  } finally {
    isAiThinking.value = false
  }
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
  messageList.value.push({ role: 'ai', type: 'thinking', content: '让我想想...' })
  scrollToBottom()

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

    // 移除思考中消息
    messageList.value.pop()

    // 判断响应类型
    if (response.responseType === 'chat') {
      // 闲聊回复：显示简短提示，点击可查看回复
      messageList.value.push({
        role: 'ai',
        type: 'text',
        content: '💬 已生成回复',
        questionId: response.questionId // 使用后端返回的用户提问消息 ID
      })
      // 保存回复内容到右侧
      currentAnalysis.value = {
        analysisResult: response.analysisResult || '好的，有什么我可以帮助你的吗？'
      }
    } else {
      // 分析回复：开始 loading 动画，显示分析结果
      // 添加思考中消息用于 loading
      messageList.value.push({ role: 'ai', type: 'thinking', content: '正在调取您的数据...' })
      scrollToBottom()

      // 开始 Loading 动画
      startLoadingAnimation()

      // 等待一小段时间让动画显示
      await new Promise(resolve => setTimeout(resolve, 500))

      // 停止 Loading 动画
      stopLoadingAnimation()

      // 移除思考中消息
      messageList.value.pop()

      // 添加 AI 回复 - 显示简短提示，点击可查看回复
      messageList.value.push({
        role: 'ai',
        type: 'text',
        content: '💬 已生成回复',
        questionId: response.questionId // 使用后端返回的用户提问消息 ID
      })

      // 保存分析结果和会话 ID
      currentAnalysis.value = response
      // 更新报告类型（使用后端返回的实际分析类型，而不是用户选择的模式）
      if (response.analysisType) {
        analysisType.value = response.analysisType
      }
      ElMessage.success('分析完成')
    }

    // 保存会话 ID
    if (response.sessionId) {
      currentSessionId.value = response.sessionId
      // 保存状态到 localStorage
      saveSessionState()
      // 刷新会话列表
      loadSessions()
    }
  } catch (error) {
    console.error('AI 分析失败:', error)
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

// 组件挂载时加载会话列表并恢复状态
onMounted(async () => {
  updateIsMobile()
  window.addEventListener('resize', updateIsMobile)
  await loadSessions()
  
  // 尝试恢复之前的会话状态
  const restored = restoreSessionState()
  if (restored && currentSessionId.value) {
    // 先清空右侧分析结果，确保不会显示旧内容
    currentAnalysis.value = null
    messageList.value = []
    
    // 异步加载聊天历史，但不阻塞页面
    try {
      const history = await aiCoachApi.getChatHistory(currentSessionId.value, 50)
      if (history && history.length > 0) {
        // 判断是否为闲聊类型：只有当 analysisType === 'chat' 时才是闲聊
        const isChatSession = analysisType.value === 'chat'
        
        for (const msg of history) {
          if (msg.role === 'user') {
            messageList.value.push({ role: 'user', type: 'text', content: msg.content })
          } else if (msg.role === 'assistant') {
            // 所有类型统一显示"已生成回复"，点击查看详细内容
            // msg.replyTo 是该 AI 回复所对应的用户提问 ID，后端用它来查询 WHERE reply_to = ?
            messageList.value.push({ 
              role: 'ai', 
              type: 'text', 
              content: '💬 已生成回复',
              questionId: msg.replyTo // 使用 replyTo 字段（即用户提问的 ID）
            })
          }
        }
        
        // 滚动到最新消息
        scrollToBottom()
      }
    } catch (e) {
      console.error('恢复聊天历史失败:', e)
      messageList.value = [{ role: 'ai', type: 'text', content: '恢复对话失败，请刷新页面重试。' }]
      currentAnalysis.value = null
    }
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', updateIsMobile)
})
</script>

<style scoped>
.agent-container {
  max-width: 1600px;
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

.sidebar-panel {
  max-width: 280px;
  flex: 0 0 280px;
}

.glass-panel {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.05);
}

.history-card {
  border-radius: 16px;
  overflow: hidden;
}

.history-card :deep(.el-card__header) {
  padding: 14px 16px;
  border-bottom: 1px solid #f0f2f5;
}

.history-card :deep(.el-card__body) {
  padding: 0;
  flex: 1;
  overflow: hidden;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.history-list {
  height: calc(100vh - 200px);
  overflow-y: auto;
  padding: 8px;
}

.empty-history {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.session-item:hover {
  background-color: #f5f7fa;
}

.session-item.active {
  background-color: #ecf5ff;
  border-left: 3px solid #409EFF;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-size: 13px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.session-meta {
  font-size: 11px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.session-action {
  color: #909399;
  cursor: pointer;
  padding: 4px;
}

.session-action:hover {
  color: #409EFF;
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

.message-item.user .message-bubble.clickable {
  cursor: pointer;
  transition: all 0.2s;
}

.message-item.user .message-bubble.clickable:hover {
  transform: scale(1.02);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
}

/* AI 消息可点击样式 */
.message-item.ai .message-bubble.clickable {
  cursor: pointer;
  transition: all 0.2s;
  border-left-width: 4px;
}

.message-item.ai .message-bubble.clickable:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
  border-left-color: #67C23A;
}

.click-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
  opacity: 0.7;
}

.message-item.user:hover .click-hint,
.message-item.ai:hover .click-hint {
  opacity: 1;
  color: #409EFF;
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

.result-anchor {
  height: 0;
}

.chat-anchor {
  height: 0;
}

@media (max-width: 768px) {
  .agent-container {
    max-width: none;
    height: auto;
    padding-bottom: 24px;
  }

  .layout-row {
    height: auto;
    flex-wrap: wrap;
    margin-left: 0;
    margin-right: 0;
  }

  .col-panel {
    height: auto;
    padding-left: 0;
    padding-right: 0;
    margin-bottom: 12px;
  }

  .sidebar-panel {
    max-width: none;
    width: 100%;
    flex: 0 0 100%;
    order: 3;
  }

  .chat-panel {
    order: 1;
  }

  .result-panel {
    order: 2;
  }

  .history-card,
  .chat-card,
  .result-card {
    border-radius: 14px;
  }

  .history-list {
    height: auto;
    max-height: 200px;
    display: flex;
    gap: 10px;
    overflow-x: auto;
    overflow-y: hidden;
    padding: 10px 12px;
  }

  .history-card :deep(.el-card__header) {
    padding: 12px 14px;
  }

  .history-title {
    font-size: 15px;
  }

  .session-item {
    min-width: 200px;
    margin-bottom: 0;
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .session-info {
    width: 100%;
  }

  .session-title {
    font-size: 14px;
  }

  .session-action {
    align-self: flex-end;
  }

  .chat-header {
    padding: 12px 14px;
  }

  .analysis-options {
    padding: 0;
    margin: 10px 12px 0;
    border-radius: 12px;
    border: 1px solid #ebeef5;
    background: #ffffff;
  }

  .analysis-type-group :deep(.el-radio-group) {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  .analysis-type-group :deep(.el-radio-button) {
    width: 100%;
  }

  .analysis-type-group :deep(.el-radio-button__inner) {
    margin: 0;
  }

  .data-option-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .chat-window {
    padding: 14px;
    min-height: 40vh;
    margin: 12px 12px 0;
    border-radius: 12px;
    border: 1px solid #ebeef5;
    background: #fafbfc;
  }

  .message-item {
    gap: 12px;
    margin-bottom: 18px;
  }

  .message-bubble-wrapper {
    max-width: 100%;
  }

  .message-bubble {
    padding: 12px 14px;
    font-size: 14px;
  }

  .quick-prompts {
    padding: 0 14px 14px;
  }

  .chat-input-area {
    padding: 14px;
    margin: 12px 12px 0;
    border-radius: 12px;
    border: 1px solid #ebeef5;
    background: #ffffff;
  }

  .input-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .send-btn {
    width: 100%;
  }
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

.empty-hint {
  font-size: 14px;
  color: #606266;
  text-align: center;
  max-width: 300px;
  margin-bottom: 24px;
  line-height: 1.6;
}

.empty-suggestions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 280px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #303133;
}

.suggestion-item:hover {
  border-color: #409EFF;
  background: #ecf5ff;
  transform: translateX(4px);
}

.suggestion-item .el-icon {
  font-size: 20px;
  color: #409EFF;
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
