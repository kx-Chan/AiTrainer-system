<template>
  <div class="lobby-container">
    <div class="hero-banner">
      <div class="hero-content">
        <h1 class="hero-title">AiTrainer <span class="highlight">视觉引擎</span> 已就绪</h1>
        <p class="hero-subtitle">无需穿戴设备，只需打开摄像头，即刻开启毫秒级骨骼点追踪与动作纠错。</p>
        <div class="hero-stats">
          <div class="stat-badge">
            <el-icon>
              <VideoCamera />
            </el-icon> 实时帧率: 30 FPS
          </div>
          <div class="stat-badge">
            <el-icon>
              <Aim />
            </el-icon> 识别延迟: < 50ms </div>
          </div>
        </div>
        <div class="cyber-grid"></div>
      </div>

      <div class="section-header">
        <h2><el-icon>
            <Lightning />
          </el-icon> 选择训练项目</h2>
        <el-radio-group v-model="filterType" size="small">
          <el-radio-button label="all">全部项目</el-radio-button>
          <el-radio-button label="leg">下肢核心</el-radio-button>
          <el-radio-button label="back">背部塑形</el-radio-button>
        </el-radio-group>
      </div>

      <el-row :gutter="24" class="workout-grid" v-loading="isLoadingWorkouts">
        <el-col :span="8" v-for="workout in filteredWorkouts" :key="workout.id">
          <el-card class="workout-card" shadow="hover" :style="{ '--theme-color': workout.color }">

            <div class="card-visual" :class="workout.id"
              :style="workout.coverUrl ? { backgroundImage: `url(${workout.coverUrl})` } : undefined">
              <div class="overlay-gradient"></div>
              <div class="visual-tags">
                <el-tag size="small" effect="dark" :color="workout.color" style="border: none;">
                  {{ workout.enName }}
                </el-tag>
              </div>
              <div class="skeleton-line line-1"></div>
              <div class="skeleton-line line-2"></div>
            </div>

            <div class="card-info">
              <h3 class="workout-name">{{ workout.name }}</h3>
              <p class="workout-desc">{{ workout.desc }}</p>

              <div class="workout-meta">
                <div class="meta-item">
                  <span class="meta-label">难度</span>
                  <el-rate v-model="workout.difficulty" disabled :max="5" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
                </div>
                <div class="meta-item">
                  <span class="meta-label">目标肌群</span>
                  <div class="muscle-tags">
                    <el-tag v-for="tag in workout.tags" :key="tag" size="small" type="info" round>{{ tag }}</el-tag>
                  </div>
                </div>
              </div>

              <el-button class="start-btn" type="primary" round @click="handleStartWorkout(workout)">
                <el-icon class="el-icon--left">
                  <VideoPlay />
                </el-icon> 开启 AI 训练
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoCamera, Aim, Lightning, VideoPlay } from '@element-plus/icons-vue'
import { workoutApi } from '@/api/workout'

const router = useRouter()
const filterType = ref('all')

const workoutList = ref([])
const isLoadingWorkouts = ref(false)

const WORKOUT_COVER_MAP = Object.freeze({
  squat: 'https://images.unsplash.com/photo-1517964603305-11c0f6f66012?auto=format&fit=crop&q=80&w=1200',
  lunge: 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&q=80&w=1200',
  good_morning: 'https://images.unsplash.com/photo-1534367610401-9f5ed68180aa?auto=format&fit=crop&q=80&w=1200'
})

const normalizeWorkout = (raw) => {
  const tags = Array.isArray(raw?.tags)
    ? raw.tags
    : typeof raw?.tags === 'string'
      ? (() => {
        try { return JSON.parse(raw.tags) } catch (e) { return [] }
      })()
      : []
  const id = String(raw?.id || '').trim()
  return {
    id,
    name: raw?.name || '',
    enName: raw?.enName || raw?.en_name || '',
    difficulty: Number(raw?.difficulty ?? 1),
    tags,
    desc: raw?.desc || raw?.description || '',
    color: raw?.color || raw?.themeColor || raw?.theme_color || '#409EFF',
    coverUrl: raw?.coverUrl || raw?.cover_url || WORKOUT_COVER_MAP[id] || ''
  }
}

const filteredWorkouts = computed(() => {
  const list = Array.isArray(workoutList.value) ? workoutList.value : []
  const t = String(filterType.value || 'all')
  if (t === 'all') return list
  if (t === 'leg') {
    return list.filter(w => (w.tags || []).some(x => ['臀腿', '核心', '腘绳肌', '单边控制'].includes(String(x))))
  }
  if (t === 'back') {
    return list.filter(w => (w.tags || []).some(x => String(x).includes('背') || String(x).includes('下背')))
  }
  return list
})

const fetchWorkouts = async () => {
  isLoadingWorkouts.value = true
  try {
    const data = await workoutApi.listWorkouts()
    const records = Array.isArray(data?.records) ? data.records : (Array.isArray(data) ? data : [])
    workoutList.value = records.map(normalizeWorkout).filter(w => w.id)
  } catch (e) {
    workoutList.value = []
  } finally {
    isLoadingWorkouts.value = false
  }
}

const handleStartWorkout = (workout) => {
  ElMessageBox.confirm(
    `即将开启【${workout.name}】模式。请确保您已穿着运动服，且全身处于摄像头画面内。`,
    '初始化 AI 视觉引擎',
    {
      confirmButtonText: '我已准备好，开启摄像头',
      cancelButtonText: '稍后开始',
      type: 'warning',
      center: true
    }
  ).then(() => {
    router.push({ name: 'WorkoutTraining', params: { workoutId: String(workout?.id || '') } })
  }).catch(() => {
    ElMessage.info('训练已取消')
  })
}

onMounted(() => {
  fetchWorkouts()
})
</script>

<style scoped>
.lobby-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* ================= 英雄横幅样式 ================= */
.hero-banner {
  background: linear-gradient(135deg, #1f2d3d 0%, #304156 100%);
  border-radius: 16px;
  padding: 40px 50px;
  margin-bottom: 40px;
  position: relative;
  overflow: hidden;
  color: #fff;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 600px;
}

.hero-title {
  font-size: 32px;
  margin: 0 0 16px 0;
  letter-spacing: 1px;
}

.highlight {
  color: #409EFF;
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
}

.hero-subtitle {
  font-size: 16px;
  color: #c0c4cc;
  line-height: 1.6;
  margin-bottom: 24px;
}

.hero-stats {
  display: flex;
  gap: 16px;
}

.stat-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.1);
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 简单的赛博朋克网格背景装饰 */
.cyber-grid {
  position: absolute;
  top: -50%;
  right: -10%;
  width: 600px;
  height: 600px;
  background-image:
    linear-gradient(rgba(64, 158, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(64, 158, 255, 0.1) 1px, transparent 1px);
  background-size: 30px 30px;
  transform: rotate(15deg);
  z-index: 1;
  opacity: 0.5;
}

/* ================= 训练卡片区样式 ================= */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 20px;
  margin: 0;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.workout-card {
  border-radius: 16px;
  overflow: hidden;
  border: none;
  transition: transform 0.3s, box-shadow 0.3s;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.workout-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 32px rgba(64, 158, 255, 0.1);
}

/* 顶部视觉区 (纯 CSS 模拟科技感背景) */
.card-visual {
  height: 160px;
  background-color: #f5f7fa;
  position: relative;
  overflow: hidden;
  background-size: cover;
  background-position: center;
}

.card-visual.squat {
  background: radial-gradient(circle at right bottom, #e6f1fc, #f5f7fa);
}

.card-visual.lunge {
  background: radial-gradient(circle at right bottom, #f0f9eb, #f5f7fa);
}

.card-visual.good_morning {
  background: radial-gradient(circle at right bottom, #fdf6ec, #f5f7fa);
}

.overlay-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.05), transparent);
}

.visual-tags {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 2;
}

/* 纯 CSS 模拟骨骼追踪连线动画 */
.skeleton-line {
  position: absolute;
  background: #409EFF;
  opacity: 0.4;
  box-shadow: 0 0 8px #409EFF;
}

.line-1 {
  width: 2px;
  height: 60px;
  bottom: 20px;
  right: 60px;
  transform: rotate(15deg);
  animation: pulse 2s infinite alternate;
}

.line-2 {
  width: 80px;
  height: 2px;
  bottom: 80px;
  right: 40px;
  transform: rotate(-10deg);
  animation: pulse 2s infinite alternate 0.5s;
}

@keyframes pulse {
  0% {
    opacity: 0.2;
  }

  100% {
    opacity: 0.8;
  }
}

/* 卡片信息区 */
.card-info {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.workout-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}

.workout-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  margin: 0 0 20px 0;
  height: 42px;
  /* 固定高度防止换行错位 */
  overflow: hidden;
}

.workout-meta {
  margin-bottom: 24px;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.meta-item:last-child {
  margin-bottom: 0;
}

.meta-label {
  width: 65px;
  font-size: 13px;
  color: #606266;
}

.muscle-tags {
  display: flex;
  gap: 8px;
}

.start-btn {
  width: 100%;
  margin-top: auto;
  font-weight: bold;
  letter-spacing: 1px;
}
</style>
