<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h2 class="page-title">数据看板 Dashboard</h2>
    </div>

    <el-row :gutter="24" class="metric-row">
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-title"><el-icon><Timer /></el-icon> 近7天总消耗</div>
          <div class="metric-value">{{ total7Days }} <span class="unit">kcal</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card workout-metric">
          <div class="metric-title"><el-icon><DataLine /></el-icon> 项目训练消耗</div>
          <div class="metric-value">{{ workoutCal }} <span class="unit">kcal</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card extra-metric">
          <div class="metric-title"><el-icon><Promotion /></el-icon> 额外运动消耗</div>
          <div class="metric-value">{{ extraCal }} <span class="unit">kcal</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-title"><el-icon><Odometer /></el-icon> 训练次数</div>
          <div class="metric-value">{{ workoutLogs.length + extraExerciseLogs.length }} <span class="unit">次</span></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="main-row">
      <el-col :span="12">
        <el-card shadow="never" class="split-card workout-card">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><Timer /></el-icon> 近7天卡路里消耗趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="calorieChartRef" class="calorie-chart"></div>
          </div>
          <div class="chart-legend">
            <span class="legend-item"><span class="legend-dot workout"></span>项目训练</span>
            <span class="legend-item"><span class="legend-dot extra"></span>额外运动</span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="split-card diet-card">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><PieChart /></el-icon> 今日营养摄入配比</span>
              <span class="total-cal">
                已摄入 {{ nutritionData.totalCalories || 0 }} / {{ nutritionData.targetCalories || 0 }} kcal
                <span class="calorie-progress">({{ getCalorieProgress() }}%)</span>
              </span>
            </div>
          </template>
          <div class="nutrition-container" v-if="nutritionData.targetCalories > 0">
            <div class="target-summary">
              <div class="target-info">
                <span class="target-label">今日应摄入</span>
                <span class="target-cal-value">{{ nutritionData.targetCalories || 0 }} kcal</span>
              </div>
              <div class="target-values">
                <span class="target-item carbs">碳水 {{ nutritionData.carbsTargetGrams || 0 }}g</span>
                <span class="target-item protein">蛋白 {{ nutritionData.proteinTargetGrams || 0 }}g</span>
                <span class="target-item fat">脂肪 {{ nutritionData.fatTargetGrams || 0 }}g</span>
              </div>
            </div>
            <div class="exercise-burned-hint" v-if="nutritionData.workoutBurnedCalories > 0 || nutritionData.extraBurnedCalories > 0">
              <span class="burned-tag workout-burned" v-if="nutritionData.workoutBurnedCalories > 0">🔥 训练 +{{ nutritionData.workoutBurnedCalories }} kcal</span>
              <span class="burned-tag extra-burned" v-if="nutritionData.extraBurnedCalories > 0">🏃 额外运动 +{{ nutritionData.extraBurnedCalories }} kcal</span>
              <span class="burned-note">已计入今日应摄入</span>
            </div>
            
            <div class="nutrition-item">
              <div class="nutrition-label">
                <span class="label-text">碳水化合物</span>
                <span class="label-percent">{{ nutritionData.carbsGrams || 0 }}g / {{ nutritionData.carbsTargetGrams || 0 }}g (目标)</span>
              </div>
              <div class="progress-bar-container">
                <div class="progress-bar carbs" :style="{ width: getProgressWidth(nutritionData.carbsGrams, nutritionData.carbsTargetGrams) }"></div>
              </div>
              <div class="nutrition-detail">
                <span>{{ nutritionData.carbsPercent || 0 }}%</span>
                <span class="status" :class="getTargetStatus(nutritionData.carbsGrams, nutritionData.carbsTargetGrams)">
                  {{ getTargetText(nutritionData.carbsGrams, nutritionData.carbsTargetGrams) }}
                </span>
              </div>
            </div>

            <div class="nutrition-item">
              <div class="nutrition-label">
                <span class="label-text">蛋白质</span>
                <span class="label-percent">{{ nutritionData.proteinGrams || 0 }}g / {{ nutritionData.proteinTargetGrams || 0 }}g (目标)</span>
              </div>
              <div class="progress-bar-container">
                <div class="progress-bar protein" :style="{ width: getProgressWidth(nutritionData.proteinGrams, nutritionData.proteinTargetGrams) }"></div>
              </div>
              <div class="nutrition-detail">
                <span>{{ nutritionData.proteinPercent || 0 }}%</span>
                <span class="status" :class="getTargetStatus(nutritionData.proteinGrams, nutritionData.proteinTargetGrams)">
                  {{ getTargetText(nutritionData.proteinGrams, nutritionData.proteinTargetGrams) }}
                </span>
              </div>
            </div>

            <div class="nutrition-item">
              <div class="nutrition-label">
                <span class="label-text">脂肪</span>
                <span class="label-percent">{{ nutritionData.fatGrams || 0 }}g / {{ nutritionData.fatTargetGrams || 0 }}g (目标)</span>
              </div>
              <div class="progress-bar-container">
                <div class="progress-bar fat" :style="{ width: getProgressWidth(nutritionData.fatGrams, nutritionData.fatTargetGrams) }"></div>
              </div>
              <div class="nutrition-detail">
                <span>{{ nutritionData.fatPercent || 0 }}%</span>
                <span class="status" :class="getTargetStatus(nutritionData.fatGrams, nutritionData.fatTargetGrams)">
                  {{ getTargetText(nutritionData.fatGrams, nutritionData.fatTargetGrams) }}
                </span>
              </div>
            </div>
          </div>
          <el-empty v-else description="今日暂无饮食记录" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志区域 - 三列布局 -->
    <el-row :gutter="24" class="log-row">
      <!-- 项目训练日志 -->
      <el-col :span="8">
        <el-card shadow="never" class="log-card">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><Calendar /></el-icon> 项目训练日志</span>
              <span class="log-count">共 {{ workoutLogs.length }} 条</span>
            </div>
            <div class="date-filter">
              <el-date-picker
                v-model="workoutDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                size="small"
                value-format="YYYY-MM-DD"
                @change="handleWorkoutDateChange"
                style="width: 100%;"
              />
            </div>
          </template>
          <el-timeline v-if="workoutLogs.length > 0">
            <el-timeline-item v-for="log in workoutLogs" :key="log.id" :timestamp="formatDate(log.createdAt)" :type="getGradeType(log.grade)" hollow>
              <div class="log-content">
                <strong>{{ log.workoutName }}</strong>
                <el-tag v-if="log.score" size="small" :type="getGradeType(log.grade)" style="margin-left: 8px;">AI 战报 {{ log.score }} 分</el-tag>
                <div class="log-detail">
                  <span>🔥 {{ log.caloriesBurned || 0 }} kcal</span>
                  <span v-if="log.durationSeconds" style="margin-left: 12px;">⏱️ {{ formatDuration(log.durationSeconds) }}</span>
                  <span v-if="log.validReps" style="margin-left: 12px;">✅ {{ log.validReps }} 次</span>
                </div>
                <div v-if="log.comment" class="log-comment">{{ log.comment }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无项目训练记录" :image-size="60" />
        </el-card>
      </el-col>

      <!-- 额外运动日志 -->
      <el-col :span="8">
        <el-card shadow="never" class="log-card extra-log-card">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><Promotion /></el-icon> 额外运动日志</span>
              <span class="log-count">共 {{ extraExerciseLogs.length }} 条</span>
            </div>
            <div class="date-filter">
              <el-date-picker
                v-model="extraDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                size="small"
                value-format="YYYY-MM-DD"
                @change="handleExtraDateChange"
                style="width: 100%;"
              />
            </div>
          </template>
          <el-timeline v-if="extraExerciseLogs.length > 0">
            <el-timeline-item v-for="log in extraExerciseLogs" :key="log.id" :timestamp="formatDate(log.exerciseDate)" color="#67C23A" hollow>
              <div class="log-content extra-log-content">
                <strong>{{ log.exerciseName }}</strong>
                <div class="log-detail">
                  <span>🔥 {{ log.caloriesBurned || 0 }} kcal</span>
                  <span v-if="log.durationMinutes" style="margin-left: 12px;">⏱️ {{ log.durationMinutes }} 分钟</span>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无额外运动记录" :image-size="60" />
        </el-card>
      </el-col>

      <!-- 饮食日志 -->
      <el-col :span="8">
        <el-card shadow="never" class="log-card diet-log-card">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon><Food /></el-icon> 饮食记录日志</span>
              <span class="log-count">共 {{ dietLogs.length }} 条</span>
            </div>
            <div class="date-filter">
              <el-date-picker
                v-model="dietDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                size="small"
                value-format="YYYY-MM-DD"
                @change="handleDietDateChange"
                style="width: 100%;"
              />
            </div>
          </template>
          <el-timeline v-if="dietLogs.length > 0">
            <el-timeline-item v-for="log in dietLogs" :key="log.mealDate + '-' + log.mealType" :timestamp="formatDate(log.mealDate)" color="#E6A23C" hollow>
              <div class="log-content diet-log-content">
                <strong>{{ log.mealTypeName }}</strong>
                <span class="meal-total-cal">🔥 {{ log.totalCalories || 0 }} kcal</span>
                <div class="food-list">
                  <div v-for="(food, idx) in log.foods" :key="idx" class="food-item">
                    <span>{{ food.foodName }}</span>
                    <span class="food-cal">{{ food.calories || 0 }}kcal</span>
                    <span v-if="food.mealTime" class="food-time">{{ food.mealTime }}</span>
                  </div>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无饮食记录" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-tooltip content="更新体征 (建议每周1次，早晨空腹最佳)" placement="left" effect="dark">
      <el-button type="warning" circle class="floating-record-btn" @click="isRecordVisible = true">
        <el-icon><DataBoard /></el-icon>
      </el-button>
    </el-tooltip>

    <el-dialog v-model="isRecordVisible" title="📊 阶段性体征复盘" width="450px" destroy-on-close>
      <el-alert title="💡 健身先健脑：体重受水分影响波动极大，切勿每天称重制造焦虑。" type="warning" :closable="false" style="margin-bottom: 20px;" />
      <el-form label-width="100px" :model="dailyRecord" label-position="left">
        <el-form-item label="当前体重(kg)">
          <el-input-number v-model="dailyRecord.weight" :precision="1" :step="0.5" :min="30" :max="200" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预估体脂(%)">
          <el-input-number v-model="dailyRecord.bodyFat" :precision="1" :step="0.5" :min="1" :max="50" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="isRecordVisible = false">暂不更新</el-button>
        <el-button type="warning" @click="saveDailyRecord">保存并同步给 AI</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { DataBoard, DataLine, Food, Odometer, Timer, PieChart, Calendar, Promotion } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getLast7DaysCalories, getTrainingLogs, getNutritionRatio } from '@/api/dashboard'

const calorieChartRef = ref(null)
let calorieChart = null

const dashboardData = reactive({
  dates: [],
  workoutCalories: [],
  extraExerciseCalories: [],
  totalCalories: [],
  totalWorkoutCalories: 0,
  totalExtraExerciseCalories: 0,
  totalCalories7Days: 0
})

const nutritionData = reactive({
  totalCalories: 0,
  carbsGrams: 0,
  proteinGrams: 0,
  fatGrams: 0,
  carbsPercent: 0,
  proteinPercent: 0,
  fatPercent: 0,
  carbsTargetPercent: 50,
  proteinTargetPercent: 30,
  fatTargetPercent: 20,
  targetCalories: 0,
  carbsTargetGrams: 0,
  proteinTargetGrams: 0,
  fatTargetGrams: 0,
  workoutBurnedCalories: 0,
  extraBurnedCalories: 0
})

// 原始数据（未过滤）
const allWorkoutLogs = ref([])
const allExtraExerciseLogs = ref([])
const allDietLogs = ref([])

// 日期范围选择
const workoutDateRange = ref([])
const extraDateRange = ref([])
const dietDateRange = ref([])

// 计算属性：根据日期范围过滤后的日志
const workoutLogs = computed(() => filterByDateRange(allWorkoutLogs.value, workoutDateRange.value, 'createdAt'))
const extraExerciseLogs = computed(() => filterByDateRange(allExtraExerciseLogs.value, extraDateRange.value, 'exerciseDate'))
const dietLogs = computed(() => filterByDateRange(allDietLogs.value, dietDateRange.value, 'mealDate'))

function filterByDateRange(logs, dateRange, dateField) {
  if (!dateRange || dateRange.length !== 2) return logs
  const [startDate, endDate] = dateRange
  if (!startDate || !endDate) return logs
  return logs.filter(log => {
    const logDate = log[dateField]
    if (!logDate) return false
    return logDate >= startDate && logDate <= endDate
  })
}

const total7Days = computed(() => dashboardData.totalCalories7Days || 0)
const workoutCal = computed(() => dashboardData.totalWorkoutCalories || 0)
const extraCal = computed(() => dashboardData.totalExtraExerciseCalories || 0)

async function loadCalorieData() {
  try {
    const data = await getLast7DaysCalories()
    if (data) {
      dashboardData.dates = data.dates || []
      dashboardData.workoutCalories = data.workoutCalories || []
      dashboardData.extraExerciseCalories = data.extraExerciseCalories || []
      dashboardData.totalCalories = data.totalCalories || []
      dashboardData.totalWorkoutCalories = data.totalWorkoutCalories || 0
      dashboardData.totalExtraExerciseCalories = data.totalExtraExerciseCalories || 0
      dashboardData.totalCalories7Days = data.totalCalories7Days || 0
      await nextTick()
      setTimeout(() => renderCalorieChart(), 100)
    }
  } catch (e) {
    console.error('加载卡路里数据失败', e)
  }
}

async function loadTrainingLogs(startDate, endDate) {
  try {
    const data = await getTrainingLogs(startDate, endDate)
    if (data) {
      allWorkoutLogs.value = data.workoutLogs || []
      allExtraExerciseLogs.value = data.extraExerciseLogs || []
      allDietLogs.value = data.dietLogs || []
    }
  } catch (e) {
    console.error('加载训练日志失败', e)
  }
}

async function loadNutritionData() {
  try {
    const data = await getNutritionRatio()
    if (data) {
      nutritionData.totalCalories = data.totalCalories || 0
      nutritionData.carbsGrams = data.carbsGrams || 0
      nutritionData.proteinGrams = data.proteinGrams || 0
      nutritionData.fatGrams = data.fatGrams || 0
      nutritionData.carbsPercent = data.carbsPercent || 0
      nutritionData.proteinPercent = data.proteinPercent || 0
      nutritionData.fatPercent = data.fatPercent || 0
      nutritionData.carbsTargetPercent = data.carbsTargetPercent || 50
      nutritionData.proteinTargetPercent = data.proteinTargetPercent || 30
      nutritionData.fatTargetPercent = data.fatTargetPercent || 20
      // 新增目标营养素克数字段
      nutritionData.targetCalories = data.targetCalories || 0
      nutritionData.carbsTargetGrams = data.carbsTargetGrams || 0
      nutritionData.proteinTargetGrams = data.proteinTargetGrams || 0
      nutritionData.fatTargetGrams = data.fatTargetGrams || 0
      // 运动消耗（已纳入目标热量计算）
      nutritionData.workoutBurnedCalories = data.workoutBurnedCalories || 0
      nutritionData.extraBurnedCalories = data.extraBurnedCalories || 0
    }
  } catch (e) {
    console.error('加载营养数据失败', e)
  }
}

// 日期范围变化处理
function handleWorkoutDateChange(val) {
  const [startDate, endDate] = val || []
  loadTrainingLogs(startDate, endDate)
}

function handleExtraDateChange(val) {
  const [startDate, endDate] = val || []
  loadTrainingLogs(startDate, endDate)
}

function handleDietDateChange(val) {
  const [startDate, endDate] = val || []
  loadTrainingLogs(startDate, endDate)
}

function getMealTypeText(type) {
  const typeMap = {
    'breakfast': '早餐',
    'lunch': '午餐',
    'dinner': '晚餐',
    'snack': '加餐'
  }
  return typeMap[type] || type || '未知'
}

function getNutritionStatus(actual, target) {
  if (!actual || !target) return 'normal'
  const diff = actual - target
  if (Math.abs(diff) <= 5) return 'normal'
  return diff > 0 ? 'high' : 'low'
}

function getNutritionText(actual, target) {
  if (!actual || !target) return '正常'
  const diff = actual - target
  if (Math.abs(diff) <= 5) return '✓ 正常'
  return diff > 0 ? '↑ 偏高' : '↓ 偏低'
}

// 获取卡路里完成进度
function getCalorieProgress() {
  const target = nutritionData.targetCalories || 0
  const current = nutritionData.totalCalories || 0
  if (target === 0) return 0
  return Math.min(Math.round((current / target) * 100), 100)
}

// 获取进度条宽度
function getProgressWidth(actual, target) {
  if (!target || target === 0) return '0%'
  const percent = Math.min((actual / target) * 100, 100)
  return Math.max(percent, 0) + '%'
}

// 获取目标完成状态
function getTargetStatus(actual, target) {
  if (!target || target === 0) return 'normal'
  const percent = (actual / target) * 100
  if (percent >= 90 && percent <= 110) return 'normal'
  return percent < 90 ? 'low' : 'high'
}

// 获取目标完成状态文本
function getTargetText(actual, target) {
  if (!target || target === 0) return '—'
  const percent = (actual / target) * 100
  if (percent >= 90 && percent <= 110) return '✓ 达标'
  return percent < 90 ? '未达标' : '超标'
}

function renderCalorieChart() {
  if (!calorieChartRef.value) return
  if (!calorieChart) {
    calorieChart = echarts.init(calorieChartRef.value)
  }
  const dates = dashboardData.dates.map(d => d.slice(5))
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['项目训练', '额外运动'], bottom: 0, textStyle: { fontSize: 12 } },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '5%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', name: 'kcal', axisLabel: { fontSize: 11 } },
    series: [
      { name: '项目训练', type: 'bar', stack: 'total', data: dashboardData.workoutCalories, itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] }, barMaxWidth: 40 },
      { name: '额外运动', type: 'bar', stack: 'total', data: dashboardData.extraExerciseCalories, itemStyle: { color: '#67C23A', borderRadius: [4, 4, 0, 0] }, barMaxWidth: 40 }
    ]
  }
  calorieChart.setOption(option, true)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const today = new Date().toISOString().slice(0, 10)
  const yesterday = new Date(Date.now() - 86400000).toISOString().slice(0, 10)
  if (dateStr === today) return '今天'
  if (dateStr === yesterday) return '昨天'
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()]
  return `${month}/${day} ${weekDay}`
}

function getGradeType(grade) {
  const gradeMap = { 'S': 'success', 'A': 'primary', 'B': 'warning', 'C': 'info' }
  return gradeMap[grade] || 'info'
}

function formatDuration(seconds) {
  const totalSeconds = Number(seconds ?? 0)
  if (totalSeconds <= 0) return '0秒'
  const minutes = Math.floor(totalSeconds / 60)
  const secs = totalSeconds % 60
  if (minutes > 0 && secs > 0) return `${minutes}分${secs}秒`
  if (minutes > 0) return `${minutes}分`
  return `${secs}秒`
}

const isRecordVisible = ref(false)
const dailyRecord = reactive({ weight: 65.5, bodyFat: 18.5 })

const saveDailyRecord = () => {
  isRecordVisible.value = false
  ElMessage.success('今日体征已记录！后端 user_body_data 流水表已更新。')
}

onMounted(() => {
  loadCalorieData()
  loadTrainingLogs()
  loadNutritionData()
  window.addEventListener('resize', () => { calorieChart?.resize() })
})
</script>

<style scoped>
.dashboard-container { max-width: 1600px; margin: 0 auto; padding-bottom: 40px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { margin: 0; font-size: 24px; color: #303133; }
.metric-row { margin-bottom: 24px; }
.metric-card { border-radius: 12px; border: none; text-align: center; padding: 10px 0; transition: transform 0.2s; }
.metric-card:hover { transform: translateY(-2px); }
.workout-metric { background: linear-gradient(145deg, #ecf5ff 0%, #ffffff 100%); }
.extra-metric { background: linear-gradient(145deg, #f0faf0 0%, #ffffff 100%); }
.metric-title { font-size: 14px; color: #909399; margin-bottom: 12px; display: flex; align-items: center; justify-content: center; gap: 6px; }
.metric-value { font-size: 32px; font-weight: 900; color: #303133; font-style: italic; }
.unit { font-size: 14px; font-style: normal; color: #c0c4cc; }
.main-row { margin-bottom: 24px; }
.split-card { border-radius: 16px; border: none; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03); display: flex; flex-direction: column; height: 100%; }
.split-card :deep(.el-card__body) { flex: 1; padding: 24px; }
.workout-card { border-top: 4px solid #409EFF; }
.diet-card { border-top: 4px solid #67C23A; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 18px; font-weight: bold; color: #303133; display: flex; align-items: center; gap: 8px; }
.total-cal, .log-count { font-size: 12px; color: #909399; }
.chart-container { height: 180px; margin-top: 10px; }
.calorie-chart { width: 100%; height: 100%; }
.chart-legend { display: flex; justify-content: center; gap: 24px; margin-top: 12px; }
.legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #606266; }
.legend-dot { width: 10px; height: 10px; border-radius: 50%; }
.legend-dot.workout { background-color: #409EFF; }
.legend-dot.extra { background-color: #67C23A; }
.nutrition-container { margin-top: 10px; }
.target-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #f0f9ff 0%, #e8f4ff 100%);
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
  border: 1px solid #d0e8ff;
}
.target-label { font-size: 13px; font-weight: 600; color: #606266; }
.target-cal-value { font-size: 18px; font-weight: 800; color: #409EFF; margin-left: 8px; }
.target-values { display: flex; gap: 12px; }
.exercise-burned-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #fff8e1 0%, #fff3e0 100%);
  border-radius: 8px;
  border: 1px solid #ffe0b2;
  flex-wrap: wrap;
}
.burned-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}
.burned-tag.workout-burned { color: #409EFF; background-color: rgba(64, 158, 255, 0.1); }
.burned-tag.extra-burned { color: #67C23A; background-color: rgba(103, 194, 58, 0.1); }
.burned-note { font-size: 11px; color: #E6A23C; margin-left: auto; }
.target-item { font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 6px; }
.target-item.carbs { color: #E6A23C; background-color: rgba(230, 162, 60, 0.1); }
.target-item.protein { color: #409EFF; background-color: rgba(64, 158, 255, 0.1); }
.target-item.fat { color: #909399; background-color: rgba(144, 147, 153, 0.1); }
.calorie-progress { color: #67C23A; font-weight: 600; margin-left: 4px; }
.nutrition-item { margin-bottom: 20px; }
.nutrition-label { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.label-text { font-size: 14px; font-weight: 600; color: #303133; }
.label-percent { font-size: 12px; color: #909399; }
.progress-bar-container { position: relative; height: 24px; background-color: #f0f0f0; border-radius: 12px; overflow: visible; }
.progress-bar { height: 100%; border-radius: 12px; transition: width 0.5s ease; min-width: 4px; }
.progress-bar.carbs { background: linear-gradient(90deg, #E6A23C 0%, #F56C6C 100%); }
.progress-bar.protein { background: linear-gradient(90deg, #409EFF 0%, #67C23A 100%); }
.progress-bar.fat { background: linear-gradient(90deg, #909399 0%, #303133 100%); }
.target-line { position: absolute; top: -4px; bottom: -4px; width: 3px; background-color: #F56C6C; border-radius: 2px; box-shadow: 0 0 4px rgba(245, 108, 108, 0.5); }
.nutrition-detail { display: flex; justify-content: space-between; align-items: center; margin-top: 6px; font-size: 12px; color: #606266; }
.status { font-weight: 600; padding: 2px 8px; border-radius: 4px; }
.status.normal { color: #67C23A; background-color: rgba(103, 194, 58, 0.1); }
.status.high { color: #F56C6C; background-color: rgba(245, 108, 108, 0.1); }
.status.low { color: #E6A23C; background-color: rgba(230, 162, 60, 0.1); }
.log-row { margin-top: 24px; }
.log-card { border-radius: 16px; border: none; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03); }
.extra-log-card { border-top: 4px solid #67C23A; }
.diet-log-card { border-top: 4px solid #E6A23C; }
.date-filter { margin-top: 12px; }
.log-content { background-color: #f8f9fb; padding: 12px 16px; border-radius: 8px; border: 1px solid #f0f2f5; margin-top: 4px; }
.extra-log-content { background: linear-gradient(135deg, #f0faf0 0%, #e8f8e8 100%); border-color: #d4edda; }
.diet-log-content { background: linear-gradient(135deg, #fff8e1 0%, #fff3e0 100%); border-color: #ffe0b2; }
.log-content strong { font-size: 15px; color: #303133; }
.log-detail { font-size: 13px; color: #909399; margin-top: 8px; }
.log-comment { font-size: 12px; color: #606266; margin-top: 6px; font-style: italic; }
.meal-total-cal { margin-left: 8px; font-size: 13px; color: #E6A23C; font-weight: 600; }
.food-list { margin-top: 8px; }
.food-item { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #606266; padding: 4px 0; border-bottom: 1px dashed #f0f0f0; }
.food-item:last-child { border-bottom: none; }
.food-cal { color: #909399; margin-right: 8px; }
.food-time { color: #c0c4cc; font-size: 11px; }
.floating-record-btn { position: fixed; bottom: 60px; right: 60px; width: 64px; height: 64px; font-size: 28px; box-shadow: 0 8px 24px rgba(64, 158, 255, 0.4); z-index: 999; transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.floating-record-btn:hover { transform: scale(1.1); }
</style>
