<template>
  <div class="settings-wrapper">
    <el-card shadow="never" class="settings-card">
      <el-tabs tab-position="left" class="custom-tabs">

        <el-tab-pane>
          <template #label>
            <span class="custom-tab-label">
              <el-icon>
                <Lock />
              </el-icon> 安全设置
            </span>
          </template>

          <div class="tab-content">
            <h2 class="section-title">修改密码</h2>
            <p class="section-desc">为了您的账号安全，请定期更换密码。</p>

            <el-form ref="passwordFormRef" label-position="top" :model="passwordForm" :rules="passwordRules" status-icon
              class="password-form">
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前使用的密码">
                  <template #prefix><el-icon>
                      <Key />
                    </el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入 6-16 位新密码">
                  <template #prefix><el-icon>
                      <Lock />
                    </el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码">
                  <template #prefix><el-icon>
                      <CircleCheck />
                    </el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item class="form-actions">
                <el-button type="primary" class="full-width-btn" :loading="isUpdatingPassword"
                  @click="handleUpdatePassword">更新密码</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane>
          <template #label>
            <span class="custom-tab-label">
              <el-icon>
                <View />
              </el-icon> 隐私偏好
            </span>
          </template>
          <div class="tab-content">
            <h2 class="section-title">隐私设置</h2>
            <div v-loading="isLoadingPrivacy">
              <div class="privacy-item">
                <div class="info">
                  <span class="title">公开 AI 战报</span>
                  <span class="desc">关闭后：训练结束自动生成的 AI 战报仅自己可见；开启后：他人可在你的动态中看到。</span>
                </div>
                <el-switch v-model="privacyForm.publicAiWorkoutReport" />
              </div>

              <div style="display:flex; justify-content:flex-end; margin-top: 18px;">
                <el-button type="primary" :loading="isSavingPrivacy" @click="savePrivacySettings">保存设置</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane>
          <template #label>
            <span class="custom-tab-label">
              <el-icon>
                <DArrowRight />
              </el-icon> 高级选项
            </span>
          </template>
          <div class="tab-content danger-zone">
            <h2 class="section-title">账号管理</h2>
            <div class="danger-item">
              <div class="info">
                <span class="title">注销账号</span>
                <span class="desc">此操作不可逆，将永久删除您的所有数据。</span>
              </div>
              <el-button type="danger" plain :loading="isDeactivating" @click="handleDeactivateAccount">注销</el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, Key, CircleCheck, View, DArrowRight } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { privacyApi } from '@/api/userSpace'
import { authApi } from '@/api/auth'

const router = useRouter()

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordFormRef = ref(null)
const isUpdatingPassword = ref(false)

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
    return
  }
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }
  callback()
}

const passwordRules = reactive({
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能小于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
})

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
    isUpdatingPassword.value = true
    await request.post('/auth/change-password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    localStorage.removeItem('jwt_token')
    router.push('/login')
  } catch (error) {
  } finally {
    isUpdatingPassword.value = false
  }
}

const privacyForm = reactive({
  publicAiWorkoutReport: true
})

const isLoadingPrivacy = ref(false)
const isSavingPrivacy = ref(false)

const fetchPrivacySettings = async () => {
  try {
    isLoadingPrivacy.value = true
    // 1. 获取响应
    const res = await privacyApi.getMySettings()

    // 2. 根据你的 Result 结构取值
    // 如果拦截器没处理，就用 res.data；处理了就直接用 res
    const actualData = res.data || res

    // 3. 对齐字段名并强制转换类型
    if (actualData && actualData.publicAiReport !== undefined) {
      // 确保后端给的 0/1 或 false 都能准确转为 Boolean
      privacyForm.publicAiWorkoutReport = !!actualData.publicAiReport
    } else {
      privacyForm.publicAiWorkoutReport = true
    }

    console.log("最终赋值给表单的值:", privacyForm.publicAiWorkoutReport)

  } catch (error) {
    console.error("获取隐私设置失败:", error)
  } finally {
    isLoadingPrivacy.value = false
  }
}

const savePrivacySettings = async () => {
  try {
    isSavingPrivacy.value = true

    // 修改点：1. 字段名去掉 Workout  2. 布尔值转为 1/0
    await privacyApi.updateMySettings({
      publicAiReport: privacyForm.publicAiWorkoutReport ? 1 : 0
    })

    ElMessage.success('隐私设置已保存')
  } catch (error) {
    // 建议加上错误捕获，这样后端校验失败时前端会有提示
    console.error('保存隐私设置失败:', error)
  } finally {
    isSavingPrivacy.value = false
  }
}

onMounted(() => {
  fetchPrivacySettings()
})

// 注销账号处理
const isDeactivating = ref(false)

const handleDeactivateAccount = async () => {
  try {
    // 弹出确认对话框，要求用户输入密码
    const { value: password } = await ElMessageBox.prompt(
      '请输入您的密码以确认注销账号（此操作不可逆）',
      '注销账号确认',
      {
        confirmButtonText: '确认注销',
        cancelButtonText: '取消',
        inputType: 'password',
        confirmButtonClass: 'el-button--danger',
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            if (!instance.inputValue) {
              ElMessage.warning('请输入密码')
              return
            }
            done()
          } else {
            done()
          }
        }
      }
    )

    if (!password) return

    isDeactivating.value = true
    await authApi.deactivateAccount({ password })

    ElMessage.success('账号已注销，感谢您的使用')
    // 清除本地存储的token
    localStorage.removeItem('jwt_token')
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('注销失败:', error)
    }
  } finally {
    isDeactivating.value = false
  }
}
</script>

<style scoped>
/* ================= 修改点2：核心居中逻辑 ================= */
.settings-wrapper {
  max-width: 960px;
  /* 限制最大宽度，防​​止在大屏下铺得太开 */
  margin: 40px auto;
  /* 上下留白，左右自动居中 */
  padding: 0 20px;
}

.settings-card {
  border-radius: 16px;
  /* 更现代的圆角 */
  border: none !important;
  /* 添加柔和阴影，增加层次感 */
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05) !important;
}

/* Tab 样式微调 */
.custom-tabs {
  min-height: 480px;
  /* 设定一个最小高度，保证页面饱满 */
}

/* Tab 标签页加上图标 */
.custom-tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  /* 图标和文字的间距 */
  font-size: 15px;
  padding: 10px 0;
}

/* 内容区域样式 */
.tab-content {
  padding: 20px 50px 20px 40px;
  /* 右侧留多点空隙 */
}

.section-title {
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 8px;
  color: #303133;
}

.section-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 30px;
}

/* 表单样式 */
.password-form {
  max-width: 380px;
  /* 表单不建议拉得太长，不利于阅读 */
}

.form-actions {
  margin-top: 30px;
}

.full-width-btn {
  width: 100%;
  height: 40px;
  font-weight: bold;
}

/* 隐私和危险区域样式 */
.privacy-item,
.danger-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.privacy-item .info .title,
.danger-item .info .title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.privacy-item .info .desc,
.danger-item .info .desc {
  font-size: 13px;
  color: #909399;
}

.danger-zone {
  border: 1px solid #fcd3d3;
  background-color: #fef0f0;
  border-radius: 8px;
  padding: 20px;
}
</style>
