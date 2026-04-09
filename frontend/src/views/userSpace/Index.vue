<template>
  <div class="user-space-container">
    <div class="user-header-banner">
      <div class="banner-title">{{ (userInfo.nickname || '用户') + ' 的个人空间' }}</div>
    </div>

    <el-card class="user-info-card" shadow="never">
      <div class="info-layout">
        <el-avatar :size="100" :src="userInfo.avatar" class="user-main-avatar" />
        <div class="user-details">
          <div class="name-row">
            <h2 class="user-name">{{ userInfo.nickname || '用户' }}</h2>
            <el-tag v-if="userInfo.goal" type="warning" effect="light" round size="small">目标: {{ userInfo.goal }}</el-tag>
            <el-tag v-if="userInfo.isPro" type="warning" size="small" effect="dark" round>PRO</el-tag>

            <div class="action-btns">
              <template v-if="isMe">
                <el-button round plain @click="editProfile">编辑资料</el-button>
                <el-button circle icon="Setting" @click="router.push('/settings')" />
              </template>
              <template v-else>
                <el-button :type="userInfo.isFollowing ? 'info' : 'primary'" round :loading="followLoading"
                  :disabled="followLoading" @click="handleFollow">
                  {{ userInfo.isFollowing ? '已关注' : '+ 关注' }}
                </el-button>
                <el-button round plain @click="scrollToGuestbook">留言</el-button>
              </template>
            </div>
          </div>

          <p class="user-bio">{{ userInfo.bio || '这个健身达人很懒，什么都没写~' }}</p>

          <div class="stats-row">
            <div class="stat-item"><strong>{{ followingCount }}</strong> 关注</div>
            <div class="stat-item"><strong>{{ followerCount }}</strong> 粉丝</div>
            <div class="stat-item"><strong>{{ totalLikes }}</strong> 获赞</div>
          </div>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="user-tabs">
        <el-tab-pane label="动态" name="posts">
          <div class="post-feed">
            <div class="dynamics-toolbar">
              <el-radio-group v-model="dynamicsCategory" size="small">
                <el-radio-button value="all">全部</el-radio-button>
                <el-radio-button value="workout_report">训练记录</el-radio-button>
                <el-radio-button value="post">推文</el-radio-button>
              </el-radio-group>
            </div>

            <div v-loading="dynamicsLoading">
              <el-alert v-if="!isMe && !userVisibility.canViewAiWorkoutReport" type="info" show-icon :closable="false"
                title="对方已关闭 AI 战报公开展示" style="margin-bottom: 12px;" />

              <div v-for="item in dynamicsList" :key="item.id" class="user-post-item">
                <PostItem v-if="item?.post && (item.type !== 'workout_report' || userVisibility.canViewAiWorkoutReport)"
                  :post="item.post" :viewer-user-id="userStore.userId" :viewer-avatar="userStore.avatar"
                  :follow-loading-id="followLoadingId" @go-to-space="goToSpace" @follow="handleFollowFromPostItem"
                  @unfollow="handleUnfollowFromPostItem" @toggle-like="toggleLike" @comment-added="handleCommentAdded"
                  @comment-deleted="handleCommentDeleted" />
              </div>

              <el-empty v-if="dynamicsList.length === 0" :description="dynamicsEmptyText" />
            </div>

            <div v-if="dynamicsPage.total > dynamicsPage.size" class="pagination-container">
              <el-pagination v-model:current-page="dynamicsPage.page" :page-size="dynamicsPage.size"
                :total="dynamicsPage.total" layout="prev, pager, next" background @current-change="fetchDynamics" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="收藏夹" name="favorites">
          <el-empty v-if="visibleFavoriteFolders.length === 0" :description="isMe ? '暂无收藏夹' : '暂无公开收藏夹'" />
          <div v-else class="favorites-grid">
            <div v-for="folder in visibleFavoriteFolders" :key="folder.id" class="fav-folder-card" @click="goToFolderDetail(folder)">
              <el-icon size="40">
                <FolderOpened />
              </el-icon>
              <span class="folder-name">{{ folder.name }}</span>

              <el-tooltip v-if="isMe && Number(folder.isPublic) !== 1" content="仅自己可见" placement="top">
                <el-icon class="lock-icon">
                  <Lock />
                </el-icon>
              </el-tooltip>

              <div v-if="isMe" class="folder-visibility-tag">
                <el-tag :type="Number(folder.isPublic) === 1 ? 'success' : 'info'" size="small" effect="plain" round>
                  {{ Number(folder.isPublic) === 1 ? '公开' : '私密' }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="留言板" name="guestbook">
          <div v-if="isMe" class="guestbook-sub-nav">
            <el-radio-group v-model="guestbookMode" size="small">
              <el-radio-button value="received">收到的留言</el-radio-button>
              <el-radio-button value="sent">发出的留言</el-radio-button>
            </el-radio-group>
          </div>

          <div v-if="!isMe" class="message-input-area">
            <el-input v-model="newGuestbookContent" type="textarea" :rows="3" placeholder="给空间主人留言..." maxlength="200"
              show-word-limit />
            <div class="input-actions">
              <el-button type="primary" size="small" round :disabled="!newGuestbookContent.trim()"
                @click="submitGuestbook">发布留言</el-button>
            </div>
          </div>

          <div v-loading="guestbookLoading" class="message-list">
            <el-empty v-if="guestbookList.length === 0" description="暂无留言" />

            <div v-for="msg in guestbookList" :key="msg.id" class="message-card">
              <div class="message-main">
                <el-avatar v-if="guestbookMode === 'received'" :size="40" :src="msg.fromUserAvatar"
                  @click="goToSpace(msg.fromUserId)" style="cursor:pointer" />
                <el-avatar v-else :size="40" :src="userInfo.avatar" />
                <div class="message-body">
                  <div class="message-info">
                    <span class="user-name">{{ guestbookMode === 'received' ? (msg.fromUserName || '用户') : ('留言给：' + (msg.toUserName || '用户')) }}</span>
                    <span class="message-time">#{{ msg.id }} · {{ msg.createTime?.substring(0, 10) }}</span>
                  </div>
                  <p class="message-text">{{ msg.content }}</p>

                  <div v-if="msg.replyContent" class="owner-reply-box">
                    <span class="reply-label">主人回复：</span>
                    <span class="reply-text">{{ msg.replyContent }}</span>
                    <div class="reply-time">{{ msg.replyTime?.substring(0, 10) }}</div>
                  </div>

                  <div v-if="canDeleteMessage(msg) || canDeleteReply(msg)" class="message-actions">
                    <el-button v-if="canDeleteMessage(msg)" type="danger" link size="small"
                      @click="deleteMessage(msg)">删除留言</el-button>
                    <el-button v-if="canDeleteReply(msg)" type="danger" link size="small"
                      @click="deleteReply(msg)">删除回复</el-button>
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

                <el-avatar v-if="guestbookMode === 'sent'" :size="40" :src="msg.toUserAvatar"
                  @click="goToSpace(msg.toUserId)" style="cursor:pointer" />
              </div>
            </div>
          </div>

          <div v-if="guestbookPager.total > guestbookPager.size" class="pagination-container">
            <el-pagination v-model:current-page="guestbookPager.page" :page-size="guestbookPager.size"
              :total="guestbookPager.total" layout="prev, pager, next" background @current-change="fetchGuestbook" />
          </div>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/userStore'
import { FolderOpened, Lock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { followApi, guestbookApi, userSpaceApi, dynamicsApi, privacyApi } from '@/api/userSpace'
import PostItem from '@/components/community/PostItem.vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const targetUserId = computed(() => route.params.id)
const isMe = computed(() => userStore.userId === Number(targetUserId.value))


const activeTab = ref('posts')
const userInfo = ref({})
const dynamicsList = ref([])
const dynamicsLoading = ref(false)
const dynamicsPage = reactive({ page: 1, size: 10, total: 0 })
const dynamicsCategory = ref('all') // all | workout_report | post
const userVisibility = reactive({ canViewAiWorkoutReport: true })
const favoriteFolders = ref([])
const followLoading = ref(false)

const followingCount = computed(() => userInfo.value?.followingCount ?? userInfo.value?.following ?? 0)
const followerCount = computed(() => userInfo.value?.followerCount ?? userInfo.value?.followers ?? 0)
const totalLikes = computed(() => userInfo.value?.totalLikes ?? 0)

const visibleFavoriteFolders = computed(() => {
  const list = Array.isArray(favoriteFolders.value) ? favoriteFolders.value : []
  if (isMe.value) return list
  return list.filter(f => Number(f?.isPublic) === 1)
})

const extractList = (res) => {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.records)) return res.records
  if (Array.isArray(res?.list)) return res.list
  if (Array.isArray(res?.data)) return res.data
  return []
}

const normalizeFolder = (folder) => {
  const id = folder?.id ?? folder?.folderId ?? folder?.collectionId ?? folder?.favoriteFolderId
  const name = folder?.name ?? folder?.folderName ?? folder?.title ?? folder?.folderTitle
  return { ...folder, id, name }
}

const normalizeDynamicsItem = (item) => {
  if (!item) return item
  const dateOnly = (value) => {
    const s = String(value ?? '').trim()
    if (!s) return ''
    return s.length >= 10 ? s.slice(0, 10) : s
  }
  const type = String(item?.type || '').trim()
  if (type === 'workout_report') {
    const aiReport = item?.aiReport ?? item?.payload ?? item?.report ?? null
    const reportId = item?.id ?? item?.dynamicId ?? item?.workoutReportId ?? aiReport?.id
    const ownerId = userInfo.value?.id ?? userInfo.value?.userId ?? Number(targetUserId.value || 0)
    const ownerIsFollowing = Boolean(userInfo.value?.isFollowing)
    const createTime = item?.createTime ?? item?.createdAt ?? item?.time ?? aiReport?.createdAt
    return {
      ...item,
      id: reportId,
      createTime,
      post: {
        id: reportId,
        author: userInfo.value?.nickname || '用户',
        avatar: userInfo.value?.avatar,
        authorId: ownerId,
        device: 'AiTrainer',
        content: item?.title ?? item?.content ?? '训练结束 AI 战报',
        images: item?.images ?? [],
        aiReport: aiReport || null,
        isPro: Boolean(userInfo.value?.isPro),
        time: dateOnly(createTime),
        sourceType: 'workout_report',
        isFollowing: ownerIsFollowing,
        likes: Number(aiReport?.likes ?? 0),
        isLiked: Boolean(aiReport?.liked)
      }
    }
  }
  if (type === 'post') {
    const post = item?.post ?? item?.payload ?? item
    const createTime = item?.createTime ?? post?.createTime ?? post?.time
    const ownerIsFollowing = Boolean(userInfo.value?.isFollowing)
    return {
      ...item,
      id: item?.id ?? post?.id,
      createTime,
      post: post ? { ...post, time: dateOnly(post?.time ?? createTime), isFollowing: ownerIsFollowing } : post
    }
  }
  return {
    ...item,
    id: item?.id ?? item?.dynamicId,
    createTime: item?.createTime ?? item?.createdAt
  }
}

// 获取用户信息
const fetchUserInfo = async () => {
  const id = String(targetUserId.value || '').trim()
  if (!id) return
  const data = isMe.value ? await userSpaceApi.getMyProfile() : await userSpaceApi.getUserProfile(id)
  userInfo.value = data || {}
}

const fetchUserVisibility = async () => {
  const id = String(targetUserId.value || '').trim()
  if (!id) return
  if (isMe.value) {
    userVisibility.canViewAiWorkoutReport = true
    return
  }
  try {
    const data = await privacyApi.getUserVisibility(id)
    userVisibility.canViewAiWorkoutReport = Boolean(data?.canViewAiWorkoutReport ?? true)
  } catch (e) {
    userVisibility.canViewAiWorkoutReport = true
  }
}

const fetchDynamics = async () => {
  const id = String(targetUserId.value || '').trim()
  if (!id) return
  if (!isMe.value && !userVisibility.canViewAiWorkoutReport && dynamicsCategory.value === 'workout_report') {
    dynamicsList.value = []
    dynamicsPage.total = 0
    return
  }
  dynamicsLoading.value = true
  try {
    const data = await dynamicsApi.listUserDynamics(id, {
      category: dynamicsCategory.value === 'all' ? undefined : dynamicsCategory.value,
      page: dynamicsPage.page,
      size: dynamicsPage.size
    })
    dynamicsPage.total = Number(data?.total || 0)
    const records = Array.isArray(data?.records) ? data.records : []
    dynamicsList.value = records.map(normalizeDynamicsItem).filter(Boolean).filter(canShowItem)
    dynamicsList.value.forEach((it) => {
      if (!it?.post) return
      it.post.time = String(it.post.time || '').slice(0, 10)
      it.post.isFollowing = Boolean(userInfo.value?.isFollowing)
    })
  } finally {
    dynamicsLoading.value = false
  }
}

const resetDynamicsState = () => {
  dynamicsList.value = []
  dynamicsLoading.value = false
  dynamicsPage.page = 1
  dynamicsPage.total = 0
  dynamicsCategory.value = 'all'
}

const canShowItem = (item) => {
  if (!item) return false
  if (String(item?.type || '').trim() !== 'workout_report') return true
  return isMe.value || userVisibility.canViewAiWorkoutReport
}

const followLoadingId = computed(() => {
  if (!followLoading.value) return null
  const id = Number(targetUserId.value || 0)
  return id || null
})

const handleFollowFromPostItem = async (post) => {
  const authorId = Number(post?.authorId || 0)
  const targetId = Number(targetUserId.value || 0)
  if (!authorId || !targetId) return
  if (authorId !== targetId) return
  if (userInfo.value.isFollowing) return
  await handleFollow()
  dynamicsList.value.forEach((it) => {
    if (!it?.post) return
    it.post.isFollowing = Boolean(userInfo.value?.isFollowing)
  })
}

const handleUnfollowFromPostItem = async (post) => {
  const authorId = Number(post?.authorId || 0)
  const targetId = Number(targetUserId.value || 0)
  if (!authorId || !targetId) return
  if (authorId !== targetId) return
  if (!userInfo.value.isFollowing) return
  await handleFollow()
  dynamicsList.value.forEach((it) => {
    if (!it?.post) return
    it.post.isFollowing = Boolean(userInfo.value?.isFollowing)
  })
}

const dynamicsEmptyText = computed(() => {
  if (!isMe.value && !userVisibility.canViewAiWorkoutReport && dynamicsCategory.value === 'workout_report') {
    return '训练记录已设为私密'
  }
  if (dynamicsCategory.value === 'workout_report') return '暂无训练记录'
  if (dynamicsCategory.value === 'post') return '暂无推文'
  return '暂无动态'
})

const toggleLike = async (post) => {
  if (!post?.id) return
  const isReportOnly = String(post?.sourceType || '') === 'workout_report'
  if (isReportOnly) {
    const beforeLiked = Boolean(post.isLiked)
    const data = beforeLiked
      ? await request.delete(`/reports/${post.id}/like`)
      : await request.post(`/reports/${post.id}/like`)
    post.isLiked = data?.liked ?? !beforeLiked
    post.likes = data?.likes ?? Math.max(0, Number(post.likes || 0) + (beforeLiked ? -1 : 1))
    const delta = beforeLiked ? -1 : 1
    userInfo.value.totalLikes = Math.max(0, Number(userInfo.value?.totalLikes || 0) + delta)
    return
  }
  if (post.isLiked) {
    const data = await request.delete(`/posts/${post.id}/like`)
    post.isLiked = data?.liked ?? false
    post.likes = data?.likes ?? Math.max(0, Number(post.likes || 0) - 1)
    return
  }
  const data = await request.post(`/posts/${post.id}/like`)
  post.isLiked = data?.liked ?? true
  post.likes = data?.likes ?? Number(post.likes || 0) + 1
}

const handleCommentAdded = (postId) => {
  dynamicsList.value.forEach((item) => {
    if (Number(item?.post?.id) !== Number(postId)) return
    item.post.comments = Number(item.post.comments || 0) + 1
  })
}

const handleCommentDeleted = (postId) => {
  dynamicsList.value.forEach((item) => {
    if (Number(item?.post?.id) !== Number(postId)) return
    item.post.comments = Math.max(0, Number(item.post.comments || 0) - 1)
  })
}

const fetchFavoriteFolders = async () => {
  const id = String(targetUserId.value || '').trim()
  if (!id) return
  if (isMe.value) {
    const res = await userSpaceApi.listMyFolders()
    favoriteFolders.value = extractList(res).map(normalizeFolder).filter(f => !!f?.id)
    return
  }
  const res = await userSpaceApi.listUserPublicFolders(id)
  favoriteFolders.value = extractList(res).map(normalizeFolder).filter(f => !!f?.id)
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
    const wasFollowing = Boolean(userInfo.value.isFollowing)
    if (wasFollowing) {
      await followApi.unfollow(targetUserId.value)
      userInfo.value.isFollowing = false
      if (userInfo.value.followerCount != null) userInfo.value.followerCount = Math.max(0, Number(userInfo.value.followerCount) - 1)
      if (userInfo.value.followers != null) userInfo.value.followers = Math.max(0, Number(userInfo.value.followers) - 1)
      ElMessage.success('已取消关注')
    } else {
      await followApi.follow(targetUserId.value)
      userInfo.value.isFollowing = true
      if (userInfo.value.followerCount != null) userInfo.value.followerCount = Number(userInfo.value.followerCount) + 1
      if (userInfo.value.followers != null) userInfo.value.followers = Number(userInfo.value.followers) + 1
      ElMessage.success('关注成功')
    }
  } finally {
    followLoading.value = false
    dynamicsList.value.forEach((it) => {
      if (!it?.post) return
      it.post.isFollowing = Boolean(userInfo.value?.isFollowing)
    })
  }
}

// --- 留言板相关变量 ---
const guestbookMode = ref('received') // received or sent
const guestbookList = ref([])
const newGuestbookContent = ref('')
const activeReplyId = ref(null) // 当前正在回复哪条记录
const currentReplyText = ref('')
const guestbookLoading = ref(false)
const guestbookPageState = reactive({
  received: { page: 1, size: 10, total: 0 },
  sent: { page: 1, size: 10, total: 0 }
})
const guestbookPager = computed(() => {
  return guestbookMode.value === 'received' ? guestbookPageState.received : guestbookPageState.sent
})
const profileCache = new Map()

const resetGuestbookState = () => {
  guestbookList.value = []
  guestbookLoading.value = false
  guestbookPageState.received.page = 1
  guestbookPageState.received.total = 0
  guestbookPageState.sent.page = 1
  guestbookPageState.sent.total = 0
  activeReplyId.value = null
  currentReplyText.value = ''
  newGuestbookContent.value = ''
  if (!isMe.value && guestbookMode.value !== 'received') {
    guestbookMode.value = 'received'
  }
}

const goToSpace = (userId) => {
  if (!userId) return
  router.push({ name: 'UserSpace', params: { id: String(userId) } })
}

const goToFolderDetail = (folder) => {
  if (!folder?.id) return
  router.push({ name: 'CollectionDetail', params: { id: String(folder.id) }, query: { from: 'userSpace', userId: String(targetUserId.value || '') } })
}

const ensureProfiles = async (userIds) => {
  const ids = Array.from(new Set((userIds || []).map(v => String(v || '').trim()).filter(Boolean)))
  const missing = ids.filter(id => !profileCache.has(id))
  if (missing.length === 0) return
  await Promise.all(missing.map(async (id) => {
    try {
      const data = await userSpaceApi.getUserProfile(id)
      profileCache.set(id, data || {})
    } catch (e) {
      profileCache.set(id, {})
    }
  }))
}

// 获取留言列表
const fetchGuestbook = async () => {
  // 根据模式调用不同的后端 API
  const id = String(targetUserId.value || '').trim()
  if (!id) return
  guestbookLoading.value = true
  try {
    const pager = guestbookPager.value
    const data = guestbookMode.value === 'received'
      ? await guestbookApi.listReceived(id, { page: pager.page, size: pager.size })
      : await guestbookApi.listSent({ page: pager.page, size: pager.size })
    const records = Array.isArray(data?.records) ? data.records : []
    pager.total = Number(data?.total || 0)
    if (guestbookMode.value === 'sent') {
      await ensureProfiles(records.map(r => r?.toUserId).filter(Boolean))
    }
    if (guestbookMode.value === 'received') {
      await ensureProfiles(records.map(r => r?.fromUserId).filter(Boolean))
    }
    guestbookList.value = records.map((msg) => {
      const toProfile = profileCache.get(String(msg?.toUserId || '')) || {}
      const fromProfile = profileCache.get(String(msg?.fromUserId || '')) || {}
      return {
        ...msg,
        toUserName: msg?.toUserName ?? toProfile?.nickname ?? '',
        toUserAvatar: msg?.toUserAvatar ?? toProfile?.avatar ?? '',
        fromUserName: msg?.fromUserName ?? fromProfile?.nickname ?? '',
        fromUserAvatar: msg?.fromUserAvatar ?? fromProfile?.avatar ?? ''
      }
    })
  } finally {
    guestbookLoading.value = false
  }
}

// 提交新留言
const submitGuestbook = async () => {
  await guestbookApi.add({
    toUserId: targetUserId.value,
    content: newGuestbookContent.value
  })
  ElMessage.success('留言成功')
  newGuestbookContent.value = ''
  guestbookPageState.received.page = 1
  fetchGuestbook()
}

// 主人回复留言
const submitReply = async (msg) => {
  await guestbookApi.reply(msg.id, {
    replyContent: currentReplyText.value
  })
  ElMessage.success('已回复')
  activeReplyId.value = null
  currentReplyText.value = ''
  fetchGuestbook()
}

const canDeleteMessage = (msg) => {
  const myId = Number(userStore.userId || 0)
  if (!myId) return false
  return Number(msg?.fromUserId) === myId
}

const canDeleteReply = (msg) => {
  if (!isMe.value) return false
  if (guestbookMode.value !== 'received') return false
  return Boolean(msg?.replyContent)
}

const deleteMessage = async (msg) => {
  await ElMessageBox.confirm('确定要删除这条留言吗？', '删除确认', { type: 'warning' })
  await guestbookApi.remove(msg.id)
  ElMessage.success('已删除')
  fetchGuestbook()
}

const deleteReply = async (msg) => {
  await ElMessageBox.confirm('确定要删除这条回复吗？', '删除确认', { type: 'warning' })
  await guestbookApi.removeReply(msg.id)
  ElMessage.success('已删除回复')
  fetchGuestbook()
}

// 修改 watch 逻辑，增加对 Tab 切换的监听
watch(activeTab, (newTab) => {
  if (newTab === 'guestbook') {
    fetchGuestbook()
  }
  if (newTab === 'posts') {
    fetchDynamics()
  }
})

watch(guestbookMode, () => {
  guestbookPager.value.page = 1
  fetchGuestbook()
})

watch(dynamicsCategory, () => {
  dynamicsPage.page = 1
  fetchDynamics()
})

watch(isMe, (next) => {
  if (!next && guestbookMode.value !== 'received') {
    guestbookMode.value = 'received'
  }
})

watch(targetUserId, () => {
  fetchUserInfo()
  fetchUserVisibility()
  fetchFavoriteFolders()
  resetDynamicsState()
  resetGuestbookState()
  if (activeTab.value === 'guestbook') fetchGuestbook()
  if (activeTab.value === 'posts') fetchDynamics()
}, { immediate: true })

watch(() => route.query?.tab, (tab) => {
  const next = String(tab || '').trim()
  if (next && next !== activeTab.value) activeTab.value = next
}, { immediate: true })

watch(activeTab, (tab) => {
  const current = String(route.query?.tab || '')
  if (current === String(tab || '')) return
  router.replace({
    name: 'UserSpace',
    params: { id: String(targetUserId.value || '') },
    query: { ...(route.query || {}), tab: String(tab || '') }
  })
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
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-title {
  font-size: 22px;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.92);
  letter-spacing: 0.5px;
  text-shadow: 0 6px 18px rgba(0, 0, 0, 0.18);
  padding: 0 16px;
  text-align: center;
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
  align-items: center;
}

.user-main-avatar {
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-details {
  flex: 1;
  padding-top: 6px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  row-gap: 8px;
}

.action-btns {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
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

.dynamics-toolbar {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 14px;
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

.folder-visibility-tag {
  position: absolute;
  left: 10px;
  bottom: 10px;
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
  0% {
    box-shadow: 0 0 5px rgba(64, 158, 255, 0.2);
  }

  50% {
    box-shadow: 0 0 20px rgba(64, 158, 255, 0.5);
  }

  100% {
    box-shadow: 0 0 5px rgba(64, 158, 255, 0.2);
  }
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

.message-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
