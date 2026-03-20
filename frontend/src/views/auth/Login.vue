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
              <el-input v-model="loginForm.username" placeholder="请输入用户名/邮箱" :prefix-icon="User" clearable />
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
            <el-form-item prop="email">
              <div class="email-input-wrapper">
                <el-input v-model="registerForm.email" placeholder="请输入邮箱地址" :prefix-icon="Message" clearable />
                <el-button 
                  type="primary" 
                  plain 
                  class="code-btn" 
                  :disabled="isSendingCode || countdown > 0"
                  @click="handleSendCode"
                >
                  {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item prop="code">
              <el-input v-model="registerForm.code" placeholder="请输入 6 位验证码" :prefix-icon="Lock" maxlength="6" />
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
import { User, Lock, Message } from '@element-plus/icons-vue'

import request from '@/utils/request'

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
  email: '',
  username: '',
  password: '',
  confirmPassword: '',
  code: ''
})

const loginFormRef = ref(null)
const registerFormRef = ref(null)

// ================= 自定义校验逻辑 =================
// 校验用户名唯一性
const validateUsernameUnique = async (rule, value, callback) => {
  if (!value) return callback()
  if (value.length < 3) return callback()
  
  try {
    const isExists = await request.get(`/auth/check-username?username=${value}`)
    if (isExists) {
      callback(new Error('该用户名已被占用'))
    } else {
      callback()
    }
  } catch (error) {
    callback() // 接口异常时不阻塞注册
  }
}

// 校验邮箱唯一性
const validateEmailUnique = async (rule, value, callback) => {
  if (!value) return callback()
  const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!emailPattern.test(value)) return callback()

  try {
    const isExists = await request.get(`/auth/check-email?email=${value}`)
    if (isExists) {
      callback(new Error('该邮箱已被注册'))
    } else {
      callback()
    }
  } catch (error) {
    callback()
  }
}

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

// 规则绑定
const loginRules = reactive({
  username: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const registerRules = reactive({
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
    { validator: validateEmailUnique, trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 15, message: '长度在 3 到 15 个字符', trigger: 'blur' },
    { validator: validateUsernameUnique, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码必须为6位数字', trigger: 'blur' }
  ]
})

// ================= 核心业务逻辑 (含防抖机制) =================

// 验证码倒计时逻辑
const countdown = ref(0)
const isSendingCode = ref(false)
let timer = null

const startCountdown = () => {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleSendCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱地址')
    return
  }
  // 校验邮箱格式
  const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!emailPattern.test(registerForm.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  try {
    isSendingCode.value = true
    await request.post(`/auth/code?email=${registerForm.email}`)
    ElMessage.success('验证码已发送，请查收邮箱')
    startCountdown()
  } catch (error) {
    console.error('Send code error:', error)
  } finally {
    isSendingCode.value = false
  }
}

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
  
  try {
    // 开启表单校验
    await loginFormRef.value.validate()
    
    isLoading.value = true 
    
    const response = await request.post('/auth/login', {
      username: loginForm.username,
      password: loginForm.password
    })

    ElMessage.success('登录成功！')
    
    // 1. 存储 Token
    localStorage.setItem('jwt_token', response.token)
    
    // 2. 存储首次登录标记 (后端字段名为 firstLogin)
    const isFirst = !!response.firstLogin
    localStorage.setItem('is_first_login', isFirst.toString())

    // 3. 核心跳转逻辑
    if (isFirst) {
      router.push('/onboarding')
    } else {
      router.push('/community')
    }
  } catch (error) {
    // 校验失败或请求失败
    console.error('Login error:', error)
  } finally {
    isLoading.value = false
  }
}

// 使用防抖包装登录提交，防止狂点回车或鼠标
const handleDebounceLogin = debounce(executeLogin, 300)

// 注册逻辑
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    // 1. 开启表单校验
    await registerFormRef.value.validate()
    
    isLoading.value = true
    
    // 2. 调用注册接口
    await request.post('/auth/register', {
      email: registerForm.email,
      username: registerForm.username,
      password: registerForm.password,
      code: registerForm.code
    })

    ElMessage.success('注册成功！请登录')
    
    // 3. 注册成功后的 UI 处理
    activeTab.value = 'login'
    loginForm.username = registerForm.username
    
    // 清空注册表单
    registerForm.email = ''
    registerForm.username = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerForm.code = ''
    
    // 重置倒计时
    if (timer) clearInterval(timer)
    countdown.value = 0
    
  } catch (error) {
    console.error('Register error:', error)
  } finally {
    isLoading.value = false
  }
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
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 20px;
}

.email-input-wrapper {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-btn {
  white-space: nowrap;
  min-width: 100px;
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