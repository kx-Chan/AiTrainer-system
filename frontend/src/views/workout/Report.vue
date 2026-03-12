<template>
  <div class="report-container">
    <div class="report-header">
      <el-button link class="back-btn" @click="$router.push('/workout')">
        <el-icon size="20"><Back /></el-icon> 返回大厅
      </el-button>
      <div class="report-title">
        <el-icon color="#E6A23C"><Trophy /></el-icon> AiTrainer 智能分析战报
      </div>
      <el-button type="primary" plain round size="small" @click="shareToCommunity">
        <el-icon><Share /></el-icon> 一键分享到社区
      </el-button>
    </div>

    <div class="report-content">
      <div class="left-panel">
        <el-card shadow="never" class="score-card dark-tech-card">
          <div class="score-header">本次综合表现</div>
          <div class="score-main">
            <div class="score-grade" :class="reportData.gradeLevel">{{ reportData.grade }}</div>
            <div class="score-number">{{ reportData.score }} <span class="score-unit">分</span></div>
          </div>
          <p class="score-comment">"{{ reportData.comment }}"</p>
        </el-card>

        <el-card shadow="never" class="radar-card">
          <template #header>
            <div class="card-title"><el-icon><DataAnalysis /></el-icon> 姿态多维解析</div>
          </template>
          <div ref="radarChartRef" class="radar-echarts"></div>
        </el-card>
      </div>

      <div class="right-panel">
        <el-row :gutter="16" class="data-grid">
          <el-col :span="12">
            <el-card shadow="hover" class="data-item">
              <div class="data-label">有效动作</div>
              <div class="data-value valid-text">{{ reportData.validReps }} <span class="unit">次</span></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="data-item">
              <div class="data-label">异常动作</div>
              <div class="data-value invalid-text">{{ reportData.invalidReps }} <span class="unit">次</span></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="data-item">
              <div class="data-label">训练时长</div>
              <div class="data-value">{{ reportData.duration }} <span class="unit">min</span></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="data-item">
              <div class="data-label">消耗预估</div>
              <div class="data-value hot-text">{{ reportData.calories }} <span class="unit">kcal</span></div>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" class="snapshot-card">
          <template #header>
            <div class="card-title">
              <span class="warning-title"><el-icon><Warning /></el-icon> AI 纠错抓拍</span>
            </div>
          </template>
          <div v-if="reportData.snapshots.length > 0" class="snapshot-list">
            <div class="snapshot-item" v-for="(img, index) in reportData.snapshots" :key="index">
              <div class="snapshot-img-wrapper">
                <el-image :src="img.url" fit="cover" class="snapshot-img" />
                <div class="error-spot" :style="{ top: img.errorY, left: img.errorX }"></div>
              </div>
              <div class="snapshot-desc">{{ img.reason }}</div>
            </div>
          </div>
          <el-empty v-else description="太棒了！本次训练没有检测到任何错误动作！" />
        </el-card>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Back, Trophy, Share, DataAnalysis, Warning } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const radarChartRef = ref(null)

// 模拟从后端获取的战报数据 (根据 URL 里的 :id 动态拉取)
const reportData = reactive({
  id: route.params.id,
  type: '深蹲',
  score: 88,
  grade: 'A',
  gradeLevel: 'grade-A',
  comment: '核心极其稳定，但下蹲深度有待加强。',
  validReps: 45,
  invalidReps: 3,
  duration: 15,
  calories: 120,
  // 维度评分：膝盖轨迹, 下蹲深度, 背部姿态, 核心稳定, 发力节奏
  radarScores: [95, 75, 88, 96, 85],
  snapshots: [
    { 
      url: 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?auto=format&fit=crop&q=80&w=400', 
      reason: '第 12 次: 膝盖轻微内扣', 
      errorX: '45%', errorY: '70%' 
    },
    { 
      url: 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&q=80&w=400', 
      reason: '第 28 次: 背部未完全挺直', 
      errorX: '60%', errorY: '40%' 
    }
  ]
})

// 初始化 ECharts 雷达图
const initRadarChart = () => {
  if (!radarChartRef.value) return
  const myChart = echarts.init(radarChartRef.value)
  
  const option = {
    radar: {
      indicator: [
        { name: '膝盖轨迹', max: 100 },
        { name: '下蹲深度', max: 100 },
        { name: '背部姿态', max: 100 },
        { name: '核心稳定', max: 100 },
        { name: '发力节奏', max: 100 }
      ],
      axisName: { color: '#606266', fontWeight: 'bold' },
      splitArea: { areaStyle: { color: ['#f8f9fa', '#f1f3f5', '#e9ecef', '#dee2e6'] } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: reportData.radarScores,
        name: '本次评分',
        itemStyle: { color: '#409EFF' },
        areaStyle: { color: 'rgba(64,158,255,0.3)' }
      }]
    }]
  }
  myChart.setOption(option)
}

const shareToCommunity = () => {
  ElMessage.success('战报已生成社交卡片，即将跳转到社区发布页！')
  setTimeout(() => {
    router.push('/community')
  }, 1000)
}

onMounted(() => {
  nextTick(() => {
    initRadarChart()
  })
})
</script>

<style scoped>
.report-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 0 40px 40px;
}

/* 顶部独立导航 */
.report-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.back-btn {
  font-size: 16px;
  color: #606266;
}
.back-btn:hover { color: #409EFF; }

.report-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 核心内容网格布局 */
.report-content {
  display: flex;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.left-panel {
  flex: 4;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.right-panel {
  flex: 5;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ================= 左侧样式 ================= */
.dark-tech-card {
  background: linear-gradient(135deg, #1f2d3d 0%, #304156 100%);
  color: #fff;
  border: none;
  text-align: center;
  padding: 20px 0;
  border-radius: 16px;
}

.score-header {
  font-size: 14px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.score-main {
  display: flex;
  justify-content: center;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 16px;
}

.score-grade {
  font-size: 60px;
  font-weight: 900;
  font-style: italic;
  line-height: 1;
  text-shadow: 0 4px 12px rgba(0,0,0,0.2);
}
.grade-S { color: #F56C6C; }
.grade-A { color: #E6A23C; }
.grade-B { color: #409EFF; }

.score-number {
  font-size: 48px;
  font-weight: bold;
}
.score-unit {
  font-size: 16px;
  font-weight: normal;
}

.score-comment {
  font-size: 15px;
  color: #e4e7ed;
  font-style: italic;
}

.radar-card { border-radius: 16px; }
.card-title {
  font-weight: bold;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.radar-echarts {
  width: 100%;
  height: 350px;
}

/* ================= 右侧样式 ================= */
.data-item {
  border-radius: 12px;
  text-align: center;
  padding: 10px 0;
  margin-bottom: 16px;
}
.data-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.data-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.unit { font-size: 14px; color: #606266; font-weight: normal; }

.valid-text { color: #67C23A; }
.invalid-text { color: #F56C6C; }
.hot-text { color: #E6A23C; }

.snapshot-card { border-radius: 16px; flex: 1; }
.warning-title { color: #F56C6C; }

.snapshot-list {
  display: flex;
  gap: 16px;
}
.snapshot-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.snapshot-img-wrapper {
  position: relative;
  width: 100%;
  height: 160px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}
.snapshot-img {
  width: 100%;
  height: 100%;
}
/* 模拟错误点标红 */
.error-spot {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 2px solid #F56C6C;
  border-radius: 50%;
  background: rgba(245, 108, 108, 0.3);
  transform: translate(-50%, -50%);
  animation: pulse-red 1.5s infinite;
}
.snapshot-desc {
  font-size: 13px;
  color: #606266;
  text-align: center;
}

@keyframes pulse-red {
  0% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(245, 108, 108, 0); }
  100% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0); }
}
</style>