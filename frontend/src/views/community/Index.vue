<template>
  <div class="community-container">
    <el-row :gutter="24">
      <el-col :span="16">
        
        <el-card class="publisher-card" shadow="never">
          <div class="publisher-layout">
            <el-avatar :size="48" :src="myAvatar" class="publisher-avatar" />
            <div class="publisher-input-area">
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
                  <el-button link type="info"><el-icon size="18"><Picture /></el-icon> 图片</el-button>
                  <el-button link type="warning"><el-icon size="18"><DataAnalysis /></el-icon> 附加 AI 战报</el-button>
                  <el-button link type="primary"><el-icon size="18"><CollectionTag /></el-icon> 话题</el-button>
                </div>
                <el-button type="primary" round class="publish-btn" :disabled="!newPostText" @click="publishPost">
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
                  <div class="post-time">{{ post.time }} · 来自 {{ post.device }}</div>
                </div>
                <el-button v-if="!post.isFollowing" size="small" round plain type="primary" class="follow-btn">+ 关注</el-button>
              </div>

              <div class="post-content">
                <p class="text-content">
                  <span v-if="post.topic" class="topic-tag">{{ post.topic }}</span>
                  {{ post.content }}
                </p>
                
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
                <div class="interaction-btn" @click="toggleLike(post)" :class="{ 'is-liked': post.isLiked }">
                  <el-icon size="18"><component :is="post.isLiked ? 'StarFilled' : 'Star'" /></el-icon>
                  <span>{{ post.likes }}</span>
                </div>
                <div class="interaction-btn">
                  <el-icon size="18"><ChatDotRound /></el-icon>
                  <span>{{ post.comments }}</span>
                </div>
                <div class="interaction-btn">
                  <el-icon size="18"><Share /></el-icon>
                  <span>分享</span>
                </div>
              </div>
            </el-card>
          </transition-group>
        </div>
      </el-col>

      <el-col :span="8">
        
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

      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Picture, DataAnalysis, CollectionTag, Trophy, ChatDotRound, Star, StarFilled, Share, Histogram, Discount, Search, Back } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const myAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const activeFeedTab = ref('discover')

// ================= 信息流原始数据 =================
// 所有的推文都存在这里
const feedList = reactive([
  {
    id: 1,
    author: '林教练 (深大荔园)',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    time: '10分钟前',
    device: 'AiTrainer App',
    isPro: true,
    isFollowing: false,
    topic: '#深蹲打卡挑战赛',
    content: '今天测试了一下 AiTrainer 最新的骨骼关键点追踪算法，延迟几乎感觉不到，太硬核了！给大家看看我的深蹲战报。',
    likes: 128,
    comments: 32,
    isLiked: false,
    aiReport: { action: '杠铃深蹲 (Squat)', score: 96, calories: 320, comment: '动作极度标准，核心极其稳定！' }
  },
  {
    id: 2,
    author: '健身萌新_小李',
    avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png',
    time: '1小时前',
    device: 'Web 端',
    isPro: false,
    isFollowing: true,
    topic: '#大学生宿舍减脂',
    content: '大三狗在宿舍用养生壶煮的西蓝花鸡胸肉，卖相不太行但饱腹感绝了！本月目标减重 5 斤！',
    likes: 45,
    comments: 12,
    isLiked: true
  }
])

// ================= 搜索与流切换核心逻辑 =================
const searchQuery = ref('')
const lastSearchQuery = ref('') // 记录真正触发搜索时的词
const isSearching = ref(false)
const searchResults = ref([])

// 计算属性：控制页面渲染哪个数组的数据
const currentFeed = computed(() => {
  return isSearching.value ? searchResults.value : feedList
})

// 执行搜索
const handleSearch = () => {
  if (!searchQuery.value.trim()) {
    clearSearch()
    return
  }
  
  isSearching.value = true
  lastSearchQuery.value = searchQuery.value
  const keyword = searchQuery.value.toLowerCase()
  
  // 模拟前端本地全文检索：匹配作者名、内容、话题
  searchResults.value = feedList.filter(post => {
    const matchContent = post.content.toLowerCase().includes(keyword)
    const matchAuthor = post.author.toLowerCase().includes(keyword)
    const matchTopic = post.topic && post.topic.toLowerCase().includes(keyword)
    return matchContent || matchAuthor || matchTopic
  })
}

// 清空搜索，返回推荐流
const clearSearch = () => {
  searchQuery.value = ''
  isSearching.value = false
  searchResults.value = []
}

// 联动：点击右侧热门话题，直接触发搜索
const quickSearch = (tagName) => {
  searchQuery.value = tagName
  handleSearch()
}

// ================= 发布器逻辑 =================
const newPostText = ref('')
const publishPost = () => {
  if (!newPostText.value.trim()) return
  const newPost = {
    id: Date.now(),
    author: '陈同学_AiTrainer',
    avatar: myAvatar,
    time: '刚刚',
    device: 'Web 端',
    content: newPostText.value,
    likes: 0,
    comments: 0,
    isFollowing: true,
    isLiked: false
  }
  feedList.unshift(newPost) // 永远插到原始大池子的头部
  newPostText.value = ''
  clearSearch() // 发布后自动切回推荐流看自己的推文
  ElMessage.success('发布成功！')
}

const toggleLike = (post) => {
  post.isLiked = !post.isLiked
  post.isLiked ? post.likes++ : post.likes--
}

// ================= 右侧边栏数据 =================
const leaderboard = reactive([
  { name: 'Jack_Iron', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', score: 3250 },
  { name: '陈同学_AiTrainer', avatar: myAvatar, score: 2840 },
  { name: '代码与铁块', avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png', score: 2100 },  { name: 'Redbird_Dream', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 1850 }
])

const trendingTags = reactive([
  { name: 'AI动作纠错体验', hot: '12.4w' },
  { name: '深蹲打卡挑战赛', hot: '8.9w' },
  { name: '大学生宿舍减脂', hot: '5.2w' },
  { name: '红鸟营备战日常', hot: '3.1w' }
])
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
.publisher-input-area :deep(.el-textarea__inner) { border: none; background-color: #f5f7fa; border-radius: 8px; padding: 12px; box-shadow: none; }
.publisher-input-area :deep(.el-textarea__inner:focus) { background-color: #fff; box-shadow: 0 0 0 1px #409EFF inset; }
.publisher-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.action-icons { display: flex; gap: 8px; }

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
</style>