<template>
  <div class="auth-container">
    <div class="bg-shape shape1"></div>
    <div class="bg-shape shape2"></div>

    <el-card class="auth-card" shadow="hover">
      <div class="auth-header">
        <h2 class="title">AiTrainer</h2>
        <p class="subtitle">你的智能 AI 健身教练</p>
      </div>

      <el-tabs v-model="activeTab" class="auth-tabs">
        
        <el-tab-pane label="密码登录" name="login">
          <el-form 
            ref="loginFormRef" 
            :model="loginForm" 
            :rules="loginRules" 
            size="large"
            @keyup.enter="handleDebounceLogin"
          >
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名/手机号" :prefix-icon="User" clearable />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
            </el-form-item>

            <div class="login-options">
              <el-checkbox v-model="loginForm.rememberMe">7天内免登录</el-checkbox>
              <el-link type="primary" :underline="false">忘记密码？</el-link>
            </div>

            <el-form-item>
              <el-button 
                type="primary" 
                class="submit-btn" 
                :loading="isLoading" 
                @click="handleDebounceLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="新用户注册" name="register">
          <el-form 
            ref="registerFormRef" 
            :model="registerForm" 
            :rules="registerRules" 
            size="large"
          >
            <el-form-item prop="phone">
              <el-input v-model="registerForm.phone" placeholder="请输入11位手机号" :prefix-icon="Phone" clearable />
            </el-form-item>

            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="设置用户名 (不可重复)" :prefix-icon="User" clearable />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="设置密码 (至少6位)" :prefix-icon="Lock" show-password />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次确认密码" :prefix-icon="Lock" show-password />
            </el-form-item>

            <el-form-item>
              <el-button 
                type="success" 
                class="submit-btn" 
                :loading="isLoading" 
                @click="handleRegister"
              >
                立即注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone } from '@element-plus/icons-vue'

const router = useRouter()

// 状态控制
const activeTab = ref('login')
const isLoading = ref(false)

// ================= 表单数据模型 =================
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const registerForm = reactive({
  phone: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const loginFormRef = ref(null)
const registerFormRef = ref(null)

// ================= 自定义校验逻辑 =================
// 校验两次密码是否一致
const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

// 校验手机号格式 (简单的正则)
const validatePhone = (rule, value, callback) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  if (value === '') {
    callback(new Error('请输入手机号'))
  } else if (!phoneRegex.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

// 规则绑定
const loginRules = reactive({
  username: [{ required: true, message: '请输入用户名或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const registerRules = reactive({
  phone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 15, message: '长度在 3 到 15 个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }]
})

// ================= 核心业务逻辑 (含防抖机制) =================

// 手写一个防抖函数 (Debounce)
const debounce = (fn, delay = 500) => {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

// 真实的登录逻辑
const executeLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate((valid) => {
    if (valid) {
      isLoading.value = true 
      
      // 模拟网络请求延时
      setTimeout(() => {
        isLoading.value = false
        
        // 模拟后端返回的数据结构
        // 假设：admin 是老用户，newbie 是新用户
        const mockResponse = {
          token: 'mock_token_ai_trainer_123',
          isFirstLogin: loginForm.username === 'newbie' // 如果用户名是 newbie，判定为首次登录
        }

        if ((loginForm.username === 'admin' || loginForm.username === 'newbie') && loginForm.password === '123456') {
          ElMessage.success('登录成功！')
          
          // 1. 存储 Token
          localStorage.setItem('jwt_token', mockResponse.token)
          
          // 2. 存储首次登录标记 (字符串形式存入 localStorage)
          localStorage.setItem('is_first_login', mockResponse.isFirstLogin.toString())

          // 3. 核心跳转逻辑：如果是新用户去引导页，老用户去社区或看板
          if (mockResponse.isFirstLogin) {
            router.push('/onboarding')
          } else {
            router.push('/community')
          }
        } else {
          ElMessage.error('用户名或密码错误 (测试: admin 或 newbie / 123456)')
        }
      }, 1000)
    }
  })
}

// 使用防抖包装登录提交，防止狂点回车或鼠标
const handleDebounceLogin = debounce(executeLogin, 300)

// 注册逻辑
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate((valid) => {
    if (valid) {
      isLoading.value = true
      
      setTimeout(() => {
        isLoading.value = false
        ElMessage.success('注册成功！请使用刚才的账号登录完成资料补全')
        
        // 注册成功后，切换到登录 Tab
        activeTab.value = 'login'
        loginForm.username = registerForm.username
        
        // 💡 提示：在真实后端中，这个新用户在 DB 里的 height/weight 为空
        // 所以下次他登录时，后端接口自然会返回 isFirstLogin: true
      }, 1000)
    }
  })
}
</script>

<style scoped>
/* ================= 极简科技风样式 ================= */
.auth-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f0f4f8; /* 浅灰蓝背景 */
  position: relative;
  overflow: hidden;
}

/* 背景几何装饰 (增加极客感) */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
}
.shape1 {
  width: 400px;
  height: 400px;
  background: rgba(64, 158, 255, 0.2);
  top: -100px;
  left: -100px;
}
.shape2 {
  width: 500px;
  height: 500px;
  background: rgba(103, 194, 58, 0.15);
  bottom: -150px;
  right: -100px;
}

.auth-card {
  width: 420px;
  z-index: 1;
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.05);
  border: none;
}

.auth-header {
  text-align: center;
  margin-bottom: 24px;
}

.title {
  margin: 0;
  font-size: 28px;
  color: #409EFF;
  letter-spacing: 1px;
}

.subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: #909399;
}

.auth-tabs :deep(.el-tabs__nav-scroll) {
  display: flex;
  justify-content: center;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.submit-btn {
  width: 100%;
  font-size: 16px;
  border-radius: 8px;
  letter-spacing: 2px;
}
</style>