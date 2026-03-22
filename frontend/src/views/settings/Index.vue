<template>
  <div class="settings-wrapper">
    <el-card shadow="never" class="settings-card">
      <el-tabs tab-position="left" class="custom-tabs">
        
        <el-tab-pane>
          <template #label>
            <span class="custom-tab-label">
              <el-icon><Lock /></el-icon> 安全设置
            </span>
          </template>
          
          <div class="tab-content">
            <h2 class="section-title">修改密码</h2>
            <p class="section-desc">为了您的账号安全，请定期更换密码。</p>
            
            <el-form label-position="top" :model="passwordForm" status-icon class="password-form">
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前使用的密码">
                  <template #prefix><el-icon><Key /></el-icon></template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入 6-16 位新密码">
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码">
                  <template #prefix><el-icon><CircleCheck /></el-icon></template>
                </el-input>
              </el-form-item>
              
              <el-form-item class="form-actions">
                <el-button type="primary" class="full-width-btn">更新密码</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane>
          <template #label>
            <span class="custom-tab-label">
              <el-icon><View /></el-icon> 隐私偏好
            </span>
          </template>
          <div class="tab-content">
            <h2 class="section-title">隐私设置</h2>
            <div class="privacy-item">
              <div class="info">
                <span class="title">公开训练动态</span>
                <span class="desc">允许其他用户在健身社区看到您的训练记录。</span>
              </div>
              <el-switch v-model="privacyForm.publicProfile" />
            </div>
            <el-divider />
            </div>
        </el-tab-pane>

        <el-tab-pane>
          <template #label>
            <span class="custom-tab-label">
              <el-icon><DArrowRight /></el-icon> 高级选项
            </span>
          </template>
          <div class="tab-content danger-zone">
            <h2 class="section-title">账号管理</h2>
            <div class="danger-item">
              <div class="info">
                <span class="title">注销账号</span>
                <span class="desc">此操作不可逆，将永久删除您的所有数据。</span>
              </div>
              <el-button type="danger" plain>注销</el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Lock, Key, CircleCheck, Microphone, View, DArrowRight } from '@element-plus/icons-vue'

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const privacyForm = reactive({
  publicProfile: true
})
</script>

<style scoped>
/* ================= 修改点2：核心居中逻辑 ================= */
.settings-wrapper {
  max-width: 960px; /* 限制最大宽度，防​​止在大屏下铺得太开 */
  margin: 40px auto; /* 上下留白，左右自动居中 */
  padding: 0 20px;
}

.settings-card {
  border-radius: 16px; /* 更现代的圆角 */
  border: none !important;
  /* 添加柔和阴影，增加层次感 */
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05) !important; 
}

/* Tab 样式微调 */
.custom-tabs {
  min-height: 480px; /* 设定一个最小高度，保证页面饱满 */
}

/* Tab 标签页加上图标 */
.custom-tab-label {
  display: flex;
  align-items: center;
  gap: 8px; /* 图标和文字的间距 */
  font-size: 15px;
  padding: 10px 0;
}

/* 内容区域样式 */
.tab-content {
  padding: 20px 50px 20px 40px; /* 右侧留多点空隙 */
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
  max-width: 380px; /* 表单不建议拉得太长，不利于阅读 */
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
.privacy-item, .danger-item {
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