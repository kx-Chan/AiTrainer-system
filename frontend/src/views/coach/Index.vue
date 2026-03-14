<template>
  <div class="agent-container">
    <el-row :gutter="24" class="layout-row">
      
      <el-col :span="12" class="col-panel">
        <el-card shadow="never" class="chat-card glass-panel">
          <template #header>
            <div class="chat-header">
              <div class="agent-title">
                <div class="ai-avatar-wrapper">
                  <el-avatar 
                    :size="36" 
                    :src="currentRole === 'coach' ? 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png' : 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
                  />
                  <div class="online-dot"></div>
                </div>
                <div class="title-text">
                  <span class="gradient-text">{{ currentRole === 'coach' ? '铁面硬核私教' : '贴心营养管家' }}</span>
                  <span class="sub-text">AiTrainer Multi-Agent 系统</span>
                </div>
              </div>
              <el-button plain round size="small" @click="resetChat"><el-icon><Refresh /></el-icon> 新对话</el-button>
            </div>
          </template>

          <div class="role-selector-bar">
            <el-radio-group v-model="currentRole" size="default" @change="handleRoleChange">
              <el-radio-button label="coach">
                <el-icon><User /></el-icon> 运动课表模式
              </el-radio-button>
              <el-radio-button label="nutritionist">
                <el-icon><IceTea /></el-icon> 营养膳食模式
              </el-radio-button>
            </el-radio-group>
          </div>

          <div class="chat-window" ref="chatWindowRef">
            <div v-for="(msg, index) in messageList" :key="index" :class="['message-item', msg.role]">
              <div class="message-avatar" v-if="msg.role === 'ai'">
                <el-avatar :size="40" :src="currentRole === 'coach' ? 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png' : 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              </div>
              <div class="message-bubble-wrapper">
                <div v-if="msg.type === 'thinking'" class="thinking-box">
                  <el-icon class="is-loading"><Loading /></el-icon> {{ msg.content }}
                </div>
                <div v-else class="message-bubble" v-html="msg.content"></div>
              </div>
            </div>
          </div>

          <div class="quick-prompts" v-if="messageList.length === 1">
            <div class="prompt-card" @click="sendQuickPrompt('分析我本周的深蹲数据，并生成专属课表')">
              <el-icon size="20" color="#409EFF"><TrendCharts /></el-icon>
              <span>分析数据并生成课表</span>
            </div>
            <div class="prompt-card" @click="sendQuickPrompt('我只有宿舍养生壶，帮我配一份减脂晚餐')">
              <el-icon size="20" color="#E6A23C"><Food /></el-icon>
              <span>宿舍党专属减脂餐</span>
            </div>
          </div>

          <div class="chat-input-area">
            <el-input 
              v-model="inputText" 
              type="textarea"
              :autosize="{ minRows: 2, maxRows: 4 }"
              :placeholder="currentRole === 'coach' ? '问问动作要领...' : '这个能吃吗？怎么配餐？'" 
              @keydown.enter.prevent="handleSend"
              :disabled="isAiThinking"
              class="mac-input"
            />
            <div class="input-actions">
              <span class="tip-text">当前模式：{{ currentRole === 'coach' ? '训练增强' : '营养管理' }}</span>
              <el-button type="primary" round size="large" :disabled="!inputText || isAiThinking" @click="handleSend" class="send-btn">
                发送 <el-icon class="el-icon--right"><Position /></el-icon>
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" class="col-panel">
        <el-card shadow="never" class="result-card glass-panel">
          
          <div v-if="!generatedPlan" class="empty-state">
            <div class="artifact-icon">✨</div>
            <h3>AiTrainer Artifacts</h3>
            <p>生成的训练计划、图表和食谱将在这里结构化呈现</p>
          </div>

          <div v-else class="generated-content slide-in">
            <div class="result-header">
              <h2><el-icon color="#409EFF"><DocumentChecked /></el-icon> 您的专属定制方案</h2>
              <el-button type="primary" plain round size="small"><el-icon><Download /></el-icon> 导出至手机</el-button>
            </div>

            <el-tabs v-model="activePlanTab" class="custom-tabs">
              <el-tab-pane label="🏋️ 智能训练课表" name="workout">
                <el-alert title="AI 洞察：您的数据反馈显示近期下肢力量处于增长期，已为您调整容量。" type="success" :closable="false" style="margin-bottom: 24px;" />
                <el-timeline>
                  <el-timeline-item v-for="(day, index) in generatedPlan.workouts" :key="index" :timestamp="day.date" placement="top" :type="day.type">
                    <el-card shadow="hover" class="plan-card">
                      <h4>{{ day.title }}</h4>
                      <div class="plan-details">
                        <el-tag v-for="action in day.actions" :key="action" size="default" type="info" effect="plain" class="action-tag">{{ action }}</el-tag>
                      </div>
                      <p class="plan-tip"><el-icon color="#E6A23C"><Warning /></el-icon> {{ day.tip }}</p>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </el-tab-pane>

              <el-tab-pane label="🥗 营养与饮食" name="diet">
                <el-row :gutter="16">
                  <el-col :span="24" v-for="meal in generatedPlan.diets" :key="meal.name" style="margin-bottom: 16px;">
                    <el-card shadow="hover" class="meal-card" :class="meal.theme">
                      <div class="meal-header">
                        <span class="meal-name">{{ meal.name }}</span>
                        <span class="meal-calo">{{ meal.calo }} kcal</span>
                      </div>
                      <ul class="meal-items">
                        <li v-for="item in meal.items" :key="item">{{ item }}</li>
                      </ul>
                    </el-card>
                  </el-col>
                </el-row>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Position, Refresh, Loading, TrendCharts, Download, 
  Warning, Food, DocumentChecked, User, IceTea 
} from '@element-plus/icons-vue'

const route = useRoute()
const chatWindowRef = ref(null)

const inputText = ref('')
const isAiThinking = ref(false)
const activePlanTab = ref('workout')
const currentRole = ref('coach') // 🏆 新增：默认角色为教练

const messageList = reactive([
  { role: 'ai', type: 'text', content: '你好！我是你的专属 AI 教练。我已经接入了你最近 30 天的体征数据和战报。今天想让我帮你分析什么？' }
])

const generatedPlan = ref(null)

// 🏆 新增：切换角色时的交互
const handleRoleChange = (role) => {
  if (role === 'coach') {
    ElMessage({ message: '已切换至【铁面私教】模式：专注于动作规范与训练强度。', type: 'primary', plain: true })
    activePlanTab.value = 'workout' // 自动切换右侧面板
  } else {
    ElMessage({ message: '已切换至【营养管家】模式：专注于每日热量与精准配餐。', type: 'success', plain: true })
    activePlanTab.value = 'diet' // 自动切换右侧面板
  }
}

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

const handleSend = () => {
  if (!inputText.value.trim() || isAiThinking.value) return
  
  const userText = inputText.value
  messageList.push({ role: 'user', type: 'text', content: userText })
  inputText.value = ''
  isAiThinking.value = true
  scrollToBottom()

  setTimeout(() => {
    messageList.push({ role: 'ai', type: 'thinking', content: `Agent 正在调取您的${currentRole.value === 'coach' ? '动作训练' : '饮食补剂'}数据流水...` })
    scrollToBottom()
    
    setTimeout(() => {
      messageList.pop() 
      messageList.push({ role: 'ai', type: 'thinking', content: `正在调用 ${currentRole.value === 'coach' ? 'Vision-Coach' : 'Diet-Master'} 模型生成深度解析方案...` })
      scrollToBottom()
      
      setTimeout(() => {
        messageList.pop()
        messageList.push({ 
          role: 'ai', 
          type: 'text', 
          content: currentRole.value === 'coach' 
            ? '分析完毕！我已经针对你的下蹲深度问题定制了<strong>突破性训练方案</strong>，请查看右侧面板。' 
            : '膳食解析完毕！由于你目前处于减脂平台期，我为你优化了<strong>碳水循环食谱</strong>，已在右侧同步更新。' 
        })
        isAiThinking.value = false
        scrollToBottom()
        renderRightPanel()
      }, 2000)
    }, 1500)
  }, 1000)
}

const resetChat = () => {
  messageList.splice(1)
  generatedPlan.value = null
}

const renderRightPanel = () => {
  generatedPlan.value = {
    workouts: [
      { date: '明天 (突破日)', type: 'warning', title: '下肢爆发力与深度强行突破', actions: ['AI 杠铃深蹲 5x10', '保加利亚分腿蹲 4x12'], tip: '核心目标：增加下蹲行程。' },
      { date: '后天 (消耗日)', type: 'primary', title: '高强度 HIIT', actions: ['波比跳 20x4', '登山跑 30s x4'], tip: '打破代谢停滞的关键。' }
    ],
    diets: [
      { name: '早餐 (高蛋白)', theme: 'bg-blue', calo: 350, items: ['无糖脱脂牛奶 250ml', '全麦吐司 1 片', '水煮蛋 2 个'] },
      { name: '午餐 (控制碳水)', theme: 'bg-green', calo: 550, items: ['鸡胸肉 150g', '西兰花 200g', '紫薯 100g'] }
    ]
  }
}

onMounted(() => {
  if (route.query.auto === 'bottleneck') {
    setTimeout(() => {
      sendQuickPrompt('分析我最近的训练瓶颈，并生成突破计划！')
    }, 500)
  }
})
</script>

<style scoped>
.agent-container { max-width: 1400px; margin: 0 auto; height: calc(100vh - 100px); padding-bottom: 20px; }
.layout-row { height: 100%; display: flex; flex-wrap: nowrap; margin-left: -12px; margin-right: -12px; }
.col-panel { height: 100%; display: flex; flex-direction: column; padding-left: 12px; padding-right: 12px; }

.glass-panel {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.05);
}

.chat-card { flex: 1; display: flex; flex-direction: column; border-radius: 16px; overflow: hidden; }
.chat-card :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; padding: 0; overflow: hidden; }

.chat-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #f0f2f5; }

/* ================= 角色切换栏：文字显形修复 ================= */
.role-selector-bar {
  background-color: #fcfdfe;
  padding: 12px;
  display: flex;
  justify-content: center;
  border-bottom: 1px solid #f0f2f5;
  z-index: 10;
}

/* 1. 基础样式：未选中时的样子 */
:deep(.el-radio-button__inner) {
  border: 1px solid #dcdfe6 !important;
  background: #ffffff !important;
  color: #606266 !important; /* 默认灰色文字 */
  border-radius: 8px !important;
  margin: 0 6px;
  padding: 8px 20px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: none !important;
}

/* 2. 移除按钮之间的粘连感 */
:deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-left: 1px solid #dcdfe6 !important;
  border-radius: 8px !important;
}

/* 3. 核心修复：无论选中哪个模式，都呈现“绿色选中 + 绿色文字” */
/* .is-active 是选中的状态，我们强制让它的文字颜色变为深绿色 */
:deep(.el-radio-button.is-active .el-radio-button__inner) {
  background-color: #f0f9eb !important; /* 浅绿色背景 */
  border-color: #67C23A !important;     /* 绿色边框 */
  color: #67C23A !important;            /* 🚨 关键：强制显示绿色字体，解决消失问题 */
}

/* 4. 鼠标浮动上去的微光效果（可选） */
:deep(.el-radio-button__inner:hover) {
  color: #67C23A;
  background-color: #f9fdf8 !important;
}

.ai-avatar-wrapper { position: relative; }
.online-dot { position: absolute; bottom: 0; right: 0; width: 10px; height: 10px; background-color: #67C23A; border-radius: 50%; border: 2px solid #fff; }
.agent-title { display: flex; align-items: center; gap: 12px; }
.title-text { display: flex; flex-direction: column; }
.gradient-text { font-size: 16px; font-weight: 900; background: linear-gradient(45deg, #409EFF, #8a2be2); -webkit-background-clip: text; color: transparent; transition: all 0.3s; }
.sub-text { font-size: 11px; color: #909399; text-transform: uppercase; letter-spacing: 1px; }

.chat-window { flex: 1; overflow-y: auto; padding: 24px; background-color: #fafbfc; }
.message-item { display: flex; gap: 16px; margin-bottom: 30px; }
.message-item.user { flex-direction: row-reverse; }

.message-bubble-wrapper { max-width: 80%; display: flex; flex-direction: column; align-items: flex-start; }
.message-item.user .message-bubble-wrapper { align-items: flex-end; }

.message-bubble { padding: 14px 20px; border-radius: 16px; font-size: 15px; line-height: 1.6; }
.message-item.ai .message-bubble { 
  background-color: #ffffff; 
  border: 1px solid #ebeef5; 
  border-top-left-radius: 4px; 
  color: #303133; 
  box-shadow: 0 2px 12px rgba(0,0,0,0.02);
  /* 🏆 核心：通过 v-bind 实现动态边框颜色 */
  border-left: 4px solid v-bind("currentRole === 'coach' ? '#409EFF' : '#67C23A'");
  transition: border-left-color 0.5s ease;
}
.message-item.user .message-bubble { background-color: #409EFF; color: #ffffff; border-top-right-radius: 4px; box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2); }

.thinking-box { padding: 12px 16px; background-color: #fffaf0; border-radius: 8px; border-left: 4px solid #E6A23C; color: #E6A23C; font-size: 13px; display: flex; align-items: center; gap: 8px; }

.quick-prompts { display: flex; gap: 12px; padding: 0 24px 20px; background-color: #fafbfc; }
.prompt-card { flex: 1; background: #fff; border: 1px solid #ebeef5; border-radius: 12px; padding: 16px; cursor: pointer; transition: all 0.3s; display: flex; flex-direction: column; gap: 8px; }
.prompt-card:hover { border-color: #409EFF; box-shadow: 0 4px 12px rgba(64,158,255,0.1); transform: translateY(-2px); }
.prompt-card span { font-size: 13px; color: #606266; font-weight: 500; }

.chat-input-area { padding: 20px 24px; background-color: #ffffff; border-top: 1px solid #ebeef5; }
.mac-input :deep(.el-textarea__inner) { background-color: #f5f7fa; border: none; border-radius: 12px; padding: 16px; font-size: 15px; resize: none; }
.input-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.tip-text { font-size: 12px; color: #c0c4cc; }

.result-card { flex: 1; border-radius: 16px; overflow-y: auto; }
.empty-state { height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center; color: #909399; }
.artifact-icon { font-size: 48px; margin-bottom: 20px; animation: float 3s ease-in-out infinite; }
@keyframes float { 0% { transform: translateY(0px); } 50% { transform: translateY(-10px); } 100% { transform: translateY(0px); } }

.generated-content { padding: 30px; }
.slide-in { animation: slideInUp 0.6s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes slideInUp { from { opacity: 0; transform: translateY(40px); } to { opacity: 1; transform: translateY(0); } }

.result-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.plan-card { border-radius: 12px; margin-bottom: 16px; border-left: 4px solid #409EFF; }
.plan-details { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 12px; }
.plan-tip { font-size: 13px; color: #606266; display: flex; align-items: center; gap: 6px; background: #fff8e6; padding: 8px 12px; border-radius: 6px; }

.meal-card { border-radius: 12px; color: #fff; border: none; padding: 18px; }
.bg-blue { background: linear-gradient(135deg, #7ec0ee 0%, #409EFF 100%); }
.bg-green { background: linear-gradient(135deg, #a8e063 0%, #56ab2f 100%); }
.meal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; border-bottom: 1px solid rgba(255,255,255,0.2); padding-bottom: 10px; }
.meal-items { padding-left: 20px; margin: 0; font-size: 14px; line-height: 1.8; }
</style>