<template>
  <div class="onboarding-container">
    <el-card class="onboarding-card">
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="基本信息" />
        <el-step title="身体指标" />
        <el-step title="确定目标" />
      </el-steps>

      <div class="form-content">
        <div v-if="activeStep === 0" class="step-fade">
          <h3>告诉 AI 你的基础情况</h3>
          <p class="subtitle">性别将影响 AI 对你基础代谢的计算公式</p>
          <el-radio-group v-model="form.gender" size="large">
            <el-radio-button label="male">男生 (Male)</el-radio-button>
            <el-radio-button label="female">女生 (Female)</el-radio-button>
          </el-radio-group>
        </div>
        
        <div v-else-if="activeStep === 1" class="step-fade">
          <h3>测量身体数据</h3>
          <p class="subtitle">请尽量提供准确的空腹数据</p>
          <div class="input-group">
            <div class="input-item">
              <span class="label">身高 (cm)</span>
              <el-input-number v-model="form.height" :min="100" :max="250" :precision="1" />
            </div>
            <div class="input-item">
              <span class="label">体重 (kg)</span>
              <el-input-number v-model="form.weight" :min="30" :max="200" :precision="1" />
            </div>
            <div class="input-item">
              <span class="label">体脂率 (%)</span>
              <el-input-number v-model="form.bodyFat" :min="5" :max="50" :precision="1" />
            </div>
          </div>
        </div>

        <div v-else-if="activeStep === 2" class="step-fade">
          <h3>你的健身愿景是？</h3>
          <p class="subtitle">AI 会根据目标调整你的碳水与蛋白质比例</p>
          <div class="goal-selection">
            <div 
              :class="['goal-box', form.goal === 'lose' ? 'active' : '']" 
              @click="form.goal = 'lose'"
            >
              <div class="goal-icon">🔥</div>
              <div class="goal-name">减脂降重</div>
              <div class="goal-desc">刷脂、提高肌肉线条清晰度</div>
            </div>
            <div 
              :class="['goal-box', form.goal === 'gain' ? 'active' : '']" 
              @click="form.goal = 'gain'"
            >
              <div class="goal-icon">💪</div>
              <div class="goal-name">增肌塑形</div>
              <div class="goal-desc">增加肌肉维度、提升力量水平</div>
            </div>
            <div 
              :class="['goal-box', form.goal === 'maintain' ? 'active' : '']" 
              @click="form.goal = 'maintain'"
            >
              <div class="goal-icon">⚖️</div>
              <div class="goal-name">保持身材</div>
              <div class="goal-desc">维持当前体态，提高身体素质</div>
            </div>
          </div>
        </div>
      </div>

      <div class="actions">
        <el-button v-if="activeStep > 0" size="large" round @click="activeStep--">上一步</el-button>
        <el-button 
          type="primary" 
          size="large" 
          round 
          :loading="isSubmitting"
          @click="nextStep"
        >
          {{ activeStep === 2 ? '完成配置，进入 AI 世界' : '下一步' }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeStep = ref(0)
const isSubmitting = ref(false)

// 响应式表单对象
const form = reactive({
  gender: 'male',
  height: 170,
  weight: 65,
  bodyFat: 20,
  goal: 'lose'
})

// 步骤跳转逻辑
const nextStep = () => {
  if (activeStep.value < 2) {
    activeStep.value++
  } else {
    finishOnboarding()
  }
}

// 最终提交逻辑
const finishOnboarding = async () => {
  isSubmitting.value = true
  try {
    // 1. 调用后端接口（此处为模拟）
    // await api.updateUserInfo(form)
    console.log('提交的数据：', form)
    
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 1500))

    // 2. 更新本地标记，通知路由守卫“资料已补全”
    localStorage.setItem('is_first_login', 'false')
    
    // 3. 跳转主页面
    ElMessage.success('配置完成，AI 已为你生成专属健身方案！')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.onboarding-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.onboarding-card {
  width: 600px;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.1);
}

.form-content {
  min-height: 250px;
  padding: 40px 0;
  text-align: center;
}

h3 { font-size: 24px; color: #303133; margin-bottom: 8px; }
.subtitle { color: #909399; font-size: 14px; margin-bottom: 30px; }

/* 目标选择框样式 */
.goal-selection {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.goal-box {
  flex: 1;
  padding: 24px;
  border: 2px solid #f0f2f5;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.goal-box:hover {
  border-color: #409EFF;
  transform: translateY(-5px);
}

.goal-box.active {
  border-color: #409EFF;
  background-color: #ecf5ff;
}

.goal-icon { font-size: 40px; margin-bottom: 12px; }
.goal-name { font-weight: bold; font-size: 18px; margin-bottom: 8px; color: #303133; }
.goal-desc { font-size: 12px; color: #909399; line-height: 1.4; }

.actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.input-group {
  display: flex;
  justify-content: space-around;
  gap: 20px;
}

.input-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.label { font-size: 14px; color: #606266; }

/* 简单的淡入动画 */
.step-fade {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>