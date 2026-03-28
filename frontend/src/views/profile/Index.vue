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
            <el-tag type="warning" effect="light" round size="small" style="margin-left: 8px;">目标: {{ userInfo.goal }}</el-tag>
            
            <el-button type="primary" :icon="Edit" plain size="small" class="edit-btn" @click="openEditModal">编辑资料</el-button>
          </div>
          <p class="bio">{{ userInfo.bio }}</p>
          
          <div class="stats-row">
            <div class="stat-item clickable" @click="openFollowDialog('following')">
              <span class="stat-value">{{ userInfo.following }}</span>
              <span class="stat-label">关注</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item clickable" @click="openFollowDialog('followers')">
              <span class="stat-value">{{ userInfo.followers }}</span>
              <span class="stat-label">粉丝</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item clickable" @click="isCalendarVisible = true">
              <span class="stat-value">{{ userInfo.totalDays }}</span>
              <span class="stat-label">累计打卡(天) <el-icon><Calendar /></el-icon></span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <div class="section-title">
      <div style="display: flex; align-items: center; gap: 8px;">
        <el-icon><Medal /></el-icon> 荣誉徽章墙
      </div>
      <el-button link type="primary" @click="isBadgeVisible = true">查看全部 <el-icon><ArrowRight /></el-icon></el-button>
    </div>
    
    <el-row :gutter="20" class="badge-row">
      <el-col :span="6" v-for="badge in badgeList.slice(0, 4)" :key="badge.id">
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

        <el-tab-pane label="我的足迹" name="footprints">
          <div class="footprint-filters">
            <el-radio-group v-model="footprintFilter" size="small">
              <el-radio-button label="liked">我赞过的</el-radio-button>
              <el-radio-button label="commented">我评论的</el-radio-button>
            </el-radio-group>
          </div>
          <div class="post-list" style="margin-top: 16px;">
            <el-card v-for="post in footprintPosts" :key="post.id" class="post-item" shadow="hover" style="margin-bottom: 12px;">
              <div style="display:flex;gap:12px;align-items:center;">
                <el-avatar :size="32" :src="post.avatar" />
                <div style="flex:1;">
                  <div style="font-weight:600;">{{ post.author }} <el-tag v-if="post.isPro" type="warning" size="small" effect="dark" round class="pro-tag">PRO</el-tag></div>
                  <div style="color:#909399;font-size:12px;">{{ post.time }} · {{ post.device }}</div>
                </div>
              </div>
              <div style="margin-top:8px;">
                <span v-if="post.topic" style="color:#409EFF;margin-right:4px;">{{ post.topic }}</span>{{ post.content }}
              </div>
            </el-card>
            <div style="display:flex;justify-content:center;margin-top:12px;">
              <el-pagination
                v-model:current-page="footprintPage.page"
                v-model:page-size="footprintPage.size"
                :total="footprintPage.total"
                layout="prev, pager, next"
                @current-change="fetchFootprints"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的收藏" name="collections">
          <el-row :gutter="20">
            <el-col :span="8" v-for="folder in collectionFolders" :key="folder.id">
              <el-card class="folder-card" shadow="hover">
                <el-icon class="folder-icon"><FolderOpened /></el-icon>
                <div class="folder-name">{{ folder.name }}</div>
                <div class="folder-count">{{ folder.count }} 篇内容</div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

      </el-tabs>
    </el-card>

    <el-dialog v-model="isEditVisible" title="编辑个人资料" width="450px" destroy-on-close class="edit-dialog" @close="cancelEdit">
      <el-form label-width="80px" :model="editForm">
        <el-form-item label="用户头像">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
          >
            <img v-if="editForm.avatar" :src="editForm.avatar" class="uploaded-avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">点击头像可重新上传本地图片 (支持 jpg/png)</div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="健身目标">
          <el-radio-group v-model="editForm.goal">
            <el-radio-button label="减脂">减脂</el-radio-button>
            <el-radio-button label="增肌">增肌</el-radio-button>
            <el-radio-button label="保持">保持</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身高 (cm)">
          <el-input-number v-model="editForm.height" :min="100" :max="250" />
        </el-form-item>
        <el-form-item label="体重 (kg)">
          <el-input-number v-model="editForm.weight" :min="30" :max="200" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="体脂率 (%)">
          <el-input-number v-model="editForm.bodyFat" :min="1" :max="50" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="个性签名">
          <el-input 
            v-model="editForm.bio" 
            type="textarea" 
            :rows="3" 
            placeholder="写一句激励自己的话吧..." 
            maxlength="100" 
            show-word-limit 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="isAvatarUploading" @click="saveProfile">保存修改</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="isFollowVisible" :title="followDialogType === 'followers' ? '我的粉丝' : '我的关注'" width="400px">
      <div class="follow-list">
        <div v-for="user in followList" :key="user.id" class="follow-item">
          <el-avatar :size="40" :src="user.avatar" />
          <div class="follow-info">
            <div class="follow-name">{{ user.name }}</div>
            <div class="follow-bio">{{ user.bio }}</div>
          </div>
          <el-button
            :type="user.isFollowing ? 'default' : 'primary'"
            size="small"
            plain
            round
            :loading="followActionLoadingId === user.id"
            @click="toggleFollow(user)"
          >
            {{ user.isFollowing ? '已关注' : '+ 关注' }}
          </el-button>
        </div>
      </div>
      <div style="display: flex; justify-content: center; margin-top: 16px;">
        <el-pagination
          v-model:current-page="followPage.page"
          v-model:page-size="followPage.size"
          :total="followPage.total"
          :pager-count="5"
          layout="prev, pager, next"
          @current-change="fetchFollowList"
        />
      </div>
    </el-dialog>

    <el-dialog v-model="isCalendarVisible" title="我的训练打卡记录" width="600px">
      <el-calendar class="custom-calendar">
        <template #date-cell="{ data }">
          <div class="calendar-cell">
            <span :class="{ 'is-today': data.isToday }">{{ data.day.split('-').slice(2).join('') }}</span>
            <div v-if="checkinDays.includes(data.day)" class="checkin-dot"></div>
          </div>
        </template>
      </el-calendar>
    </el-dialog>

    <el-dialog v-model="isBadgeVisible" title="成就徽章图鉴" width="700px">
      <el-alert title="解锁更多徽章，彰显你的自律荣誉！" type="success" :closable="false" style="margin-bottom: 20px;" />
      <el-row :gutter="16">
        <el-col :span="6" v-for="badge in badgeList" :key="badge.id" style="margin-bottom: 16px;">
          <el-tooltip :content="badge.desc" placement="top" effect="light">
            <el-card class="badge-card" :class="{ 'is-locked': !badge.unlocked }" shadow="never" style="background-color: #f8f9fa;">
              <div class="badge-icon">{{ badge.icon }}</div>
              <div class="badge-name">{{ badge.name }}</div>
              <div v-if="!badge.unlocked" class="lock-mask"><el-icon><Lock /></el-icon></div>
            </el-card>
          </el-tooltip>
        </el-col>
      </el-row>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watchEffect } from 'vue'
import { Edit, Medal, Lock, FolderOpened, ArrowRight, Calendar, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus' 
import request from '@/utils/request'

// ================= 基础数据 =================
const userInfo = reactive({
  avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
  nickname: '',
  gender: '',
  goal: '',
  bio: '',
  following: 0,
  followers: 0,
  totalDays: 0,
  height: null,
  weight: null,
  bodyFat: null
})

// 获取个人资料
const fetchProfile = async () => {
  try {
    const data = await request.get('/profile/info')
    if (data) {
      Object.assign(userInfo, data)
    }
    return data
  } catch (error) {
    console.error('获取个人资料失败:', error)
    return null
  }
}

onMounted(() => {
  fetchProfile()
})

const isEditVisible = ref(false)
const editForm = reactive({
  avatar: '',
  nickname: '',
  gender: '',
  goal: '',
  bio: '',
  height: null,
  weight: null,
  bodyFat: null
})

const pendingAvatarFile = ref(null)
const pendingAvatarPreviewUrl = ref('')

const openEditModal = async () => {
  let data = null
  try {
    data = await request.get('/profile/info')
  } catch (error) {
    data = null
  }

  const source = data || userInfo
  editForm.avatar = source.avatar || ''
  editForm.nickname = source.nickname || ''
  editForm.gender = source.gender || ''
  editForm.goal = source.goal || ''
  editForm.bio = source.bio || ''
  editForm.height = source.height ?? null
  editForm.weight = source.weight ?? null
  editForm.bodyFat = source.bodyFat ?? null

  pendingAvatarFile.value = null
  if (pendingAvatarPreviewUrl.value) {
    URL.revokeObjectURL(pendingAvatarPreviewUrl.value)
    pendingAvatarPreviewUrl.value = ''
  }

  if (data) {
    Object.assign(userInfo, data)
  }
  isEditVisible.value = true
}

// ================= 头像上传与本地预览逻辑 =================
const isAvatarUploading = ref(false)

const handleAvatarChange = (uploadFile) => {
  const file = uploadFile?.raw
  if (!file) {
    ElMessage.error('未获取到头像文件')
    return
  }

  if (!file.type?.startsWith('image/')) {
    ElMessage.error('头像只能是图片格式!')
    return
  }
  
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('头像文件不能超过 2MB')
    return
  }

  if (pendingAvatarPreviewUrl.value) {
    URL.revokeObjectURL(pendingAvatarPreviewUrl.value)
    pendingAvatarPreviewUrl.value = ''
  }

  const previewUrl = URL.createObjectURL(file)
  pendingAvatarPreviewUrl.value = previewUrl
  pendingAvatarFile.value = file
  editForm.avatar = previewUrl
}

const saveProfile = async () => {
  try {
    if (isAvatarUploading.value) {
      ElMessage.warning('头像上传中，请稍后再保存')
      return
    }

    if (pendingAvatarFile.value) {
      isAvatarUploading.value = true
      const formData = new FormData()
      formData.append('file', pendingAvatarFile.value)
      const avatarUrl = await request.post('/common/upload/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      if (!avatarUrl) {
        ElMessage.error('头像上传失败，请稍后重试')
        return
      }
      editForm.avatar = avatarUrl
    }

    await request.post('/profile/update', {
      nickname: editForm.nickname,
      gender: editForm.gender,
      goal: editForm.goal,
      bio: editForm.bio,
      height: editForm.height,
      weight: editForm.weight,
      bodyFat: editForm.bodyFat
    })
    isEditVisible.value = false
    ElMessage.success('个人资料已保存！')
    const latest = await fetchProfile()
    window.dispatchEvent(new CustomEvent('profile:updated', {
      detail: {
        avatar: latest?.avatar,
        nickname: latest?.nickname
      }
    }))
  } catch (error) {
    console.error('保存个人资料失败:', error)
  } finally {
    isAvatarUploading.value = false
    pendingAvatarFile.value = null
    if (pendingAvatarPreviewUrl.value) {
      URL.revokeObjectURL(pendingAvatarPreviewUrl.value)
      pendingAvatarPreviewUrl.value = ''
    }
  }
}

const cancelEdit = () => {
  isEditVisible.value = false
  pendingAvatarFile.value = null
  if (pendingAvatarPreviewUrl.value) {
    URL.revokeObjectURL(pendingAvatarPreviewUrl.value)
    pendingAvatarPreviewUrl.value = ''
  }
}

// ================= 弹窗 2：粉丝与关注逻辑 =================
const isFollowVisible = ref(false)
const followDialogType = ref('followers')
const followList = ref([])
const followActionLoadingId = ref(null)

const followPage = reactive({
  page: 1,
  size: 10,
  total: 0
})

const fetchFollowList = async () => {
  try {
    const data = await request.get(`/follow/${followDialogType.value}`, {
      params: { page: followPage.page, size: followPage.size }
    })
    followList.value = data?.records || []
    followPage.total = data?.total || 0
  } catch (error) {
    followList.value = []
    followPage.total = 0
  }
}

const openFollowDialog = async (type) => {
  followDialogType.value = type
  followPage.page = 1
  isFollowVisible.value = true
  await fetchFollowList()
}

const toggleFollow = async (user) => {
  if (!user?.id) return
  try {
    followActionLoadingId.value = user.id
    if (user.isFollowing) {
      await request.delete(`/follow/${user.id}`)
      ElMessage.success('已取消关注')
    } else {
      await request.post(`/follow/${user.id}`)
      ElMessage.success('关注成功')
    }
    await fetchFollowList()
    fetchProfile()
  } catch (error) {
  } finally {
    followActionLoadingId.value = null
  }
}

// ================= 弹窗 3：打卡日历逻辑 =================
const isCalendarVisible = ref(false)
// 模拟有打卡记录的日期 (格式需与 Element Plus 的 data.day 匹配)
// 注意：这里填了几个 3 月份的日子，你可以点开日历看看 10号、12号、14号的绿点
const checkinDays = reactive([
  '2026-03-10',
  '2026-03-12',
  '2026-03-14',
  '2026-03-15'
])

// ================= 弹窗 4：全部徽章逻辑 =================
const isBadgeVisible = ref(false)
const badgeList = reactive([
  { id: 1, name: '初入训练场', icon: '🏃', desc: '完成首次 AI 动作识别', unlocked: true },
  { id: 2, name: '钢铁大腿', icon: '🦵', desc: '累计完成 1000 个标准深蹲', unlocked: true },
  { id: 3, name: '自律机器', icon: '🔥', desc: '连续打卡 7 天', unlocked: true },
  { id: 4, name: '早鸟修仙', icon: '🌅', desc: '在早上 6:00 前完成一次训练', unlocked: false },
  { id: 5, name: '夜行侠', icon: '🦉', desc: '在晚上 23:00 后完成一次训练', unlocked: false },
  { id: 6, name: '百发百中', icon: '🎯', desc: '单次训练 AI 评分达到 100 分', unlocked: false },
  { id: 7, name: '社交达人', icon: '💬', desc: '推文累计获得 100 个赞', unlocked: true },
  { id: 8, name: '核心撕裂者', icon: '🍫', desc: '解锁高级核心动作库', unlocked: false }
])

// ================= 底部内容区数据 =================
const activeTab = ref('posts')
const postList = reactive([{ id: 1, time: '2026-03-12 10:30', type: 'AI战报', content: '今天使用了 AiTrainer 的深蹲模式，AI 姿态评分高达 92 分！' }])

const footprintFilter = ref('liked')
const footprintPosts = reactive([])
const footprintPage = reactive({ page: 1, size: 10, total: 0 })

const fetchFootprints = async () => {
  const api = footprintFilter.value === 'commented' ? '/posts/me/commented' : '/posts/me/liked'
  const data = await request.get(api, { params: { page: footprintPage.page, size: footprintPage.size } })
  footprintPage.total = data?.total ?? 0
  footprintPosts.splice(0, footprintPosts.length, ...(data?.records || []))
}

onMounted(async () => {
  await fetchFootprints()
})

watchEffect(async () => {
  await fetchFootprints()
})

const collectionFolders = reactive([
  { id: 1, name: '腹肌撕裂干货', count: 12 },
  { id: 2, name: '养生壶减脂食谱', count: 8 },
  { id: 3, name: 'CV 算法论文收集', count: 5 }
])
</script>

<style scoped>
.profile-container { max-width: 1000px; margin: 0 auto; animation: fadeIn 0.5s ease; }
.profile-header-card { border-radius: 16px; background: linear-gradient(135deg, #ffffff 0%, #f8faff 100%); margin-bottom: 30px; }
.user-info-wrapper { display: flex; align-items: center; gap: 30px; padding: 10px; }
.avatar-section .el-avatar { border: 4px solid #fff; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
.info-section { flex: 1; }
.name-row { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.nickname { margin: 0; font-size: 24px; color: #303133; }
.edit-btn { margin-left: auto; border-radius: 20px; }
.bio { color: #606266; font-size: 14px; margin-top: 0; margin-bottom: 20px; }

/* 增强数据栏点击交互 */
.stats-row { display: flex; align-items: center; gap: 20px; }
.stat-item { display: flex; flex-direction: column; align-items: center; padding: 8px 16px; border-radius: 8px; transition: background-color 0.3s; }
.stat-item.clickable { cursor: pointer; }
.stat-item.clickable:hover { background-color: #f0f4f8; }
.stat-value { font-size: 20px; font-weight: bold; color: #303133; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; display: flex; align-items: center; gap: 4px; }
.stat-divider { width: 1px; height: 30px; background-color: #ebeef5; }

/* 徽章区样式 */
.section-title { font-size: 18px; font-weight: bold; color: #303133; margin-bottom: 16px; display: flex; align-items: center; justify-content: space-between; }
.badge-row { margin-bottom: 30px; }
.badge-card { text-align: center; border-radius: 12px; position: relative; cursor: pointer; border: 1px solid #f0f2f5; transition: transform 0.3s; }
.badge-card:hover { transform: translateY(-3px); box-shadow: 0 8px 16px rgba(0,0,0,0.05); }
.badge-card.is-locked { filter: grayscale(100%); opacity: 0.6; }
.badge-icon { font-size: 40px; margin-bottom: 10px; }
.badge-name { font-size: 14px; font-weight: bold; }
.lock-mask { position: absolute; top: 10px; right: 10px; color: #909399; font-size: 18px; }

/* 底部内容区 */
.content-card { border-radius: 16px; min-height: 400px; }
.custom-tabs :deep(.el-tabs__item) { font-size: 16px; font-weight: 500; }
.post-item { margin-bottom: 16px; border-radius: 8px; }
.post-header { display: flex; justify-content: space-between; margin-bottom: 12px; }
.post-time { font-size: 13px; color: #909399; }
.post-content { font-size: 15px; color: #303133; line-height: 1.6; }

/* 收藏夹样式 */
.folder-card { text-align: center; border-radius: 12px; padding: 20px 0; cursor: pointer; border: 1px dashed #dcdfe6; background-color: #fafafa; }
.folder-card:hover { border-color: #409eff; color: #409eff; }
.folder-icon { font-size: 36px; color: #c0c4cc; margin-bottom: 12px; }
.folder-card:hover .folder-icon { color: #409eff; }
.folder-name { font-size: 16px; font-weight: bold; color: #303133; margin-bottom: 6px; }
.folder-count { font-size: 12px; color: #909399; }

/* 关注/粉丝列表弹窗样式 */
.follow-list { display: flex; flex-direction: column; gap: 16px; }
.follow-item { display: flex; align-items: center; gap: 12px; padding-bottom: 12px; border-bottom: 1px solid #f0f2f5; }
.follow-item:last-child { border-bottom: none; }
.follow-info { flex: 1; }
.follow-name { font-size: 15px; font-weight: bold; color: #303133; margin-bottom: 4px; }
.follow-bio { font-size: 12px; color: #909399; }

/* 日历自定义样式 */
.custom-calendar :deep(.el-calendar-table .el-calendar-day) { height: 60px; padding: 4px; }
.calendar-cell { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative; }
.is-today { font-weight: bold; color: #409EFF; }
/* 渲染绿色的打卡圆点 */
.checkin-dot { width: 6px; height: 6px; background-color: #67C23A; border-radius: 50%; margin-top: 4px; box-shadow: 0 0 4px rgba(103, 194, 58, 0.5); }

/* ================= 头像上传组件样式 ================= */
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #dcdfe6;
  border-radius: 50%; /* 变成圆形 */
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  background-color: #fafafa;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.uploaded-avatar {
  width: 80px;
  height: 80px;
  display: block;
  object-fit: cover; /* 保证图片不变形 */
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  margin-top: 8px;
  width: 100%;
}

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>
