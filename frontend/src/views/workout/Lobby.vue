<template>
  <div class="lobby-container">
    <div class="hero-banner">
      <div class="hero-content">
        <h1 class="hero-title">AiTrainer <span class="highlight">视觉引擎</span> 已就绪</h1>
        <p class="hero-subtitle">无需穿戴设备，只需打开摄像头，即刻开启毫秒级骨骼点追踪与动作纠错。</p>
        <div class="hero-stats">
          <div class="stat-badge">
            <el-icon><VideoCamera /></el-icon> 实时帧率: 30 FPS
          </div>
          <div class="stat-badge">
            <el-icon><Aim /></el-icon> 识别延迟: < 50ms
          </div>
        </div>
      </div>
      <div class="cyber-grid"></div>
    </div>

    <div class="section-header">
      <h2><el-icon><Lightning /></el-icon> 选择训练项目</h2>
      <el-radio-group v-model="filterType" size="small">
        <el-radio-button label="all">全部项目</el-radio-button>
        <el-radio-button label="leg">下肢核心</el-radio-button>
        <el-radio-button label="back">背部塑形</el-radio-button>
      </el-radio-group>
    </div>

    <el-row :gutter="24" class="workout-grid">
      <el-col :span="8" v-for="workout in workoutList" :key="workout.id">
        <el-card class="workout-card" shadow="hover" :style="{ '--theme-color': workout.color }">
          
          <div class="card-visual" :class="workout.id">
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

            <el-button 
              class="start-btn" 
              type="primary" 
              round 
              @click="handleStartWorkout(workout)"
            >
              <el-icon class="el-icon--left"><VideoPlay /></el-icon> 开启 AI 训练
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus'
import { VideoCamera, Aim, Lightning, VideoPlay } from '@element-plus/icons-vue'

const router = useRouter()
const filterType = ref('all')

// ================= AI 健身项目数据 =================
const workoutList = reactive([
  { 
    id: 'squat', 
    name: 'AI 杠铃深蹲', 
    enName: 'Barbell Squat', 
    difficulty: 3, 
    tags: ['臀腿', '力量'], 
    desc: '全面激活下肢力量，AI 实时监测膝盖轨迹、髋部深度与背部角度。', 
    color: '#409EFF' // 科技蓝
  },
  { 
    id: 'lunge', 
    name: 'AI 箭步蹲', 
    enName: 'Lunge', 
    difficulty: 2, 
    tags: ['单边控制', '塑形'], 
    desc: '改善左右发力不均，精准打击臀大肌下沿，AI 严控前膝过伸问题。', 
    color: '#67C23A' // 活力绿
  },
  { 
    id: 'good_morning', 
    name: '早安式体前屈', 
    enName: 'Good Morning', 
    difficulty: 4, 
    tags: ['核心', '腘绳肌'], 
    desc: '强化下背部与核心稳定，极度依赖动作标准度，AI 严苛监测脊柱中立位。', 
    color: '#E6A23C' // 警告橙
  }
])

// ================= 启动训练逻辑 (模拟调用底层黑盒) =================
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
    // 模拟开启摄像头的加载过程
    const loading = ElLoading.service({
      lock: true,
      text: '正在调起本地系统摄像头...',
      background: 'rgba(0, 0, 0, 0.8)',
    })

    setTimeout(() => {
      loading.setText('正在加载骨骼关键点识别模型 (Pose Estimation)...')
      
      setTimeout(() => {
        loading.close()
        ElMessage.success({ message: '引擎就绪！开始您的训练！', duration: 3000 })
        
        // -------------------------------------------------------------
        // 【重要说明】：由于你的真实 CV 训练界面是不开源的底层黑盒
        // 在实际开发中，这里可能会触发一个本地协议，或者直接弹出一个不可见的组件。
        // 为了演示完整闭环，我们在这里模拟用户训练了 20 分钟后，
        // 算法将数据返回，并自动跳转到了我们规划好的“AI 训练结算报告页”。
        // -------------------------------------------------------------
        
        // 假设算法返回了一个本次训练的流水号 ID (例如 9527)
        const mockReportId = 9527
        router.push(`/workout/report/${mockReportId}`)

      }, 1500)
    }, 1000)
  }).catch(() => {
    ElMessage.info('训练已取消')
  })
}
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
  box-shadow: 0 16px 32px rgba(var(--theme-color), 0.1);
}

/* 顶部视觉区 (纯 CSS 模拟科技感背景) */
.card-visual {
  height: 160px;
  background-color: #f5f7fa;
  position: relative;
  overflow: hidden;
}

.card-visual.squat { background: radial-gradient(circle at right bottom, #e6f1fc, #f5f7fa); }
.card-visual.lunge { background: radial-gradient(circle at right bottom, #f0f9eb, #f5f7fa); }
.card-visual.good_morning { background: radial-gradient(circle at right bottom, #fdf6ec, #f5f7fa); }

.overlay-gradient {
  position: absolute;
  bottom: 0; left: 0; right: 0; height: 50%;
  background: linear-gradient(to top, rgba(0,0,0,0.05), transparent);
}

.visual-tags {
  position: absolute;
  top: 16px; left: 16px;
  z-index: 2;
}

/* 纯 CSS 模拟骨骼追踪连线动画 */
.skeleton-line {
  position: absolute;
  background: var(--theme-color);
  opacity: 0.4;
  box-shadow: 0 0 8px var(--theme-color);
}
.line-1 {
  width: 2px; height: 60px;
  bottom: 20px; right: 60px;
  transform: rotate(15deg);
  animation: pulse 2s infinite alternate;
}
.line-2 {
  width: 80px; height: 2px;
  bottom: 80px; right: 40px;
  transform: rotate(-10deg);
  animation: pulse 2s infinite alternate 0.5s;
}

@keyframes pulse {
  0% { opacity: 0.2; }
  100% { opacity: 0.8; }
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
  height: 42px; /* 固定高度防止换行错位 */
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
.meta-item:last-child { margin-bottom: 0; }

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