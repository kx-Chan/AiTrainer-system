<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h2><el-icon><DataLine /></el-icon> 我的数据看板</h2>
      <el-button type="primary" plain round size="small">生成本周报告</el-button>
    </div>

    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in summaryStats" :key="stat.title">
        <el-card shadow="hover" class="data-card" :class="stat.theme">
          <div class="card-icon"><el-icon><component :is="stat.icon" /></el-icon></div>
          <div class="card-info">
            <div class="card-title">{{ stat.title }}</div>
            <div class="card-value">
              {{ stat.value }} <span class="card-unit">{{ stat.unit }}</span>
            </div>
            <div class="card-trend" :class="stat.trend > 0 ? 'up' : 'down'">
              <el-icon><component :is="stat.trend > 0 ? 'TopRight' : 'BottomRight'" /></el-icon>
              较上周 {{ Math.abs(stat.trend) }}%
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-section">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span><el-icon><Odometer /></el-icon> 体征变化趋势</span>
              <el-radio-group v-model="bodyChartRange" size="small">
                <el-radio-button label="7">近7天</el-radio-button>
                <el-radio-button label="30">近1月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="bodyChartRef" class="echarts-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span><el-icon><Histogram /></el-icon> AI 训练容量分布</span>
              <el-tag type="info" size="small">动作: 综合</el-tag>
            </div>
          </template>
          <div ref="workoutChartRef" class="echarts-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="log-card">
      <template #header>
        <div class="chart-header">
          <span><el-icon><Calendar /></el-icon> 近期训练与饮食日志</span>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item 
          v-for="log in recentLogs" 
          :key="log.id" 
          :timestamp="log.date" 
          :type="log.type"
          placement="top"
        >
          <el-card shadow="hover" class="timeline-content-card">
            <div class="log-header">
              <h4>{{ log.title }}</h4>
              <span class="log-duration" v-if="log.duration">
                <el-icon><Timer /></el-icon> {{ log.duration }} 分钟
              </span>
            </div>
            <p class="log-desc">{{ log.desc }}</p>
            
            <div class="log-tags" v-if="log.dietTags && log.dietTags.length > 0">
              <span class="diet-label">今日饮食打卡:</span>
              <el-tag 
                v-for="tag in log.dietTags" 
                :key="tag" 
                size="small" 
                type="success" 
                effect="plain"
                round
              >
                🍴 {{ tag }}
              </el-tag>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { DataLine, Odometer, Histogram, Calendar, Timer, TopRight, BottomRight, Trophy, Lightning, TrendCharts, Select } from '@element-plus/icons-vue'

// ================= 1. 顶部指标数据 =================
const summaryStats = reactive([
  { title: '累计 AI 训练', value: 45, unit: '次', trend: 12.5, icon: 'TrendCharts', theme: 'blue' },
  { title: '平均动作得分', value: 92, unit: '分', trend: 3.2, icon: 'Trophy', theme: 'orange' },
  { title: '本周消耗热量', value: 1250, unit: 'kcal', trend: -5.4, icon: 'Lightning', theme: 'red' },
  { title: '连续自律打卡', value: 7, unit: '天', trend: 100, icon: 'Select', theme: 'green' }
])

// ================= 2. ECharts 图表逻辑 =================
const bodyChartRef = ref(null)
const workoutChartRef = ref(null)
let bodyChartInstance = null
let workoutChartInstance = null
const bodyChartRange = ref('7')

// 初始化体征趋势图 (双 Y 轴折线图)
const initBodyChart = () => {
  if (!bodyChartRef.value) return
  bodyChartInstance = echarts.init(bodyChartRef.value)
  
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { data: ['体重 (kg)', '预估体脂率 (%)'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '10%', containLabel: true },
    xAxis: { 
      type: 'category', 
      boundaryGap: false, 
      data: ['03-06', '03-07', '03-08', '03-09', '03-10', '03-11', '03-12'],
      axisLine: { lineStyle: { color: '#E4E7ED' } },
      axisLabel: { color: '#909399' }
    },
    yAxis: [
      { type: 'value', name: '体重', min: 65, max: 75, splitLine: { lineStyle: { type: 'dashed', color: '#E4E7ED' } } },
      { type: 'value', name: '体脂率', min: 12, max: 20, splitLine: { show: false } }
    ],
    series: [
      { 
        name: '体重 (kg)', type: 'line', smooth: true, 
        itemStyle: { color: '#409EFF' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(64,158,255,0.3)' }, { offset: 1, color: 'rgba(64,158,255,0.05)' }]) },
        data: [72.5, 72.3, 72.4, 72.1, 71.8, 71.9, 71.5] 
      },
      { 
        name: '预估体脂率 (%)', type: 'line', yAxisIndex: 1, smooth: true, 
        itemStyle: { color: '#E6A23C' },
        data: [18.5, 18.4, 18.4, 18.2, 18.0, 17.9, 17.8] 
      }
    ]
  }
  bodyChartInstance.setOption(option)
}

// 初始化训练容量图 (柱状图)
const initWorkoutChart = () => {
  if (!workoutChartRef.value) return
  workoutChartInstance = echarts.init(workoutChartRef.value)
  
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '8%', top: '10%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: { lineStyle: { color: '#E4E7ED' } },
      axisLabel: { color: '#909399' }
    },
    yAxis: { type: 'value', name: '总容量 (次)', splitLine: { lineStyle: { type: 'dashed', color: '#E4E7ED' } } },
    series: [
      {
        name: '深蹲容量',
        type: 'bar',
        barWidth: '40%',
        itemStyle: { borderRadius: [4, 4, 0, 0], color: '#67C23A' },
        data: [120, 0, 150, 0, 180, 0, 200] // 模拟练腿日
      }
    ]
  }
  workoutChartInstance.setOption(option)
}

// 监听窗口大小变化，让图表自适应缩放
const handleResize = () => {
  bodyChartInstance?.resize()
  workoutChartInstance?.resize()
}

onMounted(() => {
  // 确保 DOM 渲染完毕后再初始化 ECharts
  nextTick(() => {
    initBodyChart()
    initWorkoutChart()
    window.addEventListener('resize', handleResize)
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  bodyChartInstance?.dispose()
  workoutChartInstance?.dispose()
})

// ================= 3. 底部日志数据 =================
const recentLogs = reactive([
  {
    id: 1,
    date: '今天 18:30',
    title: '下肢核心轰炸 (AI 杠铃深蹲)',
    desc: '今天动作非常标准，AiTrainer 提示背部角度控制得很完美。练完腿都在抖。',
    type: 'success',
    duration: 45,
    dietTags: ['宿舍养生壶水煮菜', '蒸锅紫薯', '水煮蛋']
  },
  {
    id: 2,
    date: '昨天 19:00',
    title: '有氧恢复与拉伸',
    desc: '没有上重量，主要进行了一些早安式体前屈，帮助拉伸腘绳肌，核心全程收紧。',
    type: 'primary',
    duration: 30,
    dietTags: ['燕麦粥', '鸡胸肉']
  },
  {
    id: 3,
    date: '03-10 20:15',
    title: '单边控制训练 (AI 箭步蹲)',
    desc: '左腿的稳定性明显不如右腿，算法精准抓拍到了我左腿膝盖内扣的错误动作，已经纠正。',
    type: 'warning',
    duration: 40,
    dietTags: []
  }
])
</script>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-header h2 {
  font-size: 22px;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 顶部卡片 */
.stat-cards {
  margin-bottom: 24px;
}
.data-card {
  border-radius: 12px;
  border: none;
  position: relative;
  overflow: hidden;
}
.card-icon {
  position: absolute;
  right: -10px;
  top: 10px;
  font-size: 80px;
  opacity: 0.1;
  transform: rotate(-15deg);
}
.data-card.blue .card-icon { color: #409EFF; }
.data-card.orange .card-icon { color: #E6A23C; }
.data-card.red .card-icon { color: #F56C6C; }
.data-card.green .card-icon { color: #67C23A; }

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}
.card-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
}
.card-unit {
  font-size: 14px;
  font-weight: normal;
  color: #606266;
}
.card-trend {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.card-trend.up { color: #F56C6C; }
.card-trend.down { color: #67C23A; }

/* 图表区 */
.chart-section {
  margin-bottom: 24px;
}
.chart-card {
  border-radius: 12px;
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
.echarts-container {
  width: 100%;
  height: 320px;
}

/* 日志区 */
.log-card {
  border-radius: 12px;
}
.timeline-content-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.log-header h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}
.log-duration {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}
.log-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 12px 0;
}
.log-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #f8fbf5;
  padding: 8px 12px;
  border-radius: 6px;
  border-left: 3px solid #67C23A;
}
.diet-label {
  font-size: 12px;
  color: #909399;
}
</style>