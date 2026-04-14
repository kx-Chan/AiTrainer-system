<template>
  <div class="report-container">
    <div class="report-header">
      <el-button link class="back-btn" @click="$router.push('/workout')">
        <el-icon size="20">
          <Back />
        </el-icon> 返回大厅
      </el-button>
      <div class="report-title">
        <el-icon color="#E6A23C">
          <Trophy />
        </el-icon> AiTrainer 智能分析战报
      </div>
      <div class="header-actions">
        <el-button type="primary" plain round size="small" @click="shareToCommunity">
          <el-icon>
            <Share />
          </el-icon> 一键分享到社区
        </el-button>
        <el-button round size="small" @click="$router.push('/workout')">结束训练</el-button>
      </div>
    </div>

    <div class="report-content" v-loading="isLoading">
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
            <div class="card-title"><el-icon>
                <DataAnalysis />
              </el-icon> 姿态多维解析</div>
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
              <span class="warning-title"><el-icon>
                  <Warning />
                </el-icon> AI 纠错抓拍</span>
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
import { ref, reactive, onBeforeUnmount, onMounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Back, Trophy, Share, DataAnalysis, Warning } from '@element-plus/icons-vue'
import { workoutApi } from '@/api/workout'

const router = useRouter()
const route = useRoute()
const radarChartRef = ref(null)
const isLoading = ref(false)
const chartInstance = ref(null)

const reportData = reactive({
  id: route.params.id,
  workoutId: '',
  type: '',
  score: 0,
  grade: '',
  gradeLevel: '',
  comment: '',
  validReps: 0,
  invalidReps: 0,
  duration: 0,
  calories: 0,
  radarScores: [],
  snapshots: []
})

const normalizeSession = (raw) => {
  const radarScores = Array.isArray(raw?.radarScores)
    ? raw.radarScores
    : typeof raw?.radarScores === 'string'
      ? (() => { try { return JSON.parse(raw.radarScores) } catch (e) { return [] } })()
      : Array.isArray(raw?.radar_scores)
        ? raw.radar_scores
        : typeof raw?.radar_scores === 'string'
          ? (() => { try { return JSON.parse(raw.radar_scores) } catch (e) { return [] } })()
          : []

  const snapshots = Array.isArray(raw?.snapshots)
    ? raw.snapshots
    : typeof raw?.snapshots === 'string'
      ? (() => { try { return JSON.parse(raw.snapshots) } catch (e) { return [] } })()
      : Array.isArray(raw?.snapshotList)
        ? raw.snapshotList
        : []

  const duration = Number(raw?.durationMinutes ?? raw?.duration_minutes ?? raw?.duration ?? 0)
  const calories = Number(raw?.caloriesBurned ?? raw?.calories_burned ?? raw?.calories ?? 0)

  return {
    id: raw?.id ?? reportData.id,
    workoutId: raw?.workoutId ?? raw?.workout_id ?? '',
    type: raw?.workoutName || raw?.type || raw?.name || '',
    score: Number(raw?.score ?? 0),
    grade: raw?.grade || '',
    gradeLevel: raw?.gradeLevel || raw?.grade_level || '',
    comment: raw?.comment || '',
    validReps: Number(raw?.validReps ?? raw?.valid_reps ?? 0),
    invalidReps: Number(raw?.invalidReps ?? raw?.invalid_reps ?? 0),
    duration,
    calories,
    radarScores,
    snapshots
  }
}

const fetchReport = async () => {
  const id = String(route.params.id || '').trim()
  if (!id) return
  isLoading.value = true
  try {
    const data = await workoutApi.getSession(id)
    Object.assign(reportData, normalizeSession(data || {}))
  } finally {
    isLoading.value = false
  }
}

const buildRadarOption = () => ({
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
      value: Array.isArray(reportData.radarScores) ? reportData.radarScores : [],
      name: '本次评分',
      itemStyle: { color: '#409EFF' },
      areaStyle: { color: 'rgba(64,158,255,0.3)' }
    }]
  }]
})

const initRadarChart = () => {
  if (!radarChartRef.value) return
  if (chartInstance.value) {
    try { chartInstance.value.dispose() } catch (e) { }
  }
  chartInstance.value = echarts.init(radarChartRef.value)
  chartInstance.value.setOption(buildRadarOption())
}

const shareToCommunity = () => {
  ElMessage.success('战报已生成社交卡片，即将跳转到社区发布页！')
  setTimeout(() => {
    router.push({ path: '/community', query: { shareReportId: String(reportData.id || '') } })
  }, 1000)
}

onMounted(() => {
  fetchReport().then(() => {
    nextTick(() => {
      initRadarChart()
    })
  })
})

const handleResize = () => {
  if (!chartInstance.value) return
  try { chartInstance.value.resize() } catch (e) { }
}

watch(() => reportData.radarScores, () => {
  if (!chartInstance.value) return
  try { chartInstance.value.setOption(buildRadarOption(), true) } catch (e) { }
}, { deep: true })

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance.value) {
    try { chartInstance.value.dispose() } catch (e) { }
    chartInstance.value = null
  }
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

.back-btn:hover {
  color: #409EFF;
}

.report-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
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
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.grade-S {
  color: #F56C6C;
}

.grade-A {
  color: #E6A23C;
}

.grade-B {
  color: #409EFF;
}

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

.radar-card {
  border-radius: 16px;
}

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

.unit {
  font-size: 14px;
  color: #606266;
  font-weight: normal;
}

.valid-text {
  color: #67C23A;
}

.invalid-text {
  color: #F56C6C;
}

.hot-text {
  color: #E6A23C;
}

.snapshot-card {
  border-radius: 16px;
  flex: 1;
}

.warning-title {
  color: #F56C6C;
}

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
  0% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.7);
  }

  70% {
    box-shadow: 0 0 0 10px rgba(245, 108, 108, 0);
  }

  100% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0);
  }
}
</style>
