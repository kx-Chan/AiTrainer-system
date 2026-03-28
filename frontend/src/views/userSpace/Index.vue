<template>
  <div class="user-space-container">
    <div class="user-header-banner"></div>

    <el-card class="user-info-card" shadow="never">
      <div class="info-layout">
        <el-avatar :size="100" :src="userInfo.avatar" class="user-main-avatar" />
        <div class="user-details">
          <div class="name-row">
            <h2 class="user-name">{{ userInfo.nickname }}</h2>
            <el-tag v-if="userInfo.isPro" type="warning" size="small" effect="dark" round>PRO</el-tag>
            
            <div class="action-btns">
                <template v-if="isMe">
                    <el-button round plain @click="editProfile">编辑资料</el-button>
                    <el-button circle icon="Setting" @click="router.push('/settings')" />
                </template>
                <template v-else>
                    <el-button 
                    :type="userInfo.isFollowing ? 'info' : 'primary'" 
                    round 
                    @click="handleFollow"
                    >
                    {{ userInfo.isFollowing ? '已关注' : '+ 关注' }}
                    </el-button>
                    <el-button round plain @click="activeTab = 'scrollToGuestbook'">留言</el-button>
                </template>
            </div>
          </div>
          
          <p class="user-bio">{{ userInfo.bio || '这个健身达人很懒，什么都没写~' }}</p>
          
          <div class="stats-row">
            <div class="stat-item"><strong>{{ userInfo.followingCount }}</strong> 关注</div>
            <div class="stat-item"><strong>{{ userInfo.followerCount }}</strong> 粉丝</div>
            <div class="stat-item"><strong>{{ userInfo.totalLikes }}</strong> 获赞</div>
          </div>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="user-tabs">
        <el-tab-pane label="动态" name="posts">
          <div class="post-feed">
             <div v-for="post in userPosts" :key="post.id" class="user-post-item">
                </div>
             <el-empty v-if="userPosts.length === 0" description="暂无动态" />
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="收藏夹" name="favorites">
          <div class="favorites-grid">
            <div 
                v-for="folder in favoriteFolders.filter(f => isMe || f.isPublic)" 
                :key="folder.id" 
                class="fav-folder-card"
            >
                <el-icon size="40"><FolderOpened /></el-icon>
                <span class="folder-name">{{ folder.name }}</span>
                
                <el-tooltip v-if="!folder.isPublic" content="仅自己可见" placement="top">
                <el-icon class="lock-icon"><Lock /></el-icon>
                </el-tooltip>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="留言板" name="guestbook">
            <div v-if="isMe" class="guestbook-sub-nav">
                <el-radio-group v-model="guestbookMode" size="small" @change="fetchGuestbook">
                <el-radio-button label="received">收到的留言</el-radio-button>
                <el-radio-button label="sent">发出的留言</el-radio-button>
                </el-radio-group>
            </div>

            <div v-if="!isMe" class="message-input-area">
                <el-input
                    v-model="newGuestbookContent"
                    type="textarea"
                    :rows="3"
                    placeholder="给空间主人留言..." maxlength="200"
                    show-word-limit
                />
                <div class="input-actions">
                    <el-button 
                    type="primary" 
                    size="small" 
                    round 
                    :disabled="!newGuestbookContent.trim()" 
                    @click="submitGuestbook"
                    >发布留言</el-button>
                </div>
            </div>

            <div class="message-list">
                <el-empty v-if="guestbookList.length === 0" description="暂无留言" />
                
                <div v-for="msg in guestbookList" :key="msg.id" class="message-card">
                    <div class="message-main">
                    <el-avatar :size="40" :src="guestbookMode === 'received' ? msg.fromUserAvatar : msg.toUserAvatar" @click="goToSpace(guestbookMode === 'received' ? msg.fromUserId : msg.toUserId)" style="cursor:pointer" />
                    <div class="message-body">
                    <div class="message-info">
                        <span class="user-name">{{ guestbookMode === 'received' ? msg.fromUserName : msg.toUserName }}</span>
                        <span class="message-time">{{ msg.createTime?.substring(0, 10) }}</span>
                    </div>
                    <p class="message-text">{{ msg.content }}</p>
                    
                    <div v-if="msg.replyContent" class="owner-reply-box">
                        <span class="reply-label">主人回复：</span>
                        <span class="reply-text">{{ msg.replyContent }}</span>
                        <div class="reply-time">{{ msg.replyTime?.substring(0, 10) }}</div>
                    </div>

                    <div v-if="isMe && guestbookMode === 'received' && !msg.replyContent" class="reply-action">
                        <div v-if="activeReplyId === msg.id" class="reply-input-inline">
                        <el-input v-model="currentReplyText" size="small" placeholder="回复留言..." />
                        <el-button type="primary" size="small" link @click="submitReply(msg)">确认</el-button>
                        <el-button size="small" link @click="activeReplyId = null">取消</el-button>
                        </div>
                        <el-button v-else type="primary" link size="small" @click="activeReplyId = msg.id">回复</el-button>
                    </div>
                    </div>
                    </div>
                </div>
            </div>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/userStore'
import { FolderOpened, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const targetUserId = computed(() => route.params.id)
const isMe = computed(() => userStore.userId === Number(targetUserId.value))


const activeTab = ref('posts')
const userInfo = ref({})
const userPosts = ref([])
const favoriteFolders = ref([])
const followLoading = ref(false)

// 获取用户信息
const fetchUserInfo = async () => {
  const data = await request.get(`/user/${targetUserId.value}/profile`)
  userInfo.value = data
}

const editProfile = () => {
  router.push('/profile')
}

// 在 script setup 中添加一个简单的跳转函数
const scrollToGuestbook = () => {
  activeTab.value = 'guestbook'
  // 细节：平滑滚动到 Tab 区域，让留言框直接出现在视口
  setTimeout(() => {
    const el = document.querySelector('.user-tabs')
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }, 100) // 给 Tab 渲染留一点时间
}

const handleFollow = async () => {
  followLoading.value = true
  try {
    if (userInfo.value.isFollowing) {
      await request.delete(`/follow/${targetUserId.value}`)
      userInfo.value.isFollowing = false
    } else {
      await request.post(`/follow/${targetUserId.value}`)
      userInfo.value.isFollowing = true
    }
  } finally {
    followLoading.value = false
  }
}

watch(targetUserId, () => {
  fetchUserInfo()
}, { immediate: true })

// --- 留言板相关变量 ---
const guestbookMode = ref('received') // received or sent
const guestbookList = ref([])
const newGuestbookContent = ref('')
const activeReplyId = ref(null) // 当前正在回复哪条记录
const currentReplyText = ref('')

// 获取留言列表
const fetchGuestbook = async () => {
  // 根据模式调用不同的后端 API
  const endpoint = guestbookMode.value === 'received' 
    ? `/guestbook/received/${targetUserId.value}` 
    : `/guestbook/sent`
    
  const data = await request.get(endpoint)
  guestbookList.value = data || []
}

// 提交新留言
const submitGuestbook = async () => {
  await request.post('/guestbook', {
    toUserId: targetUserId.value,
    content: newGuestbookContent.value
  })
  ElMessage.success('留言成功')
  newGuestbookContent.value = ''
  fetchGuestbook()
}

// 主人回复留言
const submitReply = async (msg) => {
  await request.put(`/guestbook/reply/${msg.id}`, {
    replyContent: currentReplyText.value
  })
  ElMessage.success('已回复')
  activeReplyId.value = null
  currentReplyText.value = ''
  fetchGuestbook()
}

// 修改 watch 逻辑，增加对 Tab 切换的监听
watch(activeTab, (newTab) => {
  if (newTab === 'guestbook') {
    fetchGuestbook()
  }
})
</script>

<style scoped>
.user-space-container {
  max-width: 900px;
  margin: 0 auto;
}
.user-header-banner {
  height: 180px;
  background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
  border-radius: 12px 12px 0 0;
}
.user-info-card {
  margin-top: -50px;
  border-radius: 12px;
  padding: 20px;
}
.info-layout {
  display: flex;
  gap: 24px;
  margin-bottom: 30px;
}
.user-main-avatar {
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.user-details {
  flex: 1;
}
.name-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.action-btns {
  margin-left: auto;
}
.stats-row {
  display: flex;
  gap: 20px;
  margin-top: 16px;
}
.stat-item {
  color: #606266;
  font-size: 14px;
}
.stat-item strong {
  color: #303133;
  font-size: 18px;
}
.user-tabs {
  margin-top: 20px;
}
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
  padding: 20px 0;
}
.fav-folder-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
}
.lock-icon {
  position: absolute;
  top: 8px;
  right: 8px;
  color: #909399;
}

/* 如果是本人看，头像边缘加一个呼吸灯效果，暗示“这是我” */
.is-me-avatar {
  border: 4px solid #409EFF !important;
  animation: breathe 3s infinite ease-in-out;
}

@keyframes breathe {
  0% { box-shadow: 0 0 5px rgba(64, 158, 255, 0.2); }
  50% { box-shadow: 0 0 20px rgba(64, 158, 255, 0.5); }
  100% { box-shadow: 0 0 5px rgba(64, 158, 255, 0.2); }
}

/* 留言板子导航 */
.guestbook-sub-nav {
  margin-bottom: 20px;
  text-align: center;
}

/* 留言输入区 */
.message-input-area {
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 24px;
}
.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

/* 留言卡片 */
.message-card {
  padding: 20px 0;
  border-bottom: 1px solid #f0f2f5;
}
.message-main {
  display: flex;
  gap: 16px;
}
.message-body {
  flex: 1;
}
.message-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.user-name {
  font-weight: bold;
  color: #303133;
}
.message-time {
  font-size: 12px;
  color: #909399;
}
.message-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

/* 主人回复框样式：精致的引用感 */
.owner-reply-box {
  margin-top: 12px;
  padding: 12px;
  background: #f4f4f5;
  border-left: 4px solid #909399;
  border-radius: 4px;
}
.reply-label {
  font-size: 13px;
  font-weight: bold;
  color: #409EFF;
}
.reply-text {
  font-size: 13px;
  color: #606266;
}
.reply-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}

/* 内联回复输入 */
.reply-input-inline {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}
</style>
