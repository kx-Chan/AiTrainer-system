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
              <el-radio-button label="all">全部浏览</el-radio-button>
              <el-radio-button label="liked">我赞过的</el-radio-button>
            </el-radio-group>
          </div>
          <el-timeline style="margin-top: 20px;">
            <el-timeline-item v-for="(activity, index) in footprintList" :key="index" :timestamp="activity.time" :type="activity.type">
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
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

    <el-dialog v-model="isEditVisible" title="编辑个人资料" width="450px" destroy-on-close class="edit-dialog">
      <el-form label-width="80px" :model="userInfo">
        <el-form-item label="用户头像">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
          >
            <img v-if="userInfo.avatar" :src="userInfo.avatar" class="uploaded-avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">点击头像可重新上传本地图片 (支持 jpg/png)</div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userInfo.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="健身目标">
          <el-radio-group v-model="userInfo.goal">
            <el-radio-button label="减脂">减脂</el-radio-button>
            <el-radio-button label="增肌">增肌</el-radio-button>
            <el-radio-button label="塑形">塑形</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身高 (cm)">
          <el-input-number v-model="userInfo.height" :min="100" :max="250" />
        </el-form-item>
        <el-form-item label="体重 (kg)">
          <el-input-number v-model="userInfo.weight" :min="30" :max="200" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="体脂率 (%)">
          <el-input-number v-model="userInfo.bodyFat" :min="1" :max="50" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="个性签名">
          <el-input 
            v-model="userInfo.bio" 
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
          <el-button @click="isEditVisible = false">取消</el-button>
          <el-button type="primary" @click="saveProfile">保存修改</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="isFollowVisible" :title="followDialogType === 'followers' ? '我的粉丝' : '我的关注'" width="400px">
      <div class="follow-list">
        <div v-for="user in currentFollowList" :key="user.id" class="follow-item">
          <el-avatar :size="40" :src="user.avatar" />
          <div class="follow-info">
            <div class="follow-name">{{ user.name }}</div>
            <div class="follow-bio">{{ user.bio }}</div>
          </div>
          <el-button :type="user.isFollowing ? 'default' : 'primary'" size="small" plain round>
            {{ user.isFollowing ? '已关注' : '+ 关注' }}
          </el-button>
        </div>
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
import { ref, reactive, computed } from 'vue'
import { Edit, Medal, Lock, FolderOpened, ArrowRight, Calendar, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus' 

// ================= 基础数据 =================
const userInfo = reactive({
  avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
  nickname: '陈同学_AiTrainer',
  goal: '减脂',
  bio: '生命在于折腾，代码与铁块齐飞。目标：本月体脂率降至 15%！',
  following: 24,
  followers: 128,
  totalDays: 45,
  // ====== 新增基础体征字段 ======
  height: 165,
  weight: 65.5,
  bodyFat: 18.5
})

const isEditVisible = ref(false)
const openEditModal = () => { isEditVisible.value = true }
// ================= 头像上传与本地预览逻辑 =================
const handleAvatarChange = (uploadFile) => {
  // 1. 简单的格式校验
  if (!uploadFile.raw.type.startsWith('image/')) {
    ElMessage.error('头像只能是图片格式!')
    return
  }
  
  // 2. 核心黑科技：利用浏览器原生 API 生成本地临时预览链接，无需经过后端！
  userInfo.avatar = URL.createObjectURL(uploadFile.raw)
  
  // 💡 面试吹点：在这里预留云端上传的钩子
  // 后期对接 Java 后端时，在这里构造 FormData，调用 axios.post('/api/upload') 传给后端
  // 拿到 OSS 返回的真实 URL 后，再最终赋给 userInfo.avatar
}
const saveProfile = () => {
  isEditVisible.value = false
  ElMessage.success('个人资料已保存！')
}

// ================= 弹窗 2：粉丝与关注逻辑 =================
const isFollowVisible = ref(false)
const followDialogType = ref('followers') // 'followers' 或 'following'

const openFollowDialog = (type) => {
  followDialogType.value = type
  isFollowVisible.value = true
}

// 模拟的粉丝/关注列表数据
const mockFollowers = reactive([
  { id: 1, name: '林教练 (深大荔园)', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', bio: '深蹲狂热爱好者', isFollowing: true },
  { id: 2, name: '健身萌新_小李', avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png', bio: '正在努力减脂中...', isFollowing: false }
])

const mockFollowing = reactive([
  { id: 1, name: '林教练 (深大荔园)', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', bio: '深蹲狂热爱好者', isFollowing: true }
])

const currentFollowList = computed(() => {
  return followDialogType.value === 'followers' ? mockFollowers : mockFollowing
})

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

const footprintFilter = ref('all')
const footprintList = reactive([
  { time: '今天 14:20', content: '点赞了 @林教练 的推文《新手如何避免深蹲膝盖内扣》', type: 'success' },
  { time: '昨天 21:00', content: '浏览了话题 #宿舍减脂餐', type: 'info' }
])

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