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
                  <span class="gradient-text">AiTrainer Copilot</span>
                  <span class="sub-text">你的全天候智能私教</span>
                </div>
              </div>
              <el-button plain round size="small" @click="resetChat"><el-icon><Refresh /></el-icon> 新对话</el-button>
            </div>
          </template>

          <div class="chat-window" ref="chatWindowRef">
            <div v-for="(msg, index) in messageList" :key="index" :class="['message-item', msg.role]">
              <div class="message-avatar" v-if="msg.role === 'ai'">
                <el-avatar :size="40" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
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
              placeholder="向 AiTrainer 提问，或让它为你生成专属计划..." 
              @keydown.enter.prevent="handleSend"
              :disabled="isAiThinking"
              class="mac-input"
            />
            <div class="input-actions">
              <span class="tip-text">按 Enter 发送，Shift + Enter 换行</span>
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
                <el-alert title="AI 洞察：您的深蹲下蹲深度普遍偏高，核心稳定性良好。本计划已加强下肢爆发力训练。" type="success" :closable="false" style="margin-bottom: 24px;" />
                
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
import { Position, Refresh, Loading, TrendCharts, Download, Warning, Food, DocumentChecked } from '@element-plus/icons-vue'

const route = useRoute()
const chatWindowRef = ref(null)

const inputText = ref('')
const isAiThinking = ref(false)
const activePlanTab = ref('workout')

const messageList = reactive([
  { role: 'ai', type: 'text', content: '你好！我是你的专属 AI 教练。我已经接入了你最近 30 天的体征数据和战报。今天想让我帮你分析什么？' }
])

const generatedPlan = ref(null)

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
    messageList.push({ role: 'ai', type: 'thinking', content: 'Agent 正在调取数据看板近 7 天的体征和战报流水...' })
    scrollToBottom()
    
    setTimeout(() => {
      messageList.pop() 
      messageList.push({ role: 'ai', type: 'thinking', content: '正在调用大模型进行多模态瓶颈分析并生成定制课表...' })
      scrollToBottom()
      
      setTimeout(() => {
        messageList.pop()
        // 修复了加粗文本的渲染问题，使用了 <strong> 标签
        messageList.push({ 
          role: 'ai', 
          type: 'text', 
          content: '分析完毕！结合你近期体重下降停滞（进入平台期），以及深蹲战报中呈现的下蹲深度不足问题，我为你定制了<strong>突破瓶颈期的专项训练与饮食计划</strong>。<br/><br/><strong>请在右侧专属面板查看详细方案。</strong>' 
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
      { date: '明天 (突破日)', type: 'warning', title: '下肢爆发力与深度强行突破', actions: ['AI 杠铃深蹲 (轻重量深蹲) 5x10', '保加利亚分腿蹲 4x12'], tip: '首要目标：通过 AI 纠错确保每次下蹲达到水平面以下。' },
      { date: '后天 (消耗日)', type: 'primary', title: '高强度间歇心肺循环 (HIIT)', actions: ['波比跳 4x20次', '登山跑 4x30秒', '战绳 4x30秒'], tip: '打破平台期的关键，心率需冲刺到 160 bpm 以上。' },
      { date: '大后天 (拉伸日)', type: 'info', title: '有氧与筋膜放松', actions: ['椭圆机 40min', '下肢泡沫轴滚压 15min'], tip: '加速乳酸代谢，为下一轮循环做准备。' }
    ],
    diets: [
      { name: '早餐 (欺骗餐后清肠)', theme: 'bg-blue', calo: 350, items: ['无糖脱脂牛奶 250ml', '全麦吐司 1 片', '圣女果 10 颗'] },
      { name: '午餐 (控制碳水)', theme: 'bg-green', calo: 550, items: ['蒸紫薯 100g (主食减半)', '清炒菠菜 200g', '水煮虾仁 150g'] },
      { name: '晚餐 (宿舍简易版)', theme: 'bg-orange', calo: 300, items: ['无糖脱脂酸奶 1 杯', '黄瓜 1 根', '水煮蛋 1 个 (去蛋黄)'] }
    ]
  }
}

onMounted(() => {
  if (route.query.auto === 'bottleneck') {
    setTimeout(() => {
      sendQuickPrompt('我点选了数据看板的魔法棒，请帮我分析近期的体征和训练瓶颈，并生成突破计划！')
    }, 500)
  }
})
</script>

<style scoped>
.agent-container { max-width: 1400px; margin: 0 auto; height: calc(100vh - 100px); padding-bottom: 20px; }

/* 关键修复：利用 Flexbox 强制横向排列且不换行 */
.layout-row { height: 100%; display: flex; flex-wrap: nowrap; margin-left: -12px; margin-right: -12px; }
.col-panel { height: 100%; display: flex; flex-direction: column; padding-left: 12px; padding-right: 12px; }

.glass-panel {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.05);
}

.chat-card { flex: 1; display: flex; flex-direction: column; border-radius: 16px; }
.chat-card :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; padding: 0; overflow: hidden; }

.chat-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #f0f2f5; }
.ai-avatar-wrapper { position: relative; }
.online-dot { position: absolute; bottom: 0; right: 0; width: 10px; height: 10px; background-color: #67C23A; border-radius: 50%; border: 2px solid #fff; }
.agent-title { display: flex; align-items: center; gap: 12px; }
.title-text { display: flex; flex-direction: column; }
.gradient-text { font-size: 16px; font-weight: 900; background: linear-gradient(45deg, #409EFF, #8a2be2); -webkit-background-clip: text; color: transparent; }
.sub-text { font-size: 12px; color: #909399; }

.chat-window { flex: 1; overflow-y: auto; padding: 24px; background-color: #fafbfc; }
.message-item { display: flex; gap: 16px; margin-bottom: 30px; }
.message-item.user { flex-direction: row-reverse; }

.message-bubble-wrapper { max-width: 80%; display: flex; flex-direction: column; align-items: flex-start; }
.message-item.user .message-bubble-wrapper { align-items: flex-end; }

.message-bubble { padding: 14px 20px; border-radius: 16px; font-size: 16px; line-height: 1.6; letter-spacing: 0.5px; }
.message-item.ai .message-bubble { background-color: #ffffff; border: 1px solid #ebeef5; border-top-left-radius: 4px; color: #303133; box-shadow: 0 2px 12px rgba(0,0,0,0.02); }
.message-item.user .message-bubble { background-color: #409EFF; color: #ffffff; border-top-right-radius: 4px; box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2); }

.thinking-box { padding: 12px 16px; background-color: #fffaf0; border-radius: 8px; border-left: 4px solid #E6A23C; color: #E6A23C; font-style: italic; font-size: 14px; display: flex; align-items: center; gap: 8px; }

.quick-prompts { display: flex; gap: 12px; padding: 0 24px 20px; background-color: #fafbfc; }
.prompt-card { flex: 1; background: #fff; border: 1px solid #ebeef5; border-radius: 12px; padding: 16px; cursor: pointer; transition: all 0.3s; display: flex; flex-direction: column; gap: 8px; }
.prompt-card:hover { border-color: #409EFF; box-shadow: 0 4px 12px rgba(64,158,255,0.1); transform: translateY(-2px); }
.prompt-card span { font-size: 14px; color: #606266; font-weight: 500; }

.chat-input-area { padding: 20px 24px; background-color: #ffffff; border-top: 1px solid #ebeef5; }
.mac-input :deep(.el-textarea__inner) { background-color: #f5f7fa; border: none; border-radius: 12px; padding: 16px; font-size: 15px; box-shadow: none; resize: none; }
.mac-input :deep(.el-textarea__inner:focus) { background-color: #fff; box-shadow: 0 0 0 1px #409EFF inset; }
.input-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.tip-text { font-size: 12px; color: #c0c4cc; }
.send-btn { padding: 0 24px; font-weight: bold; letter-spacing: 1px; }

.result-card { flex: 1; border-radius: 16px; overflow-y: auto; }

.empty-state { height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center; color: #909399; }
.artifact-icon { font-size: 48px; margin-bottom: 20px; animation: float 3s ease-in-out infinite; }
@keyframes float { 0% { transform: translateY(0px); } 50% { transform: translateY(-10px); } 100% { transform: translateY(0px); } }
.empty-state h3 { font-size: 24px; color: #303133; margin: 0 0 10px 0; }

.generated-content { padding: 30px; }
.slide-in { animation: slideInUp 0.6s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes slideInUp { from { opacity: 0; transform: translateY(40px); } to { opacity: 1; transform: translateY(0); } }

.result-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.result-header h2 { margin: 0; font-size: 22px; }

.custom-tabs :deep(.el-tabs__item) { font-size: 16px; height: 50px; line-height: 50px; }

.plan-card { border-radius: 12px; margin-bottom: 16px; border-left: 4px solid #409EFF; }
.plan-card h4 { margin: 0 0 16px 0; font-size: 18px; color: #303133; }
.plan-details { display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 16px; }
.action-tag { font-size: 14px; padding: 6px 12px; height: auto; border-radius: 6px; }
.plan-tip { margin: 0; font-size: 14px; color: #606266; display: flex; align-items: center; gap: 6px; background: #fff8e6; padding: 8px 12px; border-radius: 6px; }

.meal-card { border-radius: 12px; color: #fff; border: none; padding: 20px; }
.bg-blue { background: linear-gradient(135deg, #7ec0ee 0%, #409EFF 100%); }
.bg-green { background: linear-gradient(135deg, #a8e063 0%, #56ab2f 100%); }
.bg-orange { background: linear-gradient(135deg, #ffc371 0%, #ff5f6d 100%); }
.meal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; border-bottom: 1px solid rgba(255,255,255,0.2); padding-bottom: 12px; }
.meal-name { font-size: 20px; font-weight: bold; }
.meal-calo { font-size: 16px; font-weight: bold; background: rgba(0,0,0,0.2); padding: 4px 12px; border-radius: 20px; }
.meal-items { padding-left: 20px; margin: 0; font-size: 15px; line-height: 2; font-weight: 500; }
</style>