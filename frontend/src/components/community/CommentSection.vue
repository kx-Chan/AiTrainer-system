<template>
  <div class="comment-panel">
    <div class="comment-input-section">
      <el-avatar :size="32" :src="viewerAvatar" />
      <div class="input-wrapper">
        <div v-if="replyTarget" class="reply-target-bar">
          <span class="reply-hint">正在回复 @{{ replyTarget.author }}<span v-if="replyTarget.content">：{{
            clipText(replyTarget.content, 60) }}</span></span>
          <el-button link type="info" :icon="Close" @click="cancelReply">取消</el-button>
        </div>

        <el-input v-model="input" type="textarea" :autosize="{ minRows: 1, maxRows: 4 }"
          :placeholder="replyTarget ? `回复 @${replyTarget.author}...` : '友善评论，文明发言...'" resize="none"
          @keydown.enter.exact.prevent="submit" class="custom-comment-input" />
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
            <span class="comment-author" style="cursor: pointer;" @click="$emit('go-to-space', c.userId)">{{ c.author
              }}</span>
            <template v-if="c.parentId">
              <span class="reply-label">回复</span>
              <span class="reply-user" :style="{ cursor: getReplyToUserId(c) ? 'pointer' : 'default' }"
                @click="$emit('go-to-space', getReplyToUserId(c))">@{{ getReplyToUserName(c) }}</span>
            </template>
            <el-tag v-if="String(c.userId) === String(postAuthorId)" size="small" effect="plain"
              class="author-tag">作者</el-tag>
            <span class="comment-time">{{ c.time }}</span>
          </div>
          <div v-if="c.parentId && getReplyToContent(c)" class="reply-quote">
            <span class="reply-quote-text">{{ clipText(getReplyToContent(c), 80) }}</span>
          </div>
          <div class="comment-text">{{ c.content }}</div>
          <div class="comment-footer-actions">
            <div class="left-interaction">
              <el-button link size="small" @click="onReply(c)">回复</el-button>
            </div>

            <div class="right-interaction">
              <el-button v-if="canDelete(c)" link type="danger" size="small" :icon="Delete" @click="handleDelete(c.id)"
                class="comment-delete-btn">
                删除
              </el-button>
            </div>
          </div>

        </div>
      </div>

      <div v-if="hasMore" class="load-more-comments">
        <el-button text size="small" :loading="loadingMore" @click="loadMore">
          查看更多评论 <el-icon>
            <ArrowDown />
          </el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { ArrowDown, Delete, Close } from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  postId: { type: [Number, String], required: true },
  postAuthorId: { type: [Number, String], required: true },
  viewerUserId: { type: [Number, String], required: true },
  viewerAvatar: { type: String, default: '' }
})

const emit = defineEmits(['comment-added', 'comment-deleted', 'go-to-space'])

const postIdNum = computed(() => Number(props.postId))

const comments = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loadingMore = ref(false)
const submitting = ref(false)
const input = ref('')

const hasMore = computed(() => comments.value.length < total.value)

const commentById = computed(() => {
  const map = new Map()
  for (const c of comments.value) {
    if (c?.id != null) map.set(String(c.id), c)
  }
  return map
})

const clipText = (text, maxLen = 60) => {
  const s = String(text ?? '').replace(/\s+/g, ' ').trim()
  if (!s) return ''
  if (s.length <= maxLen) return s
  return `${s.slice(0, maxLen)}…`
}

const getReplyToUserName = (comment) => {
  const direct = String(comment?.replyToUserName ?? '').trim()
  if (direct) return direct
  const parentId = comment?.parentId
  if (parentId == null) return '原评论'
  const parent = commentById.value.get(String(parentId))
  const name = String(parent?.author ?? '').trim()
  return name || '原评论'
}

const getReplyToContent = (comment) => {
  const direct = String(comment?.replyToContent ?? '').trim()
  if (direct) return direct
  const parentId = comment?.parentId
  if (parentId == null) return ''
  const parent = commentById.value.get(String(parentId))
  return String(parent?.content ?? '').trim()
}

const getReplyToUserId = (comment) => {
  const direct = comment?.replyToUserId
  if (direct != null && String(direct).trim() !== '') return direct
  const parentId = comment?.parentId
  if (parentId == null) return null
  const parent = commentById.value.get(String(parentId))
  return parent?.userId ?? null
}

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
    const payload = {
      content: text,
      parentId: replyTarget.value ? replyTarget.value.id : null
    }
    const c = await request.post(`/posts/${postIdNum.value}/comments`, payload)
    if (replyTarget.value) {
      c.replyToUserName = replyTarget.value.author
      c.replyToContent = replyTarget.value.content
    }
    comments.value = [c, ...comments.value]
    total.value += 1
    input.value = ''
    emit('comment-added', postIdNum.value)
    ElMessage.success(payload.parentId ? '回复成功' : '评论成功')
  } finally {
    submitting.value = false
  }
}

// 权限判断：我是评论者 OR 我是推文作者
const canDelete = (comment) => {
  // const currentId = String(props.viewerUserId)
  const currentId = String(props.viewerUserId)
  return String(comment.userId) === currentId || String(props.postAuthorId) === currentId
}

// 执行删除
const handleDelete = async (commentId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？删除后无法恢复',
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )

    // 调用你之前写好的后端接口
    await request.delete(`/posts/comments/${commentId}`)

    // 前端本地状态同步：从数组中滤掉这一条
    comments.value = comments.value.filter(c => c.id !== commentId)
    total.value -= 1

    // 通知父组件（PostItem）更新评论计数
    emit('comment-deleted', postIdNum.value)

    ElMessage.success('评论已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// CommentSection.vue 的 script setup
const replyTarget = ref(null) // 存储当前正在回复的评论对象

// 点击“回复”按钮触发
const onReply = (comment) => {
  replyTarget.value = comment
  input.value = '' // 清空输入框
  // 计科细节：自动聚焦到输入框
  const inputEl = document.querySelector('.custom-comment-input textarea')
  if (inputEl) inputEl.focus()
}

// 取消回复模式
const cancelReply = () => {
  replyTarget.value = null
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

/* .comment-actions {
  margin-top: 4px;
} */

/* 新增：新的操作栏容器样式 */
.comment-footer-actions {
  display: flex;
  justify-content: space-between;
  /* 关键：让左侧和右侧分别对齐两端 */
  align-items: center;
  margin-top: 8px;
  /* 保持与评论正文的距离 */
}

/* 左侧按钮组 */
.comment-footer-actions .left-interaction {
  display: flex;
  gap: 12px;
  /* 如果将来有多个按钮，它们之间有间距 */
  align-items: center;
}

/* 右侧删除按钮 */
.comment-delete-btn {
  transition: all 0.3s ease;
  font-size: 13px !important;
  /* 让它比正文稍小一点，不喧宾夺主 */
}

/* 增加点视觉反馈，让删除更有警示感 */
.comment-delete-btn:hover {
  background-color: #fef0f0;
  /* 悬停时淡淡的红色背景 */
  border-radius: 4px;
}

/* ✅ 新增：回复模式下的提示条样式 */
.reply-target-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f0f2f5;
  padding: 4px 12px;
  border-radius: 4px 4px 0 0;
  border: 1px solid #dcdfe6;
  border-bottom: none;
}

.reply-hint {
  font-size: 12px;
  color: #606266;
}

/* ✅ 元数据中的回复文本 */
.reply-label {
  font-size: 13px;
  color: #909399;
  margin: 0 4px;
}

.reply-user {
  font-size: 14px;
  font-weight: 600;
  color: #409EFF;
  cursor: pointer;
}

.reply-quote {
  margin: 4px 0 8px;
  padding: 8px 10px;
  border-left: 3px solid #dcdfe6;
  background: #f6f7f9;
  border-radius: 6px;
}

.reply-quote-text {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
