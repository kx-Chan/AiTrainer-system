<template>
  <div class="collection-detail-container">
    <div class="header-section">
      <el-page-header @back="$router.back()">
        <template #content>
          <div class="header-content">
            <span class="folder-title">{{ folderName }}</span>
            <el-tag v-if="postTotal > 0" size="small" effect="plain" round>
              {{ postTotal }} 篇内容
            </el-tag>
          </div>
        </template>
      </el-page-header>
    </div>

    <div v-loading="loading" class="post-list-wrapper">
      <el-card v-for="post in posts" :key="post.id" class="minimal-post-card clickable" shadow="hover"
        @click="openPostInCommunity(post)">
        <div class="card-header">
          <el-avatar :size="40" :src="post.avatar" />
          <div class="meta-info">
            <div class="author-name">{{ post.author }}</div>
            <div class="time-device">
              {{ formatTime(post.time) }} · {{ post.device || 'AiTrainer' }}
            </div>
          </div>
        </div>

        <div class="card-content">
          {{ post.content }}
        </div>

      </el-card>

      <el-empty v-if="!loading && posts.length === 0" description="收藏夹空空如也，快去发现好内容吧" />

      <div v-if="postTotal > page.size" class="pagination-container">
        <el-pagination v-model:current-page="page.current" :page-size="page.size" :total="postTotal"
          layout="prev, pager, next" background @current-change="loadPosts" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/userStore.js'
import { ElMessage } from 'element-plus'
// ❌ 删掉旧的 import：import PostItem from '@/components/community/PostItem.vue'
import { folderApi, itemApi } from '@/api/collection'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 获取 ID
const folderId = route.params.id

// ✅ 防御性计算：防止由于 userStore 还没准备好导致页面崩溃
const currentUserId = computed(() => {
  return userStore.userInfo?.id || userStore.userInfo?.userId || 0
})

const posts = ref([])
const loading = ref(false)
const folderName = ref('收藏夹详情')
const postTotal = ref(0)
const page = ref({ current: 1, size: 10 })

// 辅助函数：简易时间格式化 (例如: 2026-03-28 截取 YY-MM-DD)
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return String(timeStr).length > 10 ? String(timeStr).substring(0, 10) : String(timeStr)
}

/**
 * 1. 加载标题和元数据
 */
const loadFolderInfo = async () => {
  try {
    const res = await folderApi.get(folderId)
    // 根据你的拦截器情况，res 往往就是 data 对象本身
    if (res && res.name) {
      folderName.value = res.name
    } else if (res?.data?.name) {
      folderName.value = res.data.name
    }
  } catch (e) {
    console.error("获取收藏夹信息失败:", e)
  }
}

/**
 * 2. 分页加载推文列表
 */
const loadPosts = async () => {
  loading.value = true
  try {
    const res = await itemApi.getPostsInFolder(folderId, {
      page: page.value.current,
      size: page.value.size
    })

    // ✅ 双保险逻辑：解决数据加载不出来的问题
    const data = res?.records ? res : res?.data

    if (data && data.records) {
      posts.value = data.records
      postTotal.value = data.total || 0
    } else {
      posts.value = []
    }
  } catch (e) {
    console.error("加载推文失败:", e)
  } finally {
    loading.value = false
  }
}

const openPostInCommunity = (post) => {
  if (!post?.id) return
  const normalized = {
    likes: post.likes ?? 0,
    favorites: post.favorites ?? 0,
    comments: post.comments ?? 0,
    ...post
  }
  try {
    sessionStorage.setItem(`community:post:${post.id}`, JSON.stringify(normalized))
  } catch (e) { }
  router.push({
    name: 'Community',
    query: { postId: String(post.id), from: 'collectionDetail', folderId: String(folderId), tab: 'collections' }
  })
}

// ❌ 移除旧的交互处理 handleUnfavoriteDetail, handleLike 等逻辑

onMounted(() => {
  if (folderId) {
    loadFolderInfo()
    loadPosts()
  }
})
</script>

<style scoped>
.collection-detail-container {
  max-width: 700px;
  /* 对齐社区主页的宽度 */
  margin: 0 auto;
  padding: 20px 15px;
}

.header-section {
  margin-bottom: 25px;
  background: #fff;
  padding: 15px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.folder-title {
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}

.post-list-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
  /* 卡片之间的呼吸感 */
}

/* ✅ 2. 核心样式修改：像素级对齐你提供的图片 (image_10.png) */
.minimal-post-card {
  border-radius: 8px;
  /* 简洁的小圆角 */
  border: 1px solid #e4e7ed;

  /* 确保底部的 padding 较小，没有图标栏后更干净 */
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.minimal-post-card.clickable {
  cursor: pointer;
  transition: all 0.2s;
}

.minimal-post-card.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #dcdfe6;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  /* 头像与内容的间距 */
}

.meta-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.time-device {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.card-content {
  font-size: 15px;
  color: #303133;
  line-height: 1.6;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>
