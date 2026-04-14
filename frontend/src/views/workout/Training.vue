<template>
  <div class="training-container">
    <div class="training-header">
      <el-button link class="back-btn" @click="endTraining">
        <el-icon size="20">
          <Back />
        </el-icon> 结束训练
      </el-button>

      <div class="training-title">
        <el-icon color="#409EFF">
          <VideoCamera />
        </el-icon>
        <span>{{ workoutName || 'AI 训练中' }}</span>
      </div>

      <div class="header-right">
        <el-tag v-if="statusTagText" :type="statusTagType" effect="plain">{{ statusTagText }}</el-tag>
      </div>
    </div>

    <div class="training-content">
      <el-row :gutter="16">
        <el-col :span="18">
          <el-card shadow="never" class="video-card">
            <div class="video-wrapper" v-loading="isStartingCamera">
              <video ref="videoRef" class="camera-video" autoplay playsinline muted></video>
              <div class="hud">
                <div class="hud-item">
                  <span class="hud-label">模型</span>
                  <span class="hud-value">BlazePose</span>
                </div>
                <div class="hud-item">
                  <span class="hud-label">追踪</span>
                  <span class="hud-value">{{ modelReady ? '就绪' : '加载中' }}</span>
                </div>
                <div class="hud-item">
                  <span class="hud-label">后端战报</span>
                  <span class="hud-value">{{ reportStatusText }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="never" class="status-card">
            <template #header>
              <div class="card-title">
                <el-icon>
                  <Cpu />
                </el-icon>
                <span>引擎状态</span>
              </div>
            </template>

            <div class="status-list">
              <div class="status-row">
                <div class="status-left">摄像头</div>
                <div class="status-right">
                  <el-tag :type="cameraReady ? 'success' : 'info'" effect="light" size="small">{{ cameraReady ? '已连接' :
                    '等待授权' }}</el-tag>
                </div>
              </div>
              <div class="status-row">
                <div class="status-left">BlazePose</div>
                <div class="status-right">
                  <el-tag :type="modelReady ? 'success' : 'warning'" effect="light" size="small">{{ modelReady ? '已加载' :
                    '加载中' }}</el-tag>
                </div>
              </div>
              <div class="status-row">
                <div class="status-left">AI 战报</div>
                <div class="status-right">
                  <el-tag :type="reportTagType" effect="light" size="small">{{ reportStatusText }}</el-tag>
                </div>
              </div>
            </div>

            <el-divider />

            <div class="actions">
              <el-button type="primary" round :loading="isEndingTraining" @click="endTraining">结束训练</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-dialog v-model="endPageVisible" title="训练结束" width="720px" :close-on-click-modal="false" :show-close="false">
      <div class="end-summary">
        <div class="end-metrics">
          <div class="metric-item">
            <div class="metric-label">本次得分</div>
            <div class="metric-value">{{ endScoreText }}</div>
          </div>
          <div class="metric-item">
            <div class="metric-label">有效次数</div>
            <div class="metric-value">{{ endValidRepsText }}</div>
          </div>
          <div class="metric-item">
            <div class="metric-label">异常次数</div>
            <div class="metric-value">{{ endInvalidRepsText }}</div>
          </div>
          <div class="metric-item">
            <div class="metric-label">训练时长</div>
            <div class="metric-value">{{ endDurationText }}</div>
          </div>
          <div class="metric-item">
            <div class="metric-label">热量消耗</div>
            <div class="metric-value">{{ endCaloriesText }}</div>
          </div>
        </div>

        <el-alert v-if="endTips.length" type="success" show-icon :closable="false" title="下一次训练小建议">
          <div class="end-tips">
            <div v-for="(t, idx) in endTips" :key="idx">{{ t }}</div>
          </div>
        </el-alert>

        <div class="end-actions">
          <el-button type="primary" round :disabled="!reportId" @click="goToReport">查看完整战报</el-button>
          <el-button round :disabled="!reportId" @click="shareToCommunity">分享到社区</el-button>
          <el-button round @click="backToLobby">返回大厅</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, VideoCamera, Cpu } from '@element-plus/icons-vue'
import { workoutApi } from '@/api/workout'

const router = useRouter()
const route = useRoute()

const workoutId = computed(() => String(route.params.workoutId || '').trim())
const workoutName = ref('')

const videoRef = ref(null)
const streamRef = ref(null)

const isStartingCamera = ref(false)
const isEndingTraining = ref(false)
const cameraReady = ref(false)
const modelReady = ref(false)
const reportId = ref('')
const isGeneratingReport = ref(false)
const reportReady = computed(() => Boolean(reportId.value))

const ended = ref(false)
const endPageVisible = ref(false)
const endSummary = ref(null)

const reportStatusText = computed(() => {
  if (isGeneratingReport.value) return '生成中'
  if (reportReady.value) return '已生成'
  return '未生成'
})

const reportTagType = computed(() => {
  if (isGeneratingReport.value) return 'warning'
  if (reportReady.value) return 'success'
  return 'info'
})

const endScoreText = computed(() => {
  const score = Number(endSummary.value?.score ?? 0)
  return `${score} 分`
})

const endValidRepsText = computed(() => String(Number(endSummary.value?.validReps ?? endSummary.value?.valid_reps ?? 0)))
const endInvalidRepsText = computed(() => String(Number(endSummary.value?.invalidReps ?? endSummary.value?.invalid_reps ?? 0)))

const endDurationText = computed(() => {
  const seconds = Number(endSummary.value?.durationSeconds ?? endSummary.value?.duration_seconds ?? 0)
  if (seconds > 0) return `${seconds} 秒`
  const minutes = Number(endSummary.value?.durationMinutes ?? endSummary.value?.duration_minutes ?? 0)
  if (minutes > 0) return `${minutes} min`
  return '0'
})

const endCaloriesText = computed(() => {
  const kcal = Number(endSummary.value?.caloriesBurned ?? endSummary.value?.calories_burned ?? endSummary.value?.calories ?? 0)
  return `${kcal} kcal`
})

const endTips = computed(() => {
  const score = Number(endSummary.value?.score ?? 0)
  const invalid = Number(endSummary.value?.invalidReps ?? endSummary.value?.invalid_reps ?? 0)
  const tips = []
  if (invalid > 0) tips.push('动作出现异常次数，建议放慢节奏，优先保证动作标准。')
  if (score < 60) tips.push('本次评分偏低，先从减少次数开始，逐步提升动作稳定性。')
  if (score >= 60 && score < 85) tips.push('动作整体不错，下一次可以尝试更稳定的呼吸节奏与核心收紧。')
  if (score >= 85) tips.push('表现优秀，保持节奏与动作幅度，下次尝试提高连续性。')
  tips.push('训练后记得补水与拉伸，避免第二天肌肉紧张。')
  return tips.slice(0, 3)
})

const statusTagText = computed(() => {
  if (ended.value) return '已结束'
  if (reportReady.value) return '战报已生成'
  if (modelReady.value) return '追踪中'
  if (cameraReady.value) return '初始化中'
  return ''
})

const statusTagType = computed(() => {
  if (ended.value) return 'info'
  if (reportReady.value) return 'success'
  if (modelReady.value) return 'warning'
  return 'info'
})

const stopStream = () => {
  const s = streamRef.value
  if (!s) return
  try {
    s.getTracks().forEach(t => t.stop())
  } catch (e) { }
  streamRef.value = null
  cameraReady.value = false
}

const startCamera = async () => {
  if (!videoRef.value) return
  isStartingCamera.value = true
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false })
    streamRef.value = stream
    videoRef.value.srcObject = stream
    cameraReady.value = true
  } finally {
    isStartingCamera.value = false
  }
}

const loadModel = async () => {
  modelReady.value = false
  await new Promise(resolve => setTimeout(resolve, 800))
  modelReady.value = true
}

const generateReport = async () => {
  const id = workoutId.value
  if (!id) return
  isGeneratingReport.value = true
  try {
    const data = await workoutApi.startSession({ workoutId: id })
    const raw = (typeof data === 'object' && data !== null) ? (data?.id ?? data?.sessionId ?? '') : data
    const sid = String(raw || '').trim()
    if (!sid) return
    reportId.value = sid
  } finally {
    isGeneratingReport.value = false
  }
}

const goToReport = () => {
  if (!reportId.value) return
  router.push({ name: 'WorkoutReport', params: { id: String(reportId.value) } })
}

const shareToCommunity = () => {
  if (!reportId.value) return
  router.push({ path: '/community', query: { shareReportId: String(reportId.value) } })
}

const backToLobby = () => {
  router.push('/workout')
}

const endTraining = async () => {
  if (ended.value) return
  ended.value = true
  isEndingTraining.value = true
  try {
    await generateReport()
    stopStream()
    if (!reportId.value) {
      router.push('/workout')
      return
    }
    try {
      const data = await workoutApi.getSession(String(reportId.value))
      endSummary.value = data || null
    } catch (e) {
      endSummary.value = null
    }
    endPageVisible.value = true
  } catch (e) {
    stopStream()
    ElMessage.error('生成 AI 战报失败，请稍后重试')
    router.push('/workout')
  } finally {
    isEndingTraining.value = false
  }
}

const fetchWorkoutName = async () => {
  const id = workoutId.value
  if (!id) return
  try {
    const data = await workoutApi.getWorkout(id)
    workoutName.value = data?.name || ''
  } catch (e) {
    workoutName.value = ''
  }
}

onMounted(async () => {
  await fetchWorkoutName()
  try {
    await startCamera()
  } catch (e) {
    ElMessage.error('摄像头授权失败，请检查浏览器权限设置')
    return
  }

  await loadModel()
})

onBeforeUnmount(() => {
  stopStream()
})
</script>

<style scoped>
.training-container {
  min-height: 100vh;
  background-color: #0b1220;
  padding: 0 24px 24px;
}

.training-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  margin-bottom: 16px;
  color: #fff;
}

.back-btn {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
}

.training-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.training-content {
  max-width: 1440px;
  margin: 0 auto;
}

.video-card,
.status-card {
  border-radius: 14px;
  border: none;
  background: rgba(255, 255, 255, 0.03);
  color: #fff;
}

.video-wrapper {
  position: relative;
  width: 100%;
  height: min(72vh, 760px);
  min-height: 420px;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.35);
}

.camera-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1);
}

.hud {
  position: absolute;
  left: 14px;
  bottom: 14px;
  display: flex;
  gap: 10px;
  background: rgba(0, 0, 0, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  padding: 10px 12px;
  backdrop-filter: blur(8px);
}

.hud-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.hud-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.hud-value {
  font-size: 13px;
  font-weight: 600;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.92);
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-left {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.75);
}

.actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.end-summary {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.end-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.metric-item {
  background: #f5f7fa;
  border-radius: 10px;
  padding: 12px;
}

.metric-label {
  font-size: 12px;
  color: #909399;
}

.metric-value {
  margin-top: 6px;
  font-size: 18px;
  font-weight: 800;
  color: #303133;
}

.end-tips {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  line-height: 1.6;
}

.end-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

:deep(.el-card__header) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
</style>
