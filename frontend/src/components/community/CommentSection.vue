<template>
  <div class="comment-panel">
    <div class="comment-input-section">
      <el-avatar :size="32" :src="viewerAvatar" />
      <div class="input-wrapper">
        <el-input
          v-model="input"
          type="textarea"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="友善评论，文明发言..."
          resize="none"
          @keydown.enter.exact.prevent="submit"
          class="custom-comment-input"
        />
        <div class="input-footer">
          <span class="hint">Enter 发送 / Shift + Enter 换行</span>
          <el-button size="small" type="primary" round :loading="submitting" :disabled="!input.trim()" @click="submit">
            发布
          </el-button>
        </div>
      </div>
    </div>

    <div class="comment-list">
      <div v-for="c in comments" :key="c.id" class="comment-item-wrapper">
        <el-avatar :size="36" :src="c.avatar" style="cursor: pointer;" @click="$emit('go-to-space', c.userId)" />
        <div class="comment-content-box">
          <div class="comment-meta">
            <span class="comment-author" style="cursor: pointer;" @click="$emit('go-to-space', c.userId)">{{ c.author }}</span>
            <el-tag v-if="String(c.userId) === String(postAuthorId)" size="small" effect="plain" class="author-tag">作者</el-tag>
            <span class="comment-time">{{ c.time }}</span>
          </div>
          <div class="comment-text">{{ c.content }}</div>
          <div class="comment-actions">
            <el-button link size="small">回复</el-button>
          </div>
        </div>
      </div>

      <div v-if="hasMore" class="load-more-comments">
        <el-button text size="small" :loading="loadingMore" @click="loadMore">
          查看更多评论 <el-icon><ArrowDown /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  postId: { type: [Number, String], required: true },
  postAuthorId: { type: [Number, String], required: true },
  viewerAvatar: { type: String, default: '' }
})

const emit = defineEmits(['comment-added', 'go-to-space'])

const postIdNum = computed(() => Number(props.postId))

const comments = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loadingMore = ref(false)
const submitting = ref(false)
const input = ref('')

const hasMore = computed(() => comments.value.length < total.value)

const fetchPage = async (reset) => {
  if (reset) {
    page.value = 1
    total.value = 0
    comments.value = []
  }
  const data = await request.get(`/posts/${postIdNum.value}/comments`, { params: { page: page.value, size: size.value } })
  total.value = data?.total ?? 0
  const list = data?.records ?? []
  comments.value = reset ? list : [...comments.value, ...list]
}

const loadMore = async () => {
  if (loadingMore.value) return
  if (!hasMore.value) return
  try {
    loadingMore.value = true
    page.value += 1
    await fetchPage(false)
  } finally {
    loadingMore.value = false
  }
}

const submit = async () => {
  const text = input.value.trim()
  if (!text) return
  if (submitting.value) return
  try {
    submitting.value = true
    const c = await request.post(`/posts/${postIdNum.value}/comments`, { content: text })
    comments.value = [c, ...comments.value]
    total.value += 1
    input.value = ''
    emit('comment-added', postIdNum.value)
    ElMessage.success('评论成功')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchPage(true)
})
</script>

<style scoped>
.comment-panel {
  margin-top: 12px;
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 8px;
  border: 1px solid #f0f2f5;
}

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
  width: 100%;
}

.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.author-tag {
  transform: scale(0.8);
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
  white-space: pre-wrap;
  word-break: break-all;
}

.comment-actions {
  margin-top: 4px;
}
</style>
