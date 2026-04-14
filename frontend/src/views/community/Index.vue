<template>
  <div class="community-container">
    <el-row :gutter="24">
      <el-col :span="16">
        <Publisher ref="publisherRef" :recommended-topics="recommendedTopicNames" @published="handlePublished"
          @go-to-space="goToSpace" />

        <div class="feed-tabs-wrapper">
          <el-tabs v-if="!isSearching" v-model="activeFeedTab" class="feed-tabs">
            <el-tab-pane label="发现" name="discover"></el-tab-pane>
            <el-tab-pane label="关注" name="following"></el-tab-pane>
          </el-tabs>

          <div v-else class="search-status-bar">
            <div class="search-info">
              <el-icon>
                <Search />
              </el-icon>
              <span>关于 "<strong>{{ lastSearchQuery }}</strong>" 的搜索结果 ({{ currentFeed.length }}条)</span>
            </div>
            <el-button link type="primary" @click="handleSearchBack">
              <el-icon>
                <Back />
              </el-icon> {{ searchBackText }}
            </el-button>
          </div>
        </div>

        <div class="feed-list">
          <el-empty v-if="currentFeed.length === 0" description="未找到相关动态，换个关键词试试吧" />

          <transition-group name="list" tag="div">
            <PostItem v-for="post in currentFeed" :key="post.id" :post="post" :viewer-user-id="userId"
              :viewer-avatar="userAvatar" :follow-loading-id="followLoadingId" @go-to-space="goToSpace"
              @follow="followAuthor" @unfollow="unfollowAuthor" @toggle-like="toggleLike"
              @favorite-changed="handleFavoriteChanged" @topic-click="handleTopicClick"
              @comment-added="handleCommentAdded" @comment-deleted="handleCommentDeleted" />
          </transition-group>
          <div class="load-more-wrapper" v-if="feedList.length < total">
            <el-button :loading="loadingMore" @click="loadMore" type="primary" plain round>加载更多</el-button>
          </div>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="sticky-sidebar">
          <div class="search-wrapper">
            <el-input v-model="searchQuery" placeholder="搜索动态、用户或话题..." size="large" clearable
              @keyup.enter="handleSearch" @clear="clearSearch" class="community-search">
              <template #prefix>
                <el-icon>
                  <Search />
                </el-icon>
              </template>
              <template #append>
                <el-button class="search-btn" @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
          </div>

          <el-card class="sidebar-card leaderboard-card" shadow="never">
            <template #header>
              <div class="sidebar-header">
                <span><el-icon>
                    <Histogram />
                  </el-icon> 深圳大学深蹲英雄榜</span>
                <el-button link type="primary" size="small">本周排名</el-button>
              </div>
            </template>
            <div class="leaderboard-list">
              <div v-for="(hero, index) in leaderboard" :key="index" class="hero-item">
                <div class="hero-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
                <el-avatar :size="32" :src="hero.avatar" :style="{ cursor: hero.userId ? 'pointer' : 'default' }"
                  @click="hero.userId && goToSpace(hero.userId)" />
                <div class="hero-name" :style="{ cursor: hero.userId ? 'pointer' : 'default' }"
                  @click="hero.userId && goToSpace(hero.userId)">
                  {{ hero.name }}
                </div>
                <div class="hero-score">{{ hero.score }} 个</div>
              </div>
            </div>
            <el-button class="view-all-btn" text type="primary"
              style="width: 100%; margin-top: 10px;">查看完整榜单</el-button>
          </el-card>

          <el-card class="sidebar-card trending-card" shadow="never">
            <template #header>
              <div class="sidebar-header">
                <span><el-icon>
                    <Discount />
                  </el-icon> 热门话题</span>
              </div>
            </template>
            <div class="trending-list">
              <div class="trending-item" v-for="(tag, index) in trendingTags" :key="index"
                @click="quickSearch(tag.name)">
                <span class="tag-hash">#</span>
                <span class="tag-name">{{ tag.name }}</span>
                <span class="tag-hot">{{ tag.hot }} 浏览</span>
              </div>
            </div>
          </el-card>
        </div>


      </el-col>
    </el-row>
    <transition name="el-fade-in-linear">
      <div v-show="showFloatButton" class="float-post-btn" @click="scrollToPublisher">
        <el-tooltip content="返回发布动态" placement="left">
          <div class="btn-content">
            <el-icon :size="24">
              <EditPen />
            </el-icon>
            <span>发动态</span>
          </div>
        </el-tooltip>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter, useRoute } from 'vue-router'
import { EditPen, Histogram, Discount, Search, Back } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/userStore'
import Publisher from '@/components/community/Publisher.vue'
import PostItem from '@/components/community/PostItem.vue'
import { workoutApi } from '@/api/workout'

const router = useRouter()
const route = useRoute()

const userStore = useUserStore()
const { avatar, nickname } = storeToRefs(userStore)
const userId = computed(() => userStore.userId)
const userAvatar = computed(() => avatar.value)
const activeFeedTab = ref('discover')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loadingMore = ref(false)
const currentTopicFilter = ref('')

// ================= 信息流原始数据 =================
// 所有的推文都存在这里
const feedList = reactive([])

// ================= 搜索与流切换核心逻辑 =================
const searchQuery = ref('')
const lastSearchQuery = ref('') // 记录真正触发搜索时的词
const isSearching = ref(false)

// 计算属性：控制页面渲染哪个数组的数据
const currentFeed = computed(() => feedList)

const isSinglePostView = computed(() => {
  const postId = String(route.query?.postId || '').trim()
  return Boolean(postId)
})

const searchBackText = computed(() => {
  if (!isSearching.value) return '返回推荐流'
  return isSinglePostView.value ? '返回' : '返回推荐流'
})

const navigateBackToOrigin = () => {
  const from = String(route.query?.from || '').trim()
  const tab = String(route.query?.tab || '').trim()
  const folderId = String(route.query?.folderId || '').trim()

  if (from === 'profile') {
    router.replace({ name: 'Profile', query: { tab: tab || 'posts' } })
    return
  }

  if (from === 'collectionDetail' && folderId) {
    router.replace({ name: 'CollectionDetail', params: { id: folderId }, query: { from: 'profile', tab: 'collections' } })
    return
  }

  router.back()
}

// 执行搜索
const handleSearch = async () => {
  const name = (searchQuery.value || '').replace(/^#/, '').trim()
  if (!name) {
    clearSearch()
    return
  }
  isSearching.value = true
  lastSearchQuery.value = name
  currentTopicFilter.value = name
  await reloadDiscover()
}

// 清空搜索，返回推荐流
const clearSearch = () => {
  searchQuery.value = ''
  isSearching.value = false
  currentTopicFilter.value = ''
  activeFeedTab.value = 'discover'
  reloadDiscover()
}

const handleSearchBack = () => {
  if (isSinglePostView.value) {
    navigateBackToOrigin()
    return
  }
  clearSearch()
}

const loadSinglePost = async (postIdRaw) => {
  const postId = String(postIdRaw || '').trim()
  if (!postId) return

  isSearching.value = true
  searchQuery.value = ''
  lastSearchQuery.value = `推文ID: ${postId}`
  currentTopicFilter.value = ''
  activeFeedTab.value = 'discover'
  loadingMore.value = false
  total.value = 0
  page.value = 1
  feedList.splice(0, feedList.length)

  let post = null
  try {
    const cache = sessionStorage.getItem(`community:post:${postId}`)
    if (cache) post = JSON.parse(cache)
  } catch (e) { }

  if (!post) {
    try {
      post = await request.get(`/posts/${postId}`)
    } catch (e) { }
  }

  if (!post) {
    try {
      const data = await request.get('/posts/search', { params: { page: 1, size: 10, keyword: postId } })
      const records = data?.records || []
      post = records.find(r => String(r?.id) === postId) || records[0] || null
    } catch (e) { }
  }

  if (!post) {
    lastSearchQuery.value = `推文ID: ${postId}`
    total.value = 0
    return
  }

  feedList.push(post)
  total.value = 1
  const content = String(post.content || '').trim()
  if (content) lastSearchQuery.value = content.length > 20 ? `${content.slice(0, 20)}...` : content
}

// 联动：点击右侧热门话题，直接触发搜索
const quickSearch = (tagName) => {
  searchQuery.value = tagName
  handleSearch()
}

const goToSpace = (targetUserId) => {
  if (!targetUserId) return
  router.push({ name: 'UserSpace', params: { id: String(targetUserId) } })
}

const followLoadingId = ref(null)
const followAuthor = async (post) => {
  if (!post?.authorId) return
  try {
    followLoadingId.value = post.authorId
    await request.post(`/follow/${post.authorId}`)

    // 【核心修复】同步更新列表中所有该作者的推文状态
    feedList.forEach(p => {
      if (p.authorId === post.authorId) {
        p.isFollowing = true
      }
    })

    ElMessage.success('关注成功')
  } finally {
    followLoadingId.value = null
  }
}
const unfollowAuthor = async (post) => {
  if (!post?.authorId) return
  try {
    followLoadingId.value = post.authorId
    await request.delete(`/follow/${post.authorId}`)

    // 【核心修复】同步取消关注状态
    feedList.forEach(p => {
      if (p.authorId === post.authorId) {
        p.isFollowing = false
      }
    })

    ElMessage.success('已取消关注')
  } finally {
    followLoadingId.value = null
  }
}

const toggleLike = async (post) => {
  if (String(post?.sourceType || '') === 'workout_report') return
  if (post.isLiked) {
    const data = await request.delete(`/posts/${post.id}/like`)
    post.isLiked = data?.liked ?? false
    post.likes = data?.likes ?? Math.max(0, (post.likes || 0) - 1)
  } else {
    const data = await request.post(`/posts/${post.id}/like`)
    post.isLiked = data?.liked ?? true
    post.likes = data?.likes ?? (post.likes || 0) + 1
  }
}

const handleFavoriteChanged = (payload) => {
  const id = payload?.postId
  if (!id) return
  feedList.forEach(p => {
    if (Number(p?.id) !== Number(id)) return
    if (typeof payload?.isFavorited === 'boolean') p.isFavorited = payload.isFavorited
    if (payload?.favorites != null) p.favorites = payload.favorites
  })
}

const handlePublished = (newPost) => {
  if (!newPost) return
  feedList.unshift(newPost)
  clearSearch()
}

const handleTopicClick = (topicName) => {
  if (!topicName) return
  searchQuery.value = topicName
  handleSearch()
}

const handleCommentAdded = (postId) => {
  const p = feedList.find(x => Number(x?.id) === Number(postId))
  if (!p) return
  p.comments = (p.comments || 0) + 1
}

const handleCommentDeleted = (postId) => {
  // 1. 在大列表中根据 ID 找到这条推文
  const targetPost = feedList.find(p => Number(p.id) === Number(postId))

  // 2. 如果找到了，就把它的评论计数减 1
  if (targetPost && targetPost.comments > 0) {
    targetPost.comments -= 1
    console.log(`推文 ${postId} 的评论数已同步减少为: ${targetPost.comments}`)
  }
}

// ================= 右侧边栏数据 =================
const leaderboard = computed(() => [
  { name: 'Jack_Iron', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', score: 3250 },
  { name: nickname.value || '我', avatar: userAvatar.value, score: 2840, userId: userId.value },
  { name: '代码与铁块', avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png', score: 2100 },
  { name: 'Redbird_Dream', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 1850 }
])

const trendingTags = reactive([
  { name: 'AI动作纠错体验', hot: '12.4w' },
  { name: '深蹲打卡挑战赛', hot: '8.9w' },
  { name: '大学生宿舍减脂', hot: '5.2w' },
  { name: '红鸟营备战日常', hot: '3.1w' }
])

const recommendedTopicNames = computed(() => trendingTags.map(tag => tag.name))

const hydrateAiReports = async (posts) => {
  const list = Array.isArray(posts) ? posts : []
  const targets = list
    .map(p => ({ post: p, id: p?.aiReportId ?? p?.ai_report_id ?? p?.aiReportID ?? p?.ai_reportId ?? p?.workoutSessionId }))
    .filter(x => x.post && !x.post.aiReport && x.id != null)

  if (!targets.length) return

  await Promise.all(targets.map(async ({ post, id }) => {
    try {
      const data = await workoutApi.getSession(String(id))
      post.aiReport = data || null
    } catch (e) { }
  }))
}

// ================= 后端分页数据加载 =================
const fetchDiscover = async () => {
  loadingMore.value = true
  const params = { page: page.value, size: size.value }
  let data
  if (isSearching.value && lastSearchQuery.value) {
    data = await request.get('/posts/search', { params: { ...params, keyword: lastSearchQuery.value } })
  } else {
    data = await request.get('/posts', { params: { ...params, topic: currentTopicFilter.value || undefined } })
  }
  total.value = data?.total ?? 0
  const records = data?.records ?? []
  await hydrateAiReports(records)
  records.forEach(r => feedList.push(r))
  loadingMore.value = false
}

const fetchFollowing = async () => {
  loadingMore.value = true
  const data = await request.get('/posts/following', { params: { page: page.value, size: size.value } })
  total.value = data?.total ?? 0
  const records = data?.records ?? []
  await hydrateAiReports(records)
  records.forEach(r => feedList.push(r))
  loadingMore.value = false
}

const reloadDiscover = async () => {
  feedList.splice(0, feedList.length)
  page.value = 1
  await fetchDiscover()
}

const reloadFollowing = async () => {
  feedList.splice(0, feedList.length)
  page.value = 1
  await fetchFollowing()
}

const loadMore = async () => {
  if (loadingMore.value) return
  if (feedList.length >= total.value) return
  page.value += 1
  if (activeFeedTab.value === 'discover') {
    await fetchDiscover()
  } else {
    await fetchFollowing()
  }
}

const showFloatButton = ref(false)
const publisherRef = ref(null) // 定义引用

// 监听滚动逻辑
const handleScroll = () => {
  const el = publisherRef.value?.rootEl?.value || publisherRef.value?.$el
  if (!el) return

  // 获取发布框在屏幕上的实时坐标
  const rect = el.getBoundingClientRect()
  const navbarHeight = 40 // 这里的数值建议和你导航栏的实际高度一致

  // 核心逻辑：如果发布框的底部 已经滚到了 导航栏下方，就显示小铅笔
  // 也就是说：只要发布框消失在视野里（被遮住），铅笔就出来
  showFloatButton.value = rect.bottom < navbarHeight
}

// 点击按钮：平滑滚回顶部并聚焦输入框
const scrollToPublisher = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })

  // 延长提示时间
  ElMessage({
    message: '已回到发布区域，分享你的健身灵感吧！',
    type: 'success',
    duration: 1000,      // 设为 5000 毫秒（5秒）
    showClose: true,     // 建议开启关闭按钮，如果用户嫌长可以手动关掉
    offset: 80           // 计科细节：如果被导航栏挡住，可以调大偏移量让他向下移一点
  })
  // 小细节：自动聚焦到输入框，用户直接就能打字
  // 如果你给 el-input 加了 ref="publisherInput"，这里可以执行 publisherInput.value.focus()
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

// 在组件卸载时移除监听，防止内存泄漏
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

watch(activeFeedTab, async (n) => {
  if (n === 'discover') {
    await reloadDiscover()
  } else {
    await reloadFollowing()
  }
})

onMounted(async () => {
  const postId = String(route.query?.postId || '').trim()
  if (postId) {
    await loadSinglePost(postId)
    return
  }
  await reloadDiscover()
})

watch(() => route.query?.postId, async (n, o) => {
  const nextId = String(n || '').trim()
  const prevId = String(o || '').trim()
  if (nextId) {
    await loadSinglePost(nextId)
    return
  }
  if (prevId && !nextId) {
    clearSearch()
  }
})
</script>

<style scoped>
.community-container {
  max-width: 1100px;
  margin: 0 auto;
}

.search-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f0f9eb;
  border-radius: 8px;
  border-left: 4px solid #67C23A;
  margin-bottom: 16px;
}

.search-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.search-info strong {
  color: #303133;
  font-size: 15px;
}

.load-more-wrapper {
  display: flex;
  justify-content: center;
  margin: 16px 0 24px;
}

.search-wrapper {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.community-search :deep(.el-input__wrapper) {
  border-radius: 12px 0 0 12px;
  box-shadow: none !important;
  border: 1px solid #dcdfe6;
  border-right: none;
}

.community-search :deep(.el-input__wrapper.is-focus) {
  border-color: #409EFF;
}

.community-search :deep(.el-input-group__append) {
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 0 12px 12px 0;
  font-weight: bold;
  letter-spacing: 1px;
}

.community-search :deep(.el-input-group__append):hover {
  background-color: #66b1ff;
}

.feed-tabs-wrapper {
  margin-bottom: 16px;
  padding: 0 4px;
}

.feed-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #ebeef5;
}

.feed-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
}

.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.sidebar-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.sidebar-header {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hero-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
}

.hero-item:last-child {
  border-bottom: none;
}

.hero-rank {
  width: 20px;
  font-weight: 900;
  font-style: italic;
  text-align: center;
  color: #c0c4cc;
}

.rank-1 {
  color: #F56C6C;
  font-size: 18px;
}

.rank-2 {
  color: #E6A23C;
  font-size: 16px;
}

.rank-3 {
  color: #409EFF;
  font-size: 15px;
}

.hero-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hero-score {
  font-size: 13px;
  font-weight: bold;
  color: #909399;
}

.trending-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  cursor: pointer;
}

.trending-item:hover .tag-name {
  color: #409EFF;
}

.tag-hash {
  color: #F56C6C;
  font-weight: bold;
  margin-right: 8px;
}

.tag-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
  transition: color 0.3s;
}

.tag-hot {
  font-size: 12px;
  color: #909399;
}

.sticky-sidebar {
  position: sticky;
  top: 80px;
  align-self: start;
  transition: top 0.3s;
}

.float-post-btn {
  position: fixed;
  right: 40px;
  bottom: 100px;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-radius: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  cursor: pointer;
  z-index: 99;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.float-post-btn:hover {
  transform: scale(1.1) translateY(-5px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.6);
}

.btn-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 10px;
  font-weight: bold;
}

.btn-content span {
  margin-top: 2px;
}
</style>

<style>
/* 使用刚才定义的 popper-class 锁定范围，防止污染其他页面的下拉框 */
.custom-unfollow-dropdown .el-dropdown-menu__item {
  font-size: 12px !important;
  /* 强制设为 12px */
  line-height: 1 !important;
  /* 压缩行高 */
  padding: 8px 16px !important;
  /* 调整边距使其更紧凑 */
  color: #f56c6c !important;
  /* 建议取消关注用红色，更有辨识度 */
}
</style>
