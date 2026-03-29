<template>
  <el-card class="post-card" shadow="hover">
    <div class="post-header">
      <el-avatar :size="40" :src="post.avatar" style="cursor: pointer;" @click="$emit('go-to-space', post.authorId)" />

      <div class="post-user-info">
        <div class="user-name" style="cursor: pointer;" @click="$emit('go-to-space', post.authorId)">
          {{ post.author }}
          <el-tag v-if="post.isPro" type="warning" size="small" effect="dark" round class="pro-tag">PRO</el-tag>
        </div>
        <div class="post-time">{{ formattedTime }} · 来自 {{ post.device }}</div>
      </div>

      <template v-if="Number(post.authorId) !== Number(viewerUserId)">
        <el-button
          v-if="!post.isFollowing"
          size="small"
          round
          plain
          type="primary"
          class="follow-btn"
          :loading="Number(followLoadingId) === Number(post.authorId)"
          @click="$emit('follow', post)"
        >
          + 关注
        </el-button>

        <el-dropdown v-else trigger="click" popper-class="custom-unfollow-dropdown">
          <el-button size="small" round class="follow-btn" :loading="Number(followLoadingId) === Number(post.authorId)">
            已关注
            <el-icon style="margin-left: 4px;"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$emit('unfollow', post)">取消关注</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </div>

    <div class="post-content">
      <p class="text-content">
        <span v-if="post.topic" class="topic-tag" @click="onTopicClick">{{ post.topic }}</span>
        {{ post.content }}
      </p>

      <div
        v-if="post.images && post.images.length"
        :class="{
          'post-images-dynamic-v2': true,
          'is-single': post.images.length === 1,
          'is-grid-2': post.images.length === 2,
          'is-grid-multi': post.images.length >= 3
        }"
      >
        <template v-if="post.images.length === 1">
          <el-image :src="post.images[0]" fit="contain" :preview-src-list="post.images" class="post-image-item-dynamic-v2-single" />
        </template>

        <template v-else>
          <div v-for="(img, idx) in post.images" :key="idx" class="image-wrapper-ratio-square">
            <el-image
              :src="img"
              fit="cover"
              :preview-src-list="post.images"
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
          <div class="report-score"><span class="score-num">{{ post.aiReport.score }}</span>分</div>
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
        <div class="interaction-btn" :class="{ 'is-liked': post.isLiked }" @click="$emit('toggle-like', post)">
          <svg
            t="1711545600"
            class="icon heart-icon"
            viewBox="0 0 1024 1024"
            version="1.1"
            xmlns="http://www.w3.org/2000/svg"
            p-id="4245"
            width="20"
            height="20"
          >
            <path
              d="M512 896a42.667 42.667 0 0 1-30.293-12.373l-306.347-306.347c-80-80-80-210.347 0-290.347a205.333 205.333 0 0 1 290.347 0l46.293 46.293 46.293-46.293a205.333 205.333 0 0 1 290.347 0c80 80 80 210.347 0 290.347l-306.347 306.347A42.667 42.667 0 0 1 512 896z"
              :fill="post.isLiked ? '#f56c6c' : 'currentColor'"
              p-id="4246"
            ></path>
          </svg>
          <span :style="{ color: post.isLiked ? '#f56c6c' : '' }">{{ post.likes }}</span>
        </div>
      </el-tooltip>

      <el-tooltip content="添加到收藏夹" placement="top" :show-after="500">
        <div class="interaction-btn" :class="{ 'is-favorited': post.isFavorited }" @click="$emit('toggle-favorite', post)">
          <el-icon size="20">
            <component :is="post.isFavorited ? 'StarFilled' : 'Star'" :color="post.isFavorited ? '#ff9900' : ''" />
          </el-icon>
          <span :style="{ color: post.isFavorited ? '#ff9900' : '' }">{{ post.favorites ?? 0 }}</span>
        </div>
      </el-tooltip>

      <el-tooltip content="查看评论" placement="top" :show-after="500">
        <div class="interaction-btn" @click="toggleComments">
          <el-icon size="20"><ChatDotRound /></el-icon>
          <span>{{ post.comments }}</span>
        </div>
      </el-tooltip>
    </div>

    <CommentSection
      v-if="showComments"
      :post-id="post.id"
      :post-author-id="post.authorId"
      :viewer-user-id="viewerUserId"
      :viewer-avatar="viewerAvatar"
      @comment-added="$emit('comment-added', $event)"
      @comment-deleted="$emit('comment-deleted', $event)"
      @go-to-space="$emit('go-to-space', $event)"
    />
  </el-card>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ArrowDown, ChatDotRound, Trophy } from '@element-plus/icons-vue'
import CommentSection from './CommentSection.vue'

const props = defineProps({
  post: { type: Object, required: true },
  viewerUserId: { type: [Number, String], required: true },
  viewerAvatar: { type: String, default: '' },
  followLoadingId: { type: [Number, String, null], default: null }
})

const emit = defineEmits(['go-to-space', 'follow', 'unfollow', 'toggle-like', 'toggle-favorite', 'topic-click', 'comment-added', 'comment-deleted'])

const showComments = ref(false)

const formattedTime = computed(() => {
  const time = props.post?.time
  if (!time) return ''
  const s = String(time)
  return s.length > 10 ? s.substring(0, 10) : s
})

const onTopicClick = () => {
  const raw = props.post?.topic
  const name = raw ? String(raw).replace(/^#/, '').trim() : ''
  if (!name) return
  emit('topic-click', name)
}

const toggleComments = () => {
  showComments.value = !showComments.value
}
</script>

<style scoped>
.post-card {
  border-radius: 12px;
  margin-bottom: 16px;
  border: none;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.post-user-info {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

.pro-tag {
  transform: scale(0.8);
  font-style: italic;
  font-weight: 900;
}

.post-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.post-content {
  padding-left: 52px;
}

.text-content {
  font-size: 15px;
  color: #303133;
  line-height: 1.6;
  margin-top: 0;
  margin-bottom: 12px;
}

.topic-tag {
  color: #409EFF;
  cursor: pointer;
  margin-right: 4px;
}

.topic-tag:hover {
  text-decoration: underline;
}

.post-images-dynamic-v2 {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
  justify-content: flex-start;
  width: fit-content;
  max-width: 100%;
}

.post-images-dynamic-v2.is-single {
  display: block;
}

.post-image-item-dynamic-v2-single {
  width: auto;
  height: auto;
  max-width: 180px !important;
  max-height: 180px !important;
  object-fit: contain;
  border-radius: 6px;
  display: block;
  margin-left: 0;
  margin-top: 0;
  border: 1px solid #f0f2f5;
}

.image-wrapper-ratio-square {
  width: 180px;
  height: 180px;
  position: relative;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #f0f2f5;
}

.image-wrapper-ratio-square .post-image-item-dynamic-v2-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ai-report-embed {
  background: linear-gradient(145deg, #fffcf5 0%, #fff8e6 100%);
  border: 1px solid #faecd8;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.report-header {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #E6A23C;
  font-weight: bold;
  margin-bottom: 12px;
  font-size: 14px;
}

.report-body {
  display: flex;
  align-items: center;
  gap: 20px;
}

.report-score {
  color: #E6A23C;
  font-size: 12px;
}

.score-num {
  font-size: 36px;
  font-weight: 900;
  line-height: 1;
  font-style: italic;
}

.report-details {
  flex: 1;
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}

.report-comment {
  margin-top: 4px;
  color: #909399;
  font-style: italic;
}

.post-footer {
  display: flex;
  padding-left: 52px;
  gap: 40px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.interaction-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
}

.interaction-btn:hover {
  color: #409EFF;
}

.interaction-btn.is-liked:hover {
  transform: scale(1.1);
}

.heart-icon {
  transition: fill 0.3s ease;
}

.interaction-btn.is-favorited {
  color: #ff9900;
}

.interaction-btn.is-favorited:hover {
  transform: scale(1.1);
}
</style>
