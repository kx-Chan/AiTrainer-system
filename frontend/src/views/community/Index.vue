<template>
  <div class="community-container">
    <el-row :gutter="24">
      <el-col :span="16">
        
        <el-card ref="publisherRef" class="publisher-card" shadow="never">
          <div class="publisher-layout">
            <el-avatar :size="48" :src="userAvatar" class="publisher-avatar" />
            <div class="publisher-input-area">
              <div v-if="selectedTopic" class="publisher-topic-bar">
                <el-tag size="small" effect="plain" round closable @close="clearTopic">#{{ selectedTopic }}</el-tag>
              </div>
              <el-input 
                v-model="newPostText" 
                type="textarea" 
                :rows="3" 
                placeholder="分享你的训练心得，或者晒出今天的 AI 战报..." 
                resize="none"
                maxlength="200"
                show-word-limit
              />
              <div class="publisher-actions">
                <div class="action-icons">
                  <el-upload
                    class="post-uploader"
                    :http-request="handleImageUpload"
                    :on-remove="handleImageRemove"
                    :file-list="uploadedFileList"
                    :limit="9"
                    multiple
                    accept="image/*"
                    list-type="picture-card">
                    <el-icon><Picture /></el-icon>
                  </el-upload>
                  <el-popover v-model:visible="isTopicPopoverVisible" placement="bottom-start" :width="320" trigger="click">
                    <div class="topic-popover">
                      <div class="topic-suggest-title">推荐话题</div>
                      <div class="topic-suggest-list">
                        <el-tag
                          v-for="name in recommendedTopicNames"
                          :key="name"
                          class="topic-suggest-tag"
                          effect="plain"
                          round
                          @click="selectTopic(name)"
                        >
                          #{{ name }}
                        </el-tag>
                      </div>
                      <el-divider style="margin: 12px 0;" />
                      <el-input
                        v-model="customTopic"
                        placeholder="自定义话题（不需要输入#）"
                        maxlength="20"
                        clearable
                        @keyup.enter="applyCustomTopic"
                      />
                      <div class="topic-popover-actions">
                        <el-button size="small" @click="clearTopic">清空</el-button>
                        <el-button size="small" type="primary" @click="applyCustomTopic">使用话题</el-button>
                      </div>
                    </div>
                    <template #reference>
                      <el-button link type="primary"><el-icon size="18"><CollectionTag /></el-icon> 话题</el-button>
                    </template>
                  </el-popover>
                </div>
                <el-button type="primary" round class="publish-btn" :loading="isPublishing" :disabled="!newPostText.trim()" @click="publishPost">
                  发布动态
                </el-button>
              </div>
            </div>
          </div>
        </el-card>

        <div class="feed-tabs-wrapper">
          <el-tabs v-if="!isSearching" v-model="activeFeedTab" class="feed-tabs">
            <el-tab-pane label="发现" name="discover"></el-tab-pane>
            <el-tab-pane label="关注" name="following"></el-tab-pane>
          </el-tabs>
          
          <div v-else class="search-status-bar">
            <div class="search-info">
              <el-icon><Search /></el-icon> 
              <span>关于 "<strong>{{ lastSearchQuery }}</strong>" 的搜索结果 ({{ currentFeed.length }}条)</span>
            </div>
            <el-button link type="primary" @click="clearSearch">
              <el-icon><Back /></el-icon> 返回推荐流
            </el-button>
          </div>
        </div>

        <div class="feed-list">
          <el-empty v-if="currentFeed.length === 0" description="未找到相关动态，换个关键词试试吧" />
          
          <transition-group name="list" tag="div">
            <el-card v-for="post in currentFeed" :key="post.id" class="post-card" shadow="hover">
              
              <div class="post-header">
                <el-avatar :size="40" :src="post.avatar" />
                <div class="post-user-info">
                  <div class="user-name">
                    {{ post.author }}
                    <el-tag v-if="post.isPro" type="warning" size="small" effect="dark" round class="pro-tag">PRO</el-tag>
                  </div>
                  <div class="post-time">
                    {{ post.time && post.time.length > 10 ? post.time.substring(0, 10) : post.time }} · 来自 {{ post.device }}
                  </div>
                </div>
                <template v-if="post.authorId !== userId">
                  <el-button
                    v-if="!post.isFollowing"
                    size="small"
                    round
                    plain
                    type="primary"
                    class="follow-btn"
                    :loading="followLoadingId === post.authorId"
                    @click="followAuthor(post)"
                  >+ 关注</el-button>
                  <el-dropdown v-else trigger="click" popper-class="custom-unfollow-dropdown">
                    <el-button size="small" round class="follow-btn" :loading="followLoadingId === post.authorId">
                      已关注
                      <el-icon style="margin-left: 4px;"><ArrowDown /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item @click="unfollowAuthor(post)" >
                          取消关注
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </template>
              </div>

              <div class="post-content">
                <p class="text-content">
                  <span v-if="post.topic" class="topic-tag">{{ post.topic }}</span>
                  {{ post.content }}
                </p>
                
                <div v-if="post.images && post.images.length" 
                    :class="{
                      'post-images-dynamic-v2': true,
                      'is-single': post.images.length === 1,
                      'is-grid-2': post.images.length === 2,
                      'is-grid-multi': post.images.length >= 3
                    }">
                  
                  <template v-if="post.images.length === 1">
                    <el-image
                      :src="post.images[0]"
                      fit="contain" :preview-src-list="post.images"
                      class="post-image-item-dynamic-v2-single"
                    />
                  </template>
                  
                  <template v-else>
                    <div v-for="(img, idx) in post.images" :key="idx" class="image-wrapper-ratio-square">
                      <el-image
                        :src="img"
                        fit="cover" :preview-src-list="post.images"
                        :initial-index="idx"
                        class="post-image-item-dynamic-v2-grid"
                      />
                    </div>
                  </template>
                </div>
                
                <div v-if="post.aiReport" class="ai-report-embed">
                  <div class="report-header">
                    <el-icon color="#E6A23C" size="18"><Trophy /></el-icon>
                    <span>AiTrainer 智能评测战报</span>
                  </div>
                  <div class="report-body">
                    <div class="report-score">
                      <span class="score-num">{{ post.aiReport.score }}</span>分
                    </div>
                    <div class="report-details">
                      <div>动作：<strong>{{ post.aiReport.action }}</strong></div>
                      <div>消耗：🔥 {{ post.aiReport.calories }} kcal</div>
                      <div class="report-comment">AI点评：{{ post.aiReport.comment }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="post-footer">
                <el-tooltip content="点赞" placement="top" :show-after="500">
                  <div class="interaction-btn" @click="toggleLike(post)" :class="{ 'is-liked': post.isLiked }">
                    <svg t="1711545600" class="icon heart-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4245" width="20" height="20">
                      <path d="M512 896a42.667 42.667 0 0 1-30.293-12.373l-306.347-306.347c-80-80-80-210.347 0-290.347a205.333 205.333 0 0 1 290.347 0l46.293 46.293 46.293-46.293a205.333 205.333 0 0 1 290.347 0c80 80 80 210.347 0 290.347l-306.347 306.347A42.667 42.667 0 0 1 512 896z" :fill="post.isLiked ? '#f56c6c' : 'currentColor'" p-id="4246"></path>
                    </svg>
                    <span :style="{ color: post.isLiked ? '#f56c6c' : '' }">{{ post.likes }}</span>
                  </div>
                </el-tooltip>

                <el-tooltip content="添加到收藏夹" placement="top" :show-after="500">
                  <div class="interaction-btn" @click="toggleFavorite(post)" :class="{ 'is-favorited': post.isFavorited }">
                    <el-icon size="20">
                      <component :is="post.isFavorited ? 'StarFilled' : 'Star'" :color="post.isFavorited ? '#ff9900' : ''" />
                    </el-icon>
                    <span :style="{ color: post.isFavorited ? '#ff9900' : '' }">{{ post.favorites ?? 0 }}</span>
                  </div>
                </el-tooltip>

                <el-tooltip content="查看评论" placement="top" :show-after="500">
                  <div class="interaction-btn" @click="toggleCommentPanel(post)">
                    <el-icon size="20"><ChatDotRound /></el-icon>
                    <span>{{ post.comments }}</span>
                  </div>
                </el-tooltip>
              </div>
              <div v-if="post.showComments" class="comment-panel">
                <div class="comment-input-section">
                  <el-avatar :size="32" :src="userAvatar" />
                  <div class="input-wrapper">
                    <el-input
                      v-model="commentInputs[post.id]"
                      type="textarea"
                      :autosize="{ minRows: 1, maxRows: 4 }"
                      placeholder="友善评论，文明发言..."
                      resize="none"
                      @keydown.enter.exact.prevent="submitComment(post)" 
                      class="custom-comment-input"
                    />
                    <div class="input-footer">
                      <span class="hint">Enter 发送 / Shift + Enter 换行</span>
                      <el-button 
                        size="small" 
                        type="primary" 
                        round
                        :loading="commentLoadingId === post.id" 
                        :disabled="!commentInputs[post.id]?.trim()"
                        @click="submitComment(post)"
                      >发布</el-button>
                    </div>
                  </div>
                </div>

                <div class="comment-list">
                  <div v-for="c in (commentsMap[post.id] || [])" :key="c.id" class="comment-item-wrapper">
                    <el-avatar :size="36" :src="c.avatar" />
                    <div class="comment-content-box">
                      <div class="comment-meta">
                        <span class="comment-author">{{ c.author }}</span>
                        <el-tag v-if="c.userId === post.authorId" size="small" effect="plain" class="author-tag">作者</el-tag>
                        <span class="comment-time">{{ c.time }}</span>
                      </div>
                      <div class="comment-text">{{ c.content }}</div>
                      <div class="comment-actions">
                        <el-button link size="small">回复</el-button>
                      </div>
                    </div>
                  </div>
                  
                  <div v-if="hasMoreComments[post.id]" class="load-more-comments">
                    <el-button text size="small" :loading="commentMoreLoadingId === post.id" @click="loadMoreComments(post)">
                      查看更多评论 <el-icon><ArrowDown /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </transition-group>
          <div class="load-more-wrapper" v-if="feedList.length < total">
            <el-button :loading="loadingMore" @click="loadMore" type="primary" plain round>加载更多</el-button>
          </div>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="sticky-sidebar">
            <div class="search-wrapper">
              <el-input
                v-model="searchQuery"
                placeholder="搜索动态、用户或话题..."
                size="large"
                clearable
                @keyup.enter="handleSearch"
                @clear="clearSearch"
                class="community-search"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
                <template #append>
                  <el-button class="search-btn" @click="handleSearch">搜索</el-button>
                </template>
              </el-input>
            </div>

            <el-card class="sidebar-card leaderboard-card" shadow="never">
              <template #header>
                <div class="sidebar-header">
                  <span><el-icon><Histogram /></el-icon> 深圳大学深蹲英雄榜</span>
                  <el-button link type="primary" size="small">本周排名</el-button>
                </div>
              </template>
              <div class="leaderboard-list">
                <div v-for="(hero, index) in leaderboard" :key="index" class="hero-item">
                  <div class="hero-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
                  <el-avatar :size="32" :src="hero.avatar" />
                  <div class="hero-name">{{ hero.name }}</div>
                  <div class="hero-score">{{ hero.score }} 个</div>
                </div>
              </div>
              <el-button class="view-all-btn" text type="primary" style="width: 100%; margin-top: 10px;">查看完整榜单</el-button>
            </el-card>

            <el-card class="sidebar-card trending-card" shadow="never">
              <template #header>
                <div class="sidebar-header">
                  <span><el-icon><Discount /></el-icon> 热门话题</span>
                </div>
              </template>
              <div class="trending-list">
                <div class="trending-item" v-for="(tag, index) in trendingTags" :key="index" @click="quickSearch(tag.name)">
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
      <div 
        v-show="showFloatButton" 
        class="float-post-btn" 
        @click="scrollToPublisher"
      >
        <el-tooltip content="返回发布动态" placement="left">
          <div class="btn-content">
            <el-icon :size="24"><EditPen /></el-icon>
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
import { Picture, CollectionTag, EditPen, Trophy, ChatDotRound, Histogram, Discount, Search, Back, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/userStore'

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
const searchResults = ref([])

// 计算属性：控制页面渲染哪个数组的数据
const currentFeed = computed(() => feedList)

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
  reloadDiscover()
}

// 联动：点击右侧热门话题，直接触发搜索
const quickSearch = (tagName) => {
  searchQuery.value = tagName
  handleSearch()
}

// ================= 发布器逻辑 =================
const newPostText = ref('')
const isPublishing = ref(false)
const isTopicPopoverVisible = ref(false)
const selectedTopic = ref('')
const customTopic = ref('')
const uploadedImages = ref([]) // { key, url }
const uploadedFileList = ref([]) // el-upload 展示用

const recommendedTopicNames = computed(() => trendingTags.map(tag => tag.name))

const selectTopic = (name) => {
  if (!name) return
  selectedTopic.value = name
  customTopic.value = name
  isTopicPopoverVisible.value = false
}

const applyCustomTopic = () => {
  const name = (customTopic.value || '').replace(/^#/, '').trim()
  if (!name) return
  selectedTopic.value = name
  isTopicPopoverVisible.value = false
}

const clearTopic = () => {
  selectedTopic.value = ''
  customTopic.value = ''
}

const handleImageUpload = async (options) => {
  const { file, onError, onSuccess } = options
  const form = new FormData()
  form.append('file', file)
  try {
    const data = await request.post('/common/upload/post-image', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    // data: { key, url }
    uploadedImages.value.push({ key: data.key, url: data.url })
    uploadedFileList.value = [...uploadedFileList.value, { name: file.name, url: data.url, response: data }]
    onSuccess && onSuccess(data)
  } catch (e) {
    onError && onError(e)
  }
}

const handleImageRemove = (file, fileList) => {
  uploadedFileList.value = fileList
  // 从 uploadedImages 同步移除
  const resp = file?.response || {}
  if (resp?.key) {
    uploadedImages.value = uploadedImages.value.filter(x => x.key !== resp.key)
  } else if (file?.url) {
    uploadedImages.value = uploadedImages.value.filter(x => x.url !== file.url)
  }
}

const publishPost = async () => {
  const content = newPostText.value.trim()
  if (!content) return
  if (isPublishing.value) return

  try {
    isPublishing.value = true
    const created = await request.post('/posts', {
      content,
      topic: selectedTopic.value || null,
      device: 'Web 端',
      imageKeys: uploadedImages.value.map(x => x.key)
    })

    const newPost = {
      id: created?.id ?? Date.now(),
      author: created?.author || nickname.value || '用户',
      authorId: userId.value,
      avatar: created?.avatar || userAvatar.value,
      time: created?.time || '刚刚',
      device: created?.device || 'Web 端',
      isPro: !!created?.isPro,
      isFollowing: created?.isFollowing ?? true,
      topic: created?.topic || (selectedTopic.value ? `#${selectedTopic.value}` : ''),
      content: created?.content || content,
      images: created?.images || uploadedImages.value.map(x => x.url),
      likes: created?.likes ?? 0,
      comments: created?.comments ?? 0,
      isLiked: created?.isLiked ?? false,
      favorites: created?.favorites ?? 0,
      isFavorited: created?.isFavorited ?? false
    }

    feedList.unshift(newPost)
    newPostText.value = ''
    clearTopic()
    uploadedImages.value = []
    uploadedFileList.value = []
    clearSearch()
    ElMessage.success('发布成功！')
  } finally {
    isPublishing.value = false
  }
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

const toggleFavorite = async (post) => {
  if (post.isFavorited) {
    const data = await request.delete(`/posts/${post.id}/favorite`)
    post.isFavorited = data?.favorited ?? false
    post.favorites = data?.favorites ?? Math.max(0, (post.favorites || 0) - 1)
  } else {
    const data = await request.post(`/posts/${post.id}/favorite`)
    post.isFavorited = data?.favorited ?? true
    post.favorites = data?.favorites ?? (post.favorites || 0) + 1
  }
}

const commentsMap = reactive({})
const commentsPage = reactive({})
const hasMoreComments = reactive({})
const commentInputs = reactive({})
const commentLoadingId = ref(null)
const commentMoreLoadingId = ref(null)

const ensureCommentsState = (postId) => {
  if (!commentsMap[postId]) commentsMap[postId] = []
  if (!commentsPage[postId]) commentsPage[postId] = { page: 1, size: 10, total: 0 }
  if (hasMoreComments[postId] === undefined) hasMoreComments[postId] = true
}
const loadComments = async (post, reset = false) => {
  ensureCommentsState(post.id)
  if (reset) {
    commentsMap[post.id] = []
    commentsPage[post.id].page = 1
    hasMoreComments[post.id] = true
  }
  const p = commentsPage[post.id]
  const data = await request.get(`/posts/${post.id}/comments`, { params: { page: p.page, size: p.size } })
  p.total = data?.total ?? 0
  ;(data?.records || []).forEach(c => commentsMap[post.id].push(c))
  if (commentsMap[post.id].length >= p.total) {
    hasMoreComments[post.id] = false
  }
}
const toggleCommentPanel = async (post) => {
  post.showComments = !post.showComments
  if (post.showComments && (commentsMap[post.id] || []).length === 0) {
    await loadComments(post, true)
  }
}
const loadMoreComments = async (post) => {
  const p = commentsPage[post.id]
  if (commentsMap[post.id].length >= p.total) return
  try {
    commentMoreLoadingId.value = post.id
    p.page += 1
    await loadComments(post)
  } finally {
    commentMoreLoadingId.value = null
  }
}
const submitComment = async (post) => {
  const text = (commentInputs[post.id] || '').trim()
  if (!text) return
  try {
    commentLoadingId.value = post.id
    const c = await request.post(`/posts/${post.id}/comments`, { content: text })
    commentsMap[post.id].unshift(c)
    post.comments = (post.comments || 0) + 1
    commentInputs[post.id] = ''
  } finally {
    commentLoadingId.value = null
  }
}

// ================= 右侧边栏数据 =================
const leaderboard = reactive([
  { name: 'Jack_Iron', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', score: 3250 },
  { name: '陈同学_AiTrainer', avatar: userAvatar.value, score: 2840 },
  { name: '代码与铁块', avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png', score: 2100 },  { name: 'Redbird_Dream', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 1850 }
])

const trendingTags = reactive([
  { name: 'AI动作纠错体验', hot: '12.4w' },
  { name: '深蹲打卡挑战赛', hot: '8.9w' },
  { name: '大学生宿舍减脂', hot: '5.2w' },
  { name: '红鸟营备战日常', hot: '3.1w' }
])

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
  records.forEach(r => feedList.push(r))
  loadingMore.value = false
}

const fetchFollowing = async () => {
  loadingMore.value = true
  const data = await request.get('/posts/following', { params: { page: page.value, size: size.value } })
  total.value = data?.total ?? 0
  const records = data?.records ?? []
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
  if (!publisherRef.value) return
  
  // 获取发布框在屏幕上的实时坐标
  const rect = publisherRef.value.$el.getBoundingClientRect()
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
  await reloadDiscover()
})
</script>

<style scoped>
.community-container { max-width: 1100px; margin: 0 auto; }

/* 搜索状态栏 UI */
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

/* ========================================================================= */
/* 动态图片展示组件 V2-FIXED: 让多图中的单张图片和单图大小完全相同 (Fixed 180x180 Squares) */
/* ========================================================================= */

.post-images-dynamic-v2 {
  /* 关键修改：改用 Flex 布局，以便更容易控制固定尺寸项目的排列和间距 */
  display: flex; 
  flex-wrap: wrap; /* 允许换行 */
  gap: 8px;
  margin-top: 8px;
  justify-content: flex-start; /* 靠左对齐 */
  
  width: fit-content; /* 计科细节：容器宽度随内容而变，不强制撑满 */
  max-width: 100%; /* 防止超出父级宽度 */
}

/* --- 单图布局保持不变（极致小巧限制） --- */
.post-images-dynamic-v2.is-single {
  display: block; /* 单图回归单列块布局 */
}

.post-image-item-dynamic-v2-single {
  width: auto;
  height: auto; 
  max-width: 180px !important; /* 极致小巧：180px 盒子 */
  max-height: 180px !important;
  object-fit: contain; /* 保持原图格式不裁剪 */
  border-radius: 6px;
  display: block;
  margin-left: 0;
  margin-top: 0;
  border: 1px solid #f0f2f5; 
}


/* --- 多图网格（2-9）的核心修改 --- */

/* 核心修改：将正方形包裹层改为固定尺寸，完全等同于单图的最大限制 */
.image-wrapper-ratio-square {
  width: 180px; /* 强制设为 180px 固定宽度 */
  height: 180px; /* 强制设为 180px 固定高度 */
  
  /* 移除原本用于响应式的 padding-bottom: 100% 技巧，因为现在高度固定了 */
  position: relative;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #f0f2f5; 
}

/* 内部图片依然填满盒子并做裁剪 (fit="cover") */
.image-wrapper-ratio-square .post-image-item-dynamic-v2-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover; /* 多图保持正方形裁剪，视觉更整齐统一 */
}


.load-more-wrapper {
  display: flex;
  justify-content: center;
  margin: 16px 0 24px;
}
.search-info strong { color: #303133; font-size: 15px; }

/* 搜索框样式 */
.search-wrapper { margin-bottom: 20px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); }
.community-search :deep(.el-input__wrapper) { border-radius: 12px 0 0 12px; box-shadow: none !important; border: 1px solid #dcdfe6; border-right: none; }
.community-search :deep(.el-input__wrapper.is-focus) { border-color: #409EFF; }
.community-search :deep(.el-input-group__append) { background-color: #409EFF; color: white; border: none; border-radius: 0 12px 12px 0; font-weight: bold; letter-spacing: 1px; }
.community-search :deep(.el-input-group__append):hover { background-color: #66b1ff; }

/* 其余核心样式保持不变 */
.publisher-card { border-radius: 12px; margin-bottom: 20px; }
.publisher-layout { display: flex; gap: 16px; }
.publisher-input-area { flex: 1; }
.publisher-topic-bar { margin-bottom: 10px; }
.publisher-input-area :deep(.el-textarea__inner) { border: none; background-color: #f5f7fa; border-radius: 8px; padding: 12px; box-shadow: none; }
.publisher-input-area :deep(.el-textarea__inner:focus) { background-color: #fff; box-shadow: 0 0 0 1px #409EFF inset; }
.publisher-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.action-icons { display: flex; gap: 8px; }
.topic-popover { display: flex; flex-direction: column; }
.topic-suggest-title { font-weight: 700; color: #303133; margin-bottom: 10px; }
.topic-suggest-list { display: flex; flex-wrap: wrap; gap: 8px; }
.topic-suggest-tag { cursor: pointer; }
.topic-popover-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 12px; }

.feed-tabs-wrapper { margin-bottom: 16px; padding: 0 4px; }
.feed-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; background-color: #ebeef5; }
.feed-tabs :deep(.el-tabs__item) { font-size: 16px; font-weight: 500; }

.list-enter-active, .list-leave-active { transition: all 0.5s ease; }
.list-enter-from, .list-leave-to { opacity: 0; transform: translateY(-20px); }

.post-card { border-radius: 12px; margin-bottom: 16px; border: none; }
.post-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.post-user-info { flex: 1; }
.user-name { font-size: 16px; font-weight: bold; color: #303133; display: flex; align-items: center; gap: 6px; }
.pro-tag { transform: scale(0.8); font-style: italic; font-weight: 900; }
.post-time { font-size: 12px; color: #909399; margin-top: 4px; }
.post-content { padding-left: 52px; }
.text-content { font-size: 15px; color: #303133; line-height: 1.6; margin-top: 0; margin-bottom: 12px; }
.topic-tag { color: #409EFF; cursor: pointer; margin-right: 4px; }
.topic-tag:hover { text-decoration: underline; }

.ai-report-embed { background: linear-gradient(145deg, #fffcf5 0%, #fff8e6 100%); border: 1px solid #faecd8; border-radius: 8px; padding: 16px; margin-bottom: 12px; }
.report-header { display: flex; align-items: center; gap: 6px; color: #E6A23C; font-weight: bold; margin-bottom: 12px; font-size: 14px; }
.report-body { display: flex; align-items: center; gap: 20px; }
.report-score { color: #E6A23C; font-size: 12px; }
.score-num { font-size: 36px; font-weight: 900; line-height: 1; font-style: italic; }
.report-details { flex: 1; font-size: 13px; color: #606266; line-height: 1.8; }
.report-comment { margin-top: 4px; color: #909399; font-style: italic; }

.post-footer { display: flex; padding-left: 52px; gap: 40px; margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f2f5; }
.interaction-btn { display: flex; align-items: center; gap: 6px; color: #909399; font-size: 14px; cursor: pointer; transition: color 0.3s; }
.interaction-btn:hover { color: #409EFF; }
.interaction-btn.is-liked { color: #E6A23C; }

.sidebar-card { border-radius: 12px; margin-bottom: 20px; }
.sidebar-header { font-size: 16px; font-weight: bold; color: #303133; display: flex; justify-content: space-between; align-items: center; }
.hero-item { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px dashed #ebeef5; }
.hero-item:last-child { border-bottom: none; }
.hero-rank { width: 20px; font-weight: 900; font-style: italic; text-align: center; color: #c0c4cc; }
.rank-1 { color: #F56C6C; font-size: 18px; }
.rank-2 { color: #E6A23C; font-size: 16px; }
.rank-3 { color: #409EFF; font-size: 15px; }
.hero-name { flex: 1; font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hero-score { font-size: 13px; font-weight: bold; color: #909399; }
.trending-item { display: flex; align-items: center; padding: 10px 0; cursor: pointer; }
.trending-item:hover .tag-name { color: #409EFF; }
.tag-hash { color: #F56C6C; font-weight: bold; margin-right: 8px; }
.tag-name { flex: 1; font-size: 14px; color: #303133; transition: color 0.3s; }
.tag-hot { font-size: 12px; color: #909399; }

/* 修改交互按钮的基础样式 */
.interaction-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1); /* 增加平滑过渡 */
  user-select: none;
}

/* 鼠标悬浮效果 */
.interaction-btn:hover {
  color: #409EFF;
}

/* 爱心激活状态（红色） */
.interaction-btn.is-liked:hover {
  transform: scale(1.1); /* 点赞后悬浮有轻微放大 */
}
.heart-icon {
  transition: fill 0.3s ease;
}

/* 收藏激活状态（橙色） */
.interaction-btn.is-favorited {
  color: #ff9900;
}
.interaction-btn.is-favorited:hover {
  transform: scale(1.1);
}

/* 评论面板容器 */
.comment-panel {
  margin-top: 12px;
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 8px;
  border: 1px solid #f0f2f5;
}

/* 输入框区域 */
.comment-input-section {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}
.input-wrapper {
  flex: 1;
}
.custom-comment-input :deep(.el-textarea__inner) {
  padding: 10px 12px;
  border-radius: 8px;
  background-color: #fff;
  transition: all 0.3s;
  font-size: 14px;
}
.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.input-footer .hint {
  font-size: 12px;
  color: #c0c4cc;
}

/* 评论单项排版 */
.comment-item-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.comment-content-box {
  flex: 1;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}
.comment-item-wrapper:last-child .comment-content-box {
  border-bottom: none;
}
.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.author-tag {
  transform: scale(0.8);
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  /* 确保 meta 容器撑满宽度，否则 margin-left 没地方推 */
  width: 100%; 
}
.comment-time {
  font-size: 12px;
  margin-left: auto;
  color: #909399;
}
.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap; /* 关键：保留换行符显示 */
  word-break: break-all;
}
.comment-actions {
  margin-top: 4px;
}

.sticky-sidebar {
  /* 核心属性 */
  position: sticky;
  
  /* 距离窗口顶部的距离。如果你的导航栏是固定的，
     这个值应该大于导航栏的高度（比如 80px）*/
  top: 80px; 
  
  /* 小细节：确保它是从顶部开始计算粘性的 */
  align-self: start; 
  
  /* 增加一个平滑过渡感 */
  transition: top 0.3s;
}

/* 缩小上传框和已上传图片的尺寸 */
:deep(.post-uploader .el-upload--picture-card),
:deep(.post-uploader .el-upload-list--picture-card .el-upload-list__item) {
  width: 80px !important;
  height: 80px !important;
  border-radius: 8px;
}

/* 调整上传框内图标的大小和位置 */
:deep(.post-uploader .el-upload--picture-card .el-icon) {
  font-size: 20px;
  color: #8c939d;
}

/* 修正上传按钮的行高，确保图标居中 */
:deep(.post-uploader .el-upload--picture-card) {
  line-height: 90px; 
}

.float-post-btn {
  position: fixed;
  right: 40px;
  bottom: 100px; /* 避开可能存在的返回顶部按钮 */
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
  font-size: 12px !important;      /* 强制设为 12px */
  line-height: 1 !important;       /* 压缩行高 */
  padding: 8px 16px !important;   /* 调整边距使其更紧凑 */
  color: #f56c6c !important;      /* 建议取消关注用红色，更有辨识度 */
}
</style>