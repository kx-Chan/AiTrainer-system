<template>
  <el-card class="post-card" shadow="hover">
    <div class="post-header">
      <el-avatar :size="40" :src="authorAvatarSrc" style="cursor: pointer;" @click="$emit('go-to-space', post.authorId)">
        <el-icon>
          <UserFilled />
        </el-icon>
      </el-avatar>

      <div class="post-user-info">
        <div class="user-name" style="cursor: pointer;" @click="$emit('go-to-space', post.authorId)">
          {{ post.author }}
          <el-tag v-if="post.isPro" type="warning" size="small" effect="dark" round class="pro-tag">PRO</el-tag>
        </div>
        <div class="post-time">{{ post.time?.substring(0, 10) }} · 来自 {{ post.device }}</div>
      </div>

      <template v-if="Number(post.authorId) !== Number(viewerUserId)">
        <el-button v-if="!post.isFollowing" size="small" round plain type="primary" class="follow-btn"
          :loading="Number(followLoadingId) === Number(post.authorId)" @click="$emit('follow', post)">
          + 关注
        </el-button>

        <el-dropdown v-else trigger="click" popper-class="custom-unfollow-dropdown">
          <el-button size="small" round class="follow-btn" :loading="Number(followLoadingId) === Number(post.authorId)">
            已关注
            <el-icon style="margin-left: 4px;">
              <ArrowDown />
            </el-icon>
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

      <div v-if="post.images && post.images.length" :class="{
        'post-images-dynamic-v2': true,
        'is-single': post.images.length === 1,
        'is-grid-2': post.images.length === 2,
        'is-grid-multi': post.images.length >= 3
      }">
        <template v-if="post.images.length === 1">
          <el-image :src="post.images[0]" fit="contain" :preview-src-list="post.images"
            class="post-image-item-dynamic-v2-single" />
        </template>

        <template v-else>
          <div v-for="(img, idx) in post.images" :key="idx" class="image-wrapper-ratio-square">
            <el-image :src="img" fit="cover" :preview-src-list="post.images" :initial-index="idx"
              class="post-image-item-dynamic-v2-grid" />
          </div>
        </template>
      </div>

      <div v-if="post.aiReport || aiReportId" class="ai-report-embed">
        <div class="report-header">
          <el-icon color="#E6A23C" size="18">
            <Trophy />
          </el-icon>
          <span>AiTrainer 智能评测战报</span>
        </div>
        <div class="report-body">
          <template v-if="post.aiReport">
            <div class="report-score"><span class="score-num">{{ post.aiReport.score }}</span>分</div>
            <div class="report-details">
              <div>动作：<strong>{{ reportActionText }}</strong></div>
              <div>次数：有效 <strong>{{ post.aiReport.validReps ?? 0 }}</strong> / 异常 <strong>{{ post.aiReport.invalidReps ??
                0
              }}</strong></div>
              <div>时长：<strong>{{ reportDurationText }}</strong> · 消耗：🔥 {{ reportCaloriesText }}</div>
              <div class="report-comment">AI点评：{{ post.aiReport.comment }}</div>
            </div>
          </template>
          <template v-else>
            <el-skeleton v-if="aiReportLoading" animated :rows="3" />
            <div v-else class="report-details">
              <div v-if="aiReportLoadFailed">
                战报加载失败
                <el-button link type="primary" @click="loadAiReport">重试</el-button>
              </div>
              <div v-else>战报加载中...</div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <div class="post-footer">
      <el-tooltip content="点赞" placement="top" :show-after="500">
        <div class="interaction-btn" :class="{ 'is-liked': post.isLiked }" @click="$emit('toggle-like', post)">
          <svg t="1711545600" class="icon heart-icon" viewBox="0 0 1024 1024" version="1.1"
            xmlns="http://www.w3.org/2000/svg" p-id="4245" width="20" height="20">
            <path
              d="M512 896a42.667 42.667 0 0 1-30.293-12.373l-306.347-306.347c-80-80-80-210.347 0-290.347a205.333 205.333 0 0 1 290.347 0l46.293 46.293 46.293-46.293a205.333 205.333 0 0 1 290.347 0c80 80 80 210.347 0 290.347l-306.347 306.347A42.667 42.667 0 0 1 512 896z"
              :fill="post.isLiked ? '#f56c6c' : 'currentColor'" p-id="4246"></path>
          </svg>
          <span :style="{ color: post.isLiked ? '#f56c6c' : '' }">{{ post.likes }}</span>
        </div>
      </el-tooltip>

      <el-tooltip v-if="!isReportOnly" content="添加到收藏夹" placement="top" :show-after="500">
        <div class="interaction-btn" :class="{ 'is-favorited': post.isFavorited }" @click="openFavoriteDialog">
          <el-icon size="20">
            <component :is="post.isFavorited ? 'StarFilled' : 'Star'" :color="post.isFavorited ? '#ff9900' : ''" />
          </el-icon>
          <span :style="{ color: post.isFavorited ? '#ff9900' : '' }">{{ post.favorites ?? 0 }}</span>
        </div>
      </el-tooltip>

      <el-tooltip v-if="!isReportOnly" content="查看评论" placement="top" :show-after="500">
        <div class="interaction-btn" @click="toggleComments">
          <el-icon size="20">
            <ChatDotRound />
          </el-icon>
          <span>{{ post.comments }}</span>
        </div>
      </el-tooltip>
    </div>

    <el-dialog v-if="!isReportOnly" v-model="favoriteDialogVisible" title="添加到收藏夹" width="420px"
      @open="loadFoldersAndSelection">
      <div class="favorite-dialog-body" v-loading="foldersLoading">
        <div v-if="folders.length" class="folder-list">
          <div v-for="f in folders" :key="f.id" class="folder-item"
            :class="{ 'is-selected': selectedFolderIdSet.has(String(f.id)) }" @click="togglePostInFolder(f)">
            <div class="folder-left">
              <el-icon size="18">
                <Folder />
              </el-icon>
              <div class="folder-meta">
                <div class="folder-name">
                  <span>{{ f.name }}</span>
                  <el-tag v-if="Number(f.isDefault) === 1" type="warning" size="small" effect="light" round>默认</el-tag>
                </div>
                <div class="folder-desc">{{ Number(f.isPublic) === 1 ? '公开' : '私密' }}</div>
              </div>
            </div>
            <el-icon v-if="selectedFolderIdSet.has(String(f.id))" size="18" color="#67C23A">
              <CircleCheckFilled />
            </el-icon>
          </div>
        </div>
        <el-empty v-else description="你还没有收藏夹" />

        <div class="folder-create">
          <template v-if="isCreatingFolder">
            <el-input v-model="newFolderName" placeholder="请输入收藏夹名称" maxlength="20" show-word-limit
              @keyup.enter="createFolder" style="margin-bottom: 12px;" />

            <div style="margin-bottom: 15px; display: flex; align-items: center; justify-content: space-between;">
              <span style="font-size: 13px; color: #606266;">是否公开：</span>
              <el-switch v-model="newFolderIsPublic" :active-value="1" :inactive-value="0" active-text="公开"
                inactive-text="私密" inline-prompt />
            </div>

            <div class="create-actions">
              <el-button type="primary" :loading="creatingFolderLoading" @click="createFolder">创建</el-button>
              <el-button @click="cancelCreateFolder">取消</el-button>
            </div>
          </template>
          <el-button v-else type="primary" plain :icon="Plus" @click="startCreateFolder">新建收藏夹</el-button>
        </div>
      </div>
    </el-dialog>

    <CommentSection v-if="showComments && !isReportOnly" :post-id="post.id" :post-author-id="post.authorId"
      :viewer-user-id="viewerUserId" :viewer-avatar="viewerAvatar" @comment-added="$emit('comment-added', $event)"
      @comment-deleted="$emit('comment-deleted', $event)" @go-to-space="$emit('go-to-space', $event)" />
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown, ChatDotRound, Folder, Plus, Trophy, CircleCheckFilled, UserFilled } from '@element-plus/icons-vue'
import CommentSection from './CommentSection.vue'
import { DEFAULT_AVATAR_URL } from '@/store/userStore'

// ✅ 1. 导入封装好的 API
import { folderApi, itemApi } from '@/api/collection'
import { workoutApi } from '@/api/workout'

const props = defineProps({
  post: { type: Object, required: true },
  viewerUserId: { type: [Number, String], required: true },
  viewerAvatar: { type: String, default: '' },
  followLoadingId: { type: [Number, String, null], default: null }
})

const emit = defineEmits(['go-to-space', 'follow', 'unfollow', 'toggle-like', 'topic-click', 'comment-added', 'comment-deleted', 'favorite-changed'])

const showComments = ref(false)

const isReportOnly = computed(() => String(props.post?.sourceType || '') === 'workout_report')

const normalizeAvatarSrc = (raw) => {
  const s = String(raw ?? '').trim()
  if (!s) return ''
  const lowered = s.toLowerCase()
  if (lowered === 'null' || lowered === 'undefined') return ''
  return s
}

const authorAvatarSrc = computed(() => normalizeAvatarSrc(props.post?.avatar) || DEFAULT_AVATAR_URL)

const aiReportId = computed(() => {
  const p = props.post || {}
  return p?.aiReportId ?? p?.ai_report_id ?? p?.aiReportID ?? p?.ai_reportId ?? p?.workoutSessionId ?? null
})

const aiReportLoading = ref(false)
const aiReportLoadFailed = ref(false)

const normalizeAiReport = (raw) => ({
  id: raw?.id,
  workoutId: raw?.workoutId ?? raw?.workout_id ?? '',
  action: raw?.action ?? raw?.type ?? '',
  score: Number(raw?.score ?? 0),
  validReps: Number(raw?.validReps ?? raw?.valid_reps ?? 0),
  invalidReps: Number(raw?.invalidReps ?? raw?.invalid_reps ?? 0),
  durationSeconds: Number(raw?.durationSeconds ?? raw?.duration_seconds ?? raw?.durationMinutes ?? raw?.duration_minutes ?? 0),
  caloriesBurned: Number(raw?.caloriesBurned ?? raw?.calories_burned ?? raw?.calories ?? 0),
  comment: raw?.comment || ''
})

const loadAiReport = async () => {
  if (isReportOnly.value) return
  if (props.post?.aiReport) return
  const id = aiReportId.value
  if (id == null || String(id).trim() === '') return
  if (aiReportLoading.value) return

  aiReportLoading.value = true
  aiReportLoadFailed.value = false
  try {
    const data = await workoutApi.getSession(String(id))
    props.post.aiReport = data ? normalizeAiReport(data) : null
  } catch (e) {
    aiReportLoadFailed.value = true
  } finally {
    aiReportLoading.value = false
  }
}

const formattedTime = computed(() => {
  const t = props.post?.time
  if (!t) return ''
  const s = String(t)
  return s.length > 16 ? s.slice(0, 16) : s
})

const WORKOUT_NAME_MAP = Object.freeze({
  squat: '深蹲',
  pushup: '俯卧撑',
  plank: '平板支撑',
  situp: '卷腹',
  lunge: '箭步蹲',
  good_morning: '早安式体前屈'
})

const reportActionText = computed(() => {
  const direct = String(props.post?.aiReport?.action || '').trim()
  if (direct) return direct
  const workoutId = String(props.post?.aiReport?.workoutId || '').trim()
  if (!workoutId) return '未知动作'
  return WORKOUT_NAME_MAP[workoutId] ? `${WORKOUT_NAME_MAP[workoutId]}（${workoutId}）` : workoutId
})

const reportDurationText = computed(() => {
  const r = props.post?.aiReport || {}
  const totalSeconds = Number(r?.durationSeconds ?? r?.duration_seconds ?? 0)
  if (totalSeconds <= 0) return '0'
  const minutes = Math.floor(totalSeconds / 60)
  const secs = totalSeconds % 60
  if (minutes > 0 && secs > 0) return `${minutes}分${secs}秒`
  if (minutes > 0) return `${minutes}分`
  return `${secs}秒`
})

const reportCaloriesText = computed(() => {
  const r = props.post?.aiReport || {}
  const kcal = Number(r?.caloriesBurned ?? r?.calories_burned ?? r?.calories ?? 0)
  return `${kcal} kcal`
})

const onTopicClick = () => {
  const topic = props.post?.topic
  if (!topic) return
  emit('topic-click', topic)
}

watch(aiReportId, () => {
  if (props.post?.aiReport) return
  if (aiReportId.value == null) return
  loadAiReport()
}, { immediate: true })

const toggleComments = () => {
  showComments.value = !showComments.value
}

const favoriteDialogVisible = ref(false)
const foldersLoading = ref(false)
const folders = ref([])
const selectedFolderIdSet = ref(new Set())

// 🟢 变量声明（只保留这一份）
const isCreatingFolder = ref(false)
const creatingFolderLoading = ref(false)
const newFolderName = ref('')

const postId = computed(() => props.post?.id)
const userId = computed(() => props.viewerUserId)
const newFolderIsPublic = ref(0) // 0 表示私密，1 表示公开

const openFavoriteDialog = () => {
  if (isReportOnly.value) return
  favoriteDialogVisible.value = true
}

// ✅ 2. 加载逻辑
const loadFoldersAndSelection = async () => {
  if (isReportOnly.value) return
  if (!postId.value) return
  foldersLoading.value = true
  try {
    const [folderList, folderIds] = await Promise.all([
      folderApi.list(),
      itemApi.getInFolderIds(postId.value)
    ])
    folders.value = Array.isArray(folderList) ? folderList : []
    const ids = Array.isArray(folderIds) ? folderIds : []
    selectedFolderIdSet.value = new Set(ids.map(x => String(x)))
  } catch (e) {
    console.error("加载收藏信息失败", e)
  } finally {
    foldersLoading.value = false
  }
}

// ✅ 3. 同步状态逻辑
const syncFavoritedState = async () => {
  if (isReportOnly.value) return
  if (!postId.value) return
  try {
    const isFavorited = await itemApi.checkFavorited(postId.value)
    props.post.isFavorited = isFavorited
  } catch (e) {
    console.error("同步收藏状态失败", e)
  }
}

// ✅ 4. 收藏切换逻辑
const togglePostInFolder = async (folder) => {
  if (isReportOnly.value) return
  const folderId = folder?.id
  if (!folderId || !postId.value) return

  const beforeAny = selectedFolderIdSet.value.size > 0
  const key = String(folderId)
  const isSelected = selectedFolderIdSet.value.has(key)

  foldersLoading.value = true
  try {
    let resData;
    if (isSelected) {
      resData = await itemApi.remove(postId.value, folderId)
      ElMessage.success(`已从「${folder?.name}」移除`)
    } else {
      resData = await itemApi.add(postId.value, folderId)
      ElMessage.success(`已收藏到「${folder?.name}」`)
    }

    const folderIds = resData?.folderIds || []
    selectedFolderIdSet.value = new Set(folderIds.map(x => String(x)))

    const afterAny = selectedFolderIdSet.value.size > 0
    props.post.isFavorited = afterAny

    const beforeCount = Number(props.post.favorites ?? 0)
    const delta = beforeAny === afterAny ? 0 : (afterAny ? 1 : -1)
    const nextCount = Math.max(0, beforeCount + delta)
    props.post.favorites = nextCount

    emit('favorite-changed', { postId: postId.value, isFavorited: afterAny, favorites: nextCount })
  } catch (e) {
    console.error("收藏操作失败", e)
  } finally {
    foldersLoading.value = false
  }
}

// 🟢 下面是控制新建收藏夹显隐的逻辑
const startCreateFolder = () => {
  isCreatingFolder.value = true
  newFolderName.value = ''
}

const cancelCreateFolder = () => {
  isCreatingFolder.value = false
  newFolderName.value = ''
}

// ✅ 5. 核心创建逻辑（只保留这一份，带自动收藏功能）
const createFolder = async () => {
  if (isReportOnly.value) return
  const name = String(newFolderName.value || '').trim()
  if (!name) {
    ElMessage.warning('收藏夹名称不能为空')
    return
  }

  creatingFolderLoading.value = true
  try {
    // 1. 发送请求，带上公开性参数
    const created = await folderApi.create({
      name,
      isPublic: newFolderIsPublic.value
    })

    if (created) {
      // 2. 提示成功
      ElMessage({
        message: '收藏夹创建成功',
        type: 'success',
        duration: 2000
      })

      // 3. 自动将当前推文存入新收藏夹（可选，建议加上，体验极佳）
      await togglePostInFolder(created)

      // 4. 重新加载列表，这样新收藏夹就会立刻出现在弹窗里
      await loadFoldersAndSelection()

      // 5. 重置状态
      isCreatingFolder.value = false
      newFolderName.value = ''
      newFolderIsPublic.value = 0 // 重置为默认私密
    }
  } catch (e) {
    console.error("创建失败", e)
    // 这里通常全局拦截器会弹窗，如果没有，手动加一个
    ElMessage.error('创建收藏夹失败，请稍后重试')
  } finally {
    creatingFolderLoading.value = false
  }
}

onMounted(() => {
  if (!isReportOnly.value) syncFavoritedState()
})

watch([postId, userId], () => {
  if (!isReportOnly.value) syncFavoritedState()
})
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

.favorite-dialog-body {
  min-height: 120px;
}

.folder-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.folder-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.folder-item:hover {
  border-color: #c6e2ff;
  background: #f5faff;
}

.folder-item.is-selected {
  border-color: #b3e19d;
  background: #f0f9eb;
}

.folder-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.folder-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.folder-name {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
  font-weight: 600;
}

.folder-desc {
  font-size: 12px;
  color: #909399;
}

.folder-create {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.create-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>
