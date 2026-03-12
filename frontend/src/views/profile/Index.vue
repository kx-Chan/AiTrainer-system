<template>
  <div class="profile-container">
    <el-card class="profile-header-card" shadow="never">
      <div class="user-info-wrapper">
        <div class="avatar-section">
          <el-avatar :size="100" :src="userInfo.avatar" />
        </div>
        
        <div class="info-section">
          <div class="name-row">
            <h2 class="nickname">{{ userInfo.nickname }}</h2>
            <el-tag type="success" effect="light" round size="small">Lv.4 健身达人</el-tag>
            <el-tag type="warning" effect="light" round size="small" style="margin-left: 8px;">目标: {{ userInfo.goal }}</el-tag>
            
            <el-button type="primary" :icon="Edit" plain size="small" class="edit-btn" @click="openEditModal">编辑资料</el-button>
          </div>
          <p class="bio">{{ userInfo.bio }}</p>
          
          <div class="stats-row">
            <div class="stat-item"><span class="stat-value">{{ userInfo.following }}</span><span class="stat-label">关注</span></div>
            <div class="stat-divider"></div>
            <div class="stat-item"><span class="stat-value">{{ userInfo.followers }}</span><span class="stat-label">粉丝</span></div>
            <div class="stat-divider"></div>
            <div class="stat-item"><span class="stat-value">{{ userInfo.totalDays }}</span><span class="stat-label">累计打卡(天)</span></div>
          </div>
        </div>
      </div>
    </el-card>

    <div class="section-title"><el-icon><Medal /></el-icon> 荣誉徽章墙</div>
    <el-row :gutter="20" class="badge-row">
      <el-col :span="6" v-for="badge in badgeList" :key="badge.id">
        <el-tooltip :content="badge.desc" placement="top" effect="light">
          <el-card class="badge-card" :class="{ 'is-locked': !badge.unlocked }" shadow="hover">
            <div class="badge-icon">{{ badge.icon }}</div>
            <div class="badge-name">{{ badge.name }}</div>
            <div v-if="!badge.unlocked" class="lock-mask"><el-icon><Lock /></el-icon></div>
          </el-card>
        </el-tooltip>
      </el-col>
    </el-row>

    <el-card class="content-card" shadow="never">
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane label="我的推文" name="posts">
          <div class="post-list">
            <el-card v-for="post in postList" :key="post.id" class="post-item" shadow="hover">
              <div class="post-header">
                <span class="post-time">{{ post.time }}</span>
                <el-tag :type="post.type === 'AI战报' ? 'warning' : 'info'" size="small">{{ post.type }}</el-tag>
              </div>
              <p class="post-content">{{ post.content }}</p>
            </el-card>
          </div>
        </el-tab-pane>
        </el-tabs>
    </el-card>

    <el-dialog 
      v-model="isEditVisible" 
      title="编辑个人资料" 
      width="450px" 
      destroy-on-close
      class="edit-dialog"
    >
      <el-form 
        ref="editFormRef" 
        :model="editForm" 
        :rules="editRules" 
        label-width="80px"
      >
        <el-form-item label="头像">
          <div class="avatar-uploader-mock">
            <el-avatar :size="60" :src="editForm.avatar" />
            <div class="upload-hint">点击更换图片 (开发中)</div>
          </div>
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" maxlength="15" show-word-limit placeholder="请输入炫酷的昵称" />
        </el-form-item>

        <el-form-item label="当前目标" prop="goal">
          <el-radio-group v-model="editForm.goal">
            <el-radio-button label="减脂">🔥 减脂</el-radio-button>
            <el-radio-button label="增肌">💪 增肌</el-radio-button>
            <el-radio-button label="塑形">✨ 塑形</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="个性签名" prop="bio">
          <el-input 
            v-model="editForm.bio" 
            type="textarea" 
            :rows="3" 
            maxlength="50" 
            show-word-limit 
            placeholder="写下你的健身宣言吧..." 
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isEditVisible = false">取 消</el-button>
          <el-button type="primary" :loading="isSaving" @click="saveProfile">保存修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Edit, Medal, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// ================= 1. 页面渲染数据 (真实展示的数据) =================
const userInfo = reactive({
  avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
  nickname: '陈同学_AiTrainer',
  goal: '减脂',
  bio: '生命在于折腾，代码与铁块齐飞。目标：本月体脂率降至 15%！',
  following: 24,
  followers: 128,
  totalDays: 45
})

// ================= 2. 编辑弹窗逻辑与数据隔离 =================
const isEditVisible = ref(false)
const isSaving = ref(false)
const editFormRef = ref(null)

// 专门用于在弹窗里编辑的“草稿数据”
const editForm = reactive({
  avatar: '',
  nickname: '',
  goal: '',
  bio: ''
})

// 表单校验规则
const editRules = reactive({
  nickname: [
    { required: true, message: '昵称不能为空', trigger: 'blur' },
    { min: 2, max: 15, message: '长度在 2 到 15 个字符', trigger: 'blur' }
  ],
  bio: [
    { required: true, message: '个性签名不能为空', trigger: 'blur' }
  ]
})

// 打开弹窗：将现有数据深拷贝一份给草稿箱
const openEditModal = () => {
  editForm.avatar = userInfo.avatar
  editForm.nickname = userInfo.nickname
  editForm.goal = userInfo.goal
  editForm.bio = userInfo.bio
  isEditVisible.value = true
}

// 保存修改：校验 -> 模拟请求 -> 同步数据
const saveProfile = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate((valid) => {
    if (valid) {
      isSaving.value = true // 开启按钮 Loading
      
      // 模拟将数据发送给后端的网络延迟
      setTimeout(() => {
        // 请求成功后，将草稿箱的数据真正同步给页面的 userInfo
        userInfo.nickname = editForm.nickname
        userInfo.goal = editForm.goal
        userInfo.bio = editForm.bio
        
        isSaving.value = false
        isEditVisible.value = false // 关闭弹窗
        ElMessage.success('个人资料更新成功！')
      }, 800)
    }
  })
}

// ================= 3. 其他保持不变的模拟数据 =================
const badgeList = reactive([
  { id: 1, name: '初入训练场', icon: '🏃', desc: '完成首次 AI 动作识别', unlocked: true },
  { id: 2, name: '钢铁大腿', icon: '🦵', desc: '累计完成 1000 个标准深蹲', unlocked: true },
  { id: 3, name: '自律机器', icon: '🔥', desc: '连续打卡 7 天', unlocked: true },
  { id: 4, name: '早鸟修仙', icon: '🌅', desc: '在早上 6:00 前完成一次训练', unlocked: false }
])

const activeTab = ref('posts')
const postList = reactive([
  { id: 1, time: '2026-03-12 10:30', type: 'AI战报', content: '今天使用了 AiTrainer 的深蹲模式，AI 姿态评分高达 92 分！核心收紧的感觉越来越好了。' }
])
</script>

<style scoped>
/* 保持原有基础样式，新增弹窗内的样式 */
.profile-container { max-width: 1000px; margin: 0 auto; animation: fadeIn 0.5s ease; }
.profile-header-card { border-radius: 16px; background: linear-gradient(135deg, #ffffff 0%, #f8faff 100%); margin-bottom: 30px; }
.user-info-wrapper { display: flex; align-items: center; gap: 30px; padding: 10px; }
.avatar-section .el-avatar { border: 4px solid #fff; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
.info-section { flex: 1; }
.name-row { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.nickname { margin: 0; font-size: 24px; color: #303133; }
.edit-btn { margin-left: auto; border-radius: 20px; }
.bio { color: #606266; font-size: 14px; margin-top: 0; margin-bottom: 20px; }
.stats-row { display: flex; align-items: center; gap: 20px; }
.stat-item { display: flex; flex-direction: column; align-items: center; }
.stat-value { font-size: 20px; font-weight: bold; color: #303133; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
.stat-divider { width: 1px; height: 30px; background-color: #ebeef5; }
.section-title { font-size: 18px; font-weight: bold; color: #303133; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }
.badge-row { margin-bottom: 30px; }
.badge-card { text-align: center; border-radius: 12px; position: relative; cursor: pointer; border: 1px solid #f0f2f5; }
.badge-card.is-locked { filter: grayscale(100%); opacity: 0.6; }
.badge-icon { font-size: 40px; margin-bottom: 10px; }
.badge-name { font-size: 14px; font-weight: bold; }
.lock-mask { position: absolute; top: 10px; right: 10px; color: #909399; font-size: 18px; }

/* ======== 新增：编辑弹窗内部样式 ======== */
.avatar-uploader-mock {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background-color 0.3s;
}
.avatar-uploader-mock:hover {
  background-color: #f5f7fa;
}
.upload-hint {
  font-size: 13px;
  color: #409eff;
}
.edit-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
  padding-bottom: 10px;
}
</style>