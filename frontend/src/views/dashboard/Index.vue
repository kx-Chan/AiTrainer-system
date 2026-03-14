<template>
  <div class="dashboard-container">
    
    <div class="page-header">
      <h2 class="page-title">数据看板 Dashboard</h2>
      <el-button type="primary" size="large" round class="report-btn" @click="generateWeeklyReport" :loading="isGenerating">
        <el-icon><Document /></el-icon> {{ isGenerating ? '正在生成...' : '生成本周健康周报' }}
      </el-button>
    </div>

    <el-row :gutter="24" class="metric-row">
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-title"><el-icon><Trophy /></el-icon> AI 训练总时长</div>
          <div class="metric-value">1,240 <span class="unit">分钟</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-title"><el-icon><DataLine /></el-icon> 动作达标率</div>
          <div class="metric-value">92 <span class="unit">%</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card diet-metric">
          <div class="metric-title"><el-icon><Food /></el-icon> 今日热量摄入</div>
          <div class="metric-value">1,850 <span class="unit">kcal</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card diet-metric">
          <div class="metric-title"><el-icon><Odometer /></el-icon> 蛋白质缺口</div>
          <div class="metric-value">15 <span class="unit">g</span></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="main-row">
      <el-col :span="12">
        <el-card shadow="never" class="split-card workout-card">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><Timer /></el-icon> 运动与消耗趋势</span>
              <el-button type="warning" plain size="small" round @click="goToAICoach">✨ AI 瓶颈分析</el-button>
            </div>
          </template>
          
          <div class="chart-placeholder">
            <div class="bar-chart">
              <div class="bar" style="height: 60%;" title="周一"></div>
              <div class="bar" style="height: 80%;" title="周二"></div>
              <div class="bar" style="height: 40%;" title="周三"></div>
              <div class="bar" style="height: 90%; background-color: #409EFF;" title="今日"></div>
            </div>
            <div class="chart-desc">近 7 天卡路里消耗趋势 (kcal)</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="split-card diet-card">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><PieChart /></el-icon> 营养摄入配比</span>
              <el-button type="success" plain size="small" round>📸 拍餐盘</el-button>
            </div>
          </template>
          
          <div class="macro-container">
            <div class="macro-item">
              <div class="macro-label">碳水化合物 (50%)</div>
              <el-progress :percentage="85" color="#E6A23C" :stroke-width="12" />
              <div class="macro-text">已摄入 180g / 目标 210g</div>
            </div>
            <div class="macro-item">
              <div class="macro-label">蛋白质 (30%)</div>
              <el-progress :percentage="90" color="#F56C6C" :stroke-width="12" />
              <div class="macro-text">已摄入 120g / 目标 135g</div>
            </div>
            <div class="macro-item">
              <div class="macro-label">脂肪 (20%)</div>
              <el-progress :percentage="60" color="#67C23A" :stroke-width="12" />
              <div class="macro-text">已摄入 45g / 目标 75g</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="log-row">
      <el-col :span="12">
        <el-card shadow="never" class="log-card">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><Calendar /></el-icon> 详细训练日志</span>
              <el-button link type="primary">查看全部</el-button>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item timestamp="今天 10:30" type="primary" hollow>
              <div class="log-content">
                <strong>杠铃深蹲 (Squat)</strong> - 5组x8次 
                <el-tag size="small" type="success" style="margin-left: 8px;">AI 战报 96 分</el-tag>
                <div class="log-detail">🔥 消耗 320 kcal · 核心稳定性极佳</div>
              </div>
            </el-timeline-item>
            <el-timeline-item timestamp="昨天 18:00" type="info" hollow>
              <div class="log-content">
                <strong>早安式体前屈</strong> - 4组x12次
                <div class="log-detail">🔥 消耗 150 kcal · 后侧链发力标准</div>
              </div>
            </el-timeline-item>
            <el-timeline-item timestamp="周三 20:00" type="warning" hollow>
              <div class="log-content">
                <strong>高强度 HIIT 循环</strong> - 20 分钟
                <div class="log-detail">🔥 消耗 300 kcal · 平均心率 155 bpm</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="log-card">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><Dish /></el-icon> 详细饮食日志</span>
              <el-button link type="success">查看全部</el-button>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item timestamp="今天 12:30 (午餐)" color="#67C23A" hollow>
              <div class="log-content">
                <strong>紫薯鸡胸肉减脂餐</strong>
                <el-tag size="small" type="warning" effect="plain" style="margin-left: 8px;">蛋白质丰富</el-tag>
                <div class="log-detail">🍚 摄入 650 kcal · 蛋白质 35g</div>
              </div>
            </el-timeline-item>
            <el-timeline-item timestamp="今天 08:00 (早餐)" color="#67C23A" hollow>
              <div class="log-content">
                <strong>脱脂牛奶 + 全麦面包</strong>
                <div class="log-detail">🍞 摄入 350 kcal · 碳水化合物优质</div>
              </div>
            </el-timeline-item>
            <el-timeline-item timestamp="昨天 20:00 (晚餐)" color="#909399" hollow>
              <div class="log-content">
                <strong>宿舍简易荞麦面</strong>
                <div class="log-detail">🍜 摄入 400 kcal · 钠含量略高</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <el-tooltip content="更新体征 (建议每周1次，早晨空腹最佳)" placement="left" effect="dark">
      <el-button type="warning" circle class="floating-record-btn" @click="isRecordVisible = true">
        <el-icon><DataBoard /></el-icon>
      </el-button>
    </el-tooltip>

    <el-dialog v-model="isRecordVisible" title="📊 阶段性体征复盘" width="450px" destroy-on-close>
      <el-alert 
        title="💡 健身先健脑：体重受水分影响波动极大，切勿每天称重制造焦虑。建议每周固定时间（如周末早晨空腹）记录一次即可，让 AI 捕捉长期趋势。" 
        type="warning" 
        :closable="false" 
        style="margin-bottom: 20px;" 
      />
      
      <el-form label-width="100px" :model="dailyRecord" label-position="left">
        <el-form-item label="复盘日期">
          <el-date-picker v-model="dailyRecord.date" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="当前体重(kg)">
          <el-input-number v-model="dailyRecord.weight" :precision="1" :step="0.5" :min="30" :max="200" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="预估体脂(%)">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-input-number v-model="dailyRecord.bodyFat" :precision="1" :step="0.5" :min="1" :max="50" style="flex: 1;" />
            <el-tooltip content="如果没有体脂秤，可根据镜子里的腹肌清晰度粗略估算">
              <el-button plain><el-icon><QuestionFilled /></el-icon></el-button>
            </el-tooltip>
          </div>
        </el-form-item>

        <el-form-item label="形体记录">
          <el-upload
            class="progress-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
          >
            <div class="upload-trigger">
              <el-icon class="upload-icon"><Camera /></el-icon>
              <span class="upload-text">点击上传本周身材照<br/>(仅自己可见)</span>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isRecordVisible = false">暂不更新</el-button>
          <el-button type="warning" @click="saveDailyRecord">保存并同步给 AI</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataBoard, QuestionFilled, Camera } from '@element-plus/icons-vue'
// 确保引入了所有的图标
import { Trophy, DataLine, Food, Odometer, Timer, PieChart, Document, Calendar, Dish } from '@element-plus/icons-vue'

// ================= 体征记录弹窗逻辑 =================
const isRecordVisible = ref(false)
const dailyRecord = reactive({
  date: new Date(), // 默认今天
  weight: 65.5,
  bodyFat: 18.5
})

const saveDailyRecord = () => {
  // 模拟发送数据给后端
  isRecordVisible.value = false
  ElMessage.success('今日体征已记录！后端 user_body_data 流水表已更新。')
  
  // 💡 面试亮点：在这里可以说“前端触发保存后，看板上的 ECharts 折线图会重新请求接口，实现无刷新动态重绘”
}

const router = useRouter()
const isGenerating = ref(false)

const goToAICoach = () => {
  router.push('/coach?auto=bottleneck')
}

// 模拟生成周报的交互逻辑
const generateWeeklyReport = () => {
  isGenerating.value = true
  ElMessage.success('正在调取本周数据，AI 正在深度分析中...')
  
  setTimeout(() => {
    isGenerating.value = false
    ElMessage({
      message: '周报生成完毕！即将跳转至 AI 私教解析页面...',
      type: 'success',
      duration: 3000
    })
    // 自动跳转到私教页面并带上周报参数
    setTimeout(() => {
      router.push('/coach?auto=weekly_report')
    }, 1500)
  }, 2000)
}
</script>

<style scoped>
.dashboard-container { max-width: 1400px; margin: 0 auto; padding-bottom: 40px; }

/* 页面头部样式 */
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { margin: 0; font-size: 24px; color: #303133; }
.report-btn { box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3); font-weight: bold; letter-spacing: 1px; }

/* 顶部指标样式 */
.metric-row { margin-bottom: 24px; }
.metric-card { border-radius: 12px; border: none; text-align: center; padding: 10px 0; }
.diet-metric { background: linear-gradient(145deg, #f8fdf6 0%, #ffffff 100%); }
.metric-title { font-size: 14px; color: #909399; margin-bottom: 12px; display: flex; align-items: center; justify-content: center; gap: 6px; }
.metric-value { font-size: 32px; font-weight: 900; color: #303133; font-style: italic; }
.unit { font-size: 14px; font-style: normal; color: #c0c4cc; }

/* 核心布局保障 */
.main-row { margin-bottom: 24px; }
.split-card { border-radius: 16px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.03); display: flex; flex-direction: column; height: 100%; }
.split-card :deep(.el-card__body) { flex: 1; padding: 24px; }

/* 蓝绿分明的顶部装饰线 */
.workout-card { border-top: 4px solid #409EFF; }
.diet-card { border-top: 4px solid #67C23A; }

.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 18px; font-weight: bold; color: #303133; display: flex; align-items: center; gap: 8px; }

/* 左侧图表样式 */
.chart-placeholder { height: 160px; background-color: #f8f9fb; border-radius: 12px; display: flex; flex-direction: column; justify-content: flex-end; padding: 20px 20px 10px; margin-top: 10px; }
.bar-chart { display: flex; align-items: flex-end; justify-content: space-around; height: 100%; width: 80%; margin: 0 auto; border-bottom: 2px solid #ebeef5; }
.bar { width: 30px; background-color: #c6e2ff; border-radius: 4px 4px 0 0; transition: all 0.3s; }
.bar:hover { background-color: #409EFF; }
.chart-desc { text-align: center; font-size: 12px; color: #909399; margin-top: 10px; }

/* 右侧营养进度条样式 */
.macro-container { display: flex; flex-direction: column; gap: 20px; padding-top: 10px; }
.macro-item { display: flex; flex-direction: column; gap: 6px; }
.macro-label { font-size: 14px; font-weight: bold; color: #606266; }
.macro-text { font-size: 12px; color: #909399; text-align: right; }

/* 日志区域样式 */
.log-card { border-radius: 16px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.log-content { background-color: #f8f9fb; padding: 12px 16px; border-radius: 8px; border: 1px solid #f0f2f5; margin-top: 4px; }
.log-content strong { font-size: 15px; color: #303133; }
.log-detail { font-size: 13px; color: #909399; margin-top: 8px; }

/* ================= 悬浮打卡按钮 ================= */
.floating-record-btn {
  position: fixed;
  bottom: 60px;
  right: 60px;
  width: 64px;
  height: 64px;
  font-size: 28px;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.4);
  z-index: 999;
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.floating-record-btn:hover {
  transform: scale(1.1);
}

/* 形体照上传组件样式 */
.progress-uploader { width: 100%; }
.upload-trigger {
  width: 100%;
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #fafafa;
  cursor: pointer;
  transition: all 0.3s;
}
.upload-trigger:hover { border-color: #E6A23C; background-color: #fdf6ec; }
.upload-icon { font-size: 24px; color: #909399; margin-bottom: 8px; }
.upload-text { font-size: 12px; color: #909399; text-align: center; line-height: 1.4; }
.upload-trigger:hover .upload-icon, .upload-trigger:hover .upload-text { color: #E6A23C; }

</style>