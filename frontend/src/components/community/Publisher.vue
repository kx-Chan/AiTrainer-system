<template>
  <el-card ref="rootCardRef" class="publisher-card" shadow="never">
    <div class="publisher-layout">
      <el-avatar :size="48" :src="viewerAvatarSrc" class="publisher-avatar" style="cursor: pointer;" @click="goToMine">
        <el-icon>
          <UserFilled />
        </el-icon>
      </el-avatar>
      <div class="publisher-input-area">
        <div v-if="selectedTopic" class="publisher-topic-bar">
          <el-tag size="small" effect="plain" round closable @close="clearTopic">#{{ selectedTopic }}</el-tag>
        </div>

        <div v-if="selectedAiReport" class="publisher-topic-bar">
          <el-tag size="small" effect="plain" round closable @close="clearAiReport">
            已附加 AI 战报 · {{ aiReportLabel }}
          </el-tag>
        </div>

        <el-input v-model="newPostText" type="textarea" :rows="3" placeholder="分享你的训练心得，或者晒出今天的 AI 战报..." resize="none"
          maxlength="200" show-word-limit />

        <div class="publisher-actions">
          <div class="action-icons">
            <el-upload class="post-uploader" :http-request="handleImageUpload" :on-remove="handleImageRemove"
              :file-list="uploadedFileList" :limit="9" multiple accept="image/*" list-type="picture-card">
              <el-icon>
                <Picture />
              </el-icon>
            </el-upload>

            <el-popover v-model:visible="aiReportPopoverVisible" placement="bottom-start" :width="360" trigger="click"
              @show="ensureReportsLoaded">
              <div class="report-popover">
                <div class="report-popover-title">选择要带上的 AI 战报</div>
                <div class="report-list" v-loading="reportsLoading">
                  <div v-if="myReports.length" class="report-items">
                    <div v-for="r in myReports" :key="r.id" class="report-item"
                      :class="{ 'is-selected': String(selectedAiReport?.id) === String(r.id) }" @click="selectAiReport(r)">
                      <div class="report-item-left">
                        <div class="report-item-name">{{ r.workoutName || '训练战报' }}</div>
                        <div class="report-item-meta">{{ r.createdAtText }}</div>
                      </div>
                      <div class="report-item-right">
                        <el-tag type="success" size="small" effect="light">{{ r.score }}分</el-tag>
                      </div>
                    </div>
                  </div>
                  <el-empty v-else description="暂无 AI 战报" />
                </div>
                <div class="report-popover-actions">
                  <el-button size="small" @click="clearAiReport">清空</el-button>
                  <el-button size="small" type="primary" :loading="reportsLoading" @click="refreshReports">刷新</el-button>
                </div>
              </div>
              <template #reference>
                <el-button link type="primary">
                  <el-icon size="18">
                    <Trophy />
                  </el-icon> 战报
                </el-button>
              </template>
            </el-popover>

            <el-popover v-model:visible="isTopicPopoverVisible" placement="bottom-start" :width="320" trigger="click">
              <div class="topic-popover">
                <div class="topic-suggest-title">推荐话题</div>
                <div class="topic-suggest-list">
                  <el-tag v-for="name in recommendedTopics" :key="name" class="topic-suggest-tag" effect="plain" round
                    @click="selectTopic(name)">
                    #{{ name }}
                  </el-tag>
                </div>
                <el-divider style="margin: 12px 0;" />
                <el-input v-model="customTopic" placeholder="自定义话题（不需要输入#）" maxlength="20" clearable
                  @keyup.enter="applyCustomTopic" />
                <div class="topic-popover-actions">
                  <el-button size="small" @click="clearTopic">清空</el-button>
                  <el-button size="small" type="primary" @click="applyCustomTopic">使用话题</el-button>
                </div>
              </div>
              <template #reference>
                <el-button link type="primary">
                  <el-icon size="18">
                    <CollectionTag />
                  </el-icon> 话题
                </el-button>
              </template>
            </el-popover>
          </div>

          <el-button type="primary" round class="publish-btn" :loading="isPublishing"
            :disabled="!newPostText.trim() && !selectedAiReport" @click="publishPost">
            发布动态
          </el-button>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { CollectionTag, Picture, Trophy, UserFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/userStore'
import { DEFAULT_AVATAR_URL } from '@/store/userStore'
import { workoutApi } from '@/api/workout'

const props = defineProps({
  recommendedTopics: { type: Array, default: () => [] },
  device: { type: String, default: 'Web 端' }
})

const emit = defineEmits(['published', 'go-to-space'])

const userStore = useUserStore()
const { avatar, nickname } = storeToRefs(userStore)
const viewerId = computed(() => userStore.userId)
const viewerAvatar = computed(() => avatar.value)
const route = useRoute()

const normalizeAvatarSrc = (raw) => {
  const s = String(raw ?? '').trim()
  if (!s) return ''
  const lowered = s.toLowerCase()
  if (lowered === 'null' || lowered === 'undefined') return ''
  return s
}

const viewerAvatarSrc = computed(() => normalizeAvatarSrc(viewerAvatar.value) || DEFAULT_AVATAR_URL)

const rootCardRef = ref(null)
const rootEl = computed(() => rootCardRef.value?.$el || null)
defineExpose({ rootEl })

const newPostText = ref('')
const isPublishing = ref(false)
const isTopicPopoverVisible = ref(false)
const selectedTopic = ref('')
const customTopic = ref('')
const uploadedImages = ref([])
const uploadedFileList = ref([])

const aiReportPopoverVisible = ref(false)
const reportsLoading = ref(false)
const myReports = ref([])
const selectedAiReport = ref(null)

const formatDateTime = (raw) => {
  const s = String(raw || '').trim()
  if (!s) return ''
  const normalized = s.replace('T', ' ')
  return normalized.length > 16 ? normalized.slice(0, 16) : normalized
}

const normalizeReportItem = (raw) => ({
  id: raw?.id,
  workoutId: raw?.workoutId ?? raw?.workout_id ?? '',
  workoutName: raw?.workoutName ?? raw?.workout_name ?? raw?.type ?? raw?.name ?? '',
  score: Number(raw?.score ?? 0),
  validReps: Number(raw?.validReps ?? raw?.valid_reps ?? 0),
  invalidReps: Number(raw?.invalidReps ?? raw?.invalid_reps ?? 0),
  durationSeconds: Number(raw?.durationSeconds ?? raw?.duration_seconds ?? 0),
  durationMinutes: Number(raw?.durationMinutes ?? raw?.duration_minutes ?? 0),
  caloriesBurned: Number(raw?.caloriesBurned ?? raw?.calories_burned ?? 0),
  comment: raw?.comment || '',
  createdAt: raw?.createdAt ?? raw?.created_at ?? raw?.createTime ?? raw?.create_time ?? '',
  createdAtText: formatDateTime(raw?.createdAt ?? raw?.created_at ?? raw?.createTime ?? raw?.create_time ?? '')
})

const ensureReportsLoaded = async () => {
  if (reportsLoading.value) return
  if (myReports.value.length) return
  await refreshReports()
}

const refreshReports = async () => {
  reportsLoading.value = true
  try {
    const data = await workoutApi.listMySessions({ page: 1, size: 20 })
    const records = Array.isArray(data?.records) ? data.records : (Array.isArray(data) ? data : [])
    myReports.value = records.map(normalizeReportItem).filter(x => x?.id != null)
  } finally {
    reportsLoading.value = false
  }
}

const selectAiReport = (r) => {
  selectedAiReport.value = r || null
  aiReportPopoverVisible.value = false
}

const clearAiReport = () => {
  selectedAiReport.value = null
}

const aiReportLabel = computed(() => {
  const r = selectedAiReport.value
  if (!r) return ''
  const name = String(r.workoutName || '').trim() || '训练战报'
  const score = Number(r.score ?? 0)
  return `${name} · ${score}分`
})

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
    uploadedImages.value = [...uploadedImages.value, { key: data.key, url: data.url }]
    uploadedFileList.value = [...uploadedFileList.value, { name: file.name, url: data.url, response: data }]
    onSuccess && onSuccess(data)
  } catch (e) {
    onError && onError(e)
  }
}

const handleImageRemove = (file, fileList) => {
  uploadedFileList.value = fileList
  const resp = file?.response || {}
  if (resp?.key) {
    uploadedImages.value = uploadedImages.value.filter(x => x.key !== resp.key)
  } else if (file?.url) {
    uploadedImages.value = uploadedImages.value.filter(x => x.url !== file.url)
  }
}

const publishPost = async () => {
  const content = newPostText.value.trim()
  const hasReport = Boolean(selectedAiReport.value?.id)
  const finalContent = content || (hasReport ? '分享了一份 AI 战报' : '')
  if (!finalContent) return
  if (isPublishing.value) return

  try {
    isPublishing.value = true
    const created = await request.post('/posts', {
      content: finalContent,
      topic: selectedTopic.value || null,
      device: props.device,
      imageKeys: uploadedImages.value.map(x => x.key),
      aiReportId: selectedAiReport.value?.id ?? null
    })

    const newPost = {
      id: created?.id ?? Date.now(),
      author: created?.author || nickname.value || '用户',
      authorId: viewerId.value,
      avatar: normalizeAvatarSrc(created?.avatar) || viewerAvatarSrc.value,
      time: created?.time || '刚刚',
      device: created?.device || props.device,
      isPro: !!created?.isPro,
      isFollowing: created?.isFollowing ?? true,
      topic: created?.topic || (selectedTopic.value ? `#${selectedTopic.value}` : ''),
      content: created?.content || finalContent,
      images: created?.images || uploadedImages.value.map(x => x.url),
      likes: created?.likes ?? 0,
      comments: created?.comments ?? 0,
      isLiked: created?.isLiked ?? false,
      favorites: created?.favorites ?? 0,
      isFavorited: created?.isFavorited ?? false,
      aiReport: created?.aiReport ?? (selectedAiReport.value ? { ...selectedAiReport.value } : null)
    }

    emit('published', newPost)
    newPostText.value = ''
    clearTopic()
    clearAiReport()
    uploadedImages.value = []
    uploadedFileList.value = []
    ElMessage.success('发布成功！')
  } finally {
    isPublishing.value = false
  }
}

const applyShareReportFromRoute = async (idRaw) => {
  const id = String(idRaw || '').trim()
  if (!id) return
  try {
    const data = await workoutApi.getSession(id)
    const normalized = normalizeReportItem(data || {})
    if (normalized?.id != null) selectedAiReport.value = normalized
  } catch (e) { }
}

onMounted(() => {
  const id = route.query?.shareReportId
  if (id) applyShareReportFromRoute(id)
})

watch(() => route.query?.shareReportId, (n, o) => {
  const next = String(n || '').trim()
  const prev = String(o || '').trim()
  if (!next || next === prev) return
  applyShareReportFromRoute(next)
})

watch(aiReportPopoverVisible, (v) => {
  if (v) ensureReportsLoaded()
})

const goToMine = () => {
  if (!viewerId.value) return
  emit('go-to-space', viewerId.value)
}
</script>

<style scoped>
.publisher-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.publisher-layout {
  display: flex;
  gap: 16px;
}

.publisher-input-area {
  flex: 1;
}

.publisher-topic-bar {
  margin-bottom: 10px;
}

.publisher-input-area :deep(.el-textarea__inner) {
  border: none;
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  box-shadow: none;
}

.publisher-input-area :deep(.el-textarea__inner:focus) {
  background-color: #fff;
  box-shadow: 0 0 0 1px #409EFF inset;
}

.publisher-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.action-icons {
  display: flex;
  gap: 8px;
}

.topic-popover {
  display: flex;
  flex-direction: column;
}

.topic-suggest-title {
  font-weight: 700;
  color: #303133;
  margin-bottom: 10px;
}

.topic-suggest-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-suggest-tag {
  cursor: pointer;
}

.topic-popover-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

.report-popover {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.report-popover-title {
  font-weight: 700;
  color: #303133;
}

.report-list {
  max-height: 280px;
  overflow: auto;
}

.report-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.report-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.2s, border-color 0.2s;
}

.report-item:hover {
  background-color: #f5f7fa;
}

.report-item.is-selected {
  border-color: #409EFF;
  background-color: #ecf5ff;
}

.report-item-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.report-item-name {
  font-weight: 700;
  color: #303133;
}

.report-item-meta {
  font-size: 12px;
  color: #909399;
}

.report-popover-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

:deep(.post-uploader .el-upload--picture-card),
:deep(.post-uploader .el-upload-list--picture-card .el-upload-list__item) {
  width: 80px !important;
  height: 80px !important;
  border-radius: 8px;
}

:deep(.post-uploader .el-upload--picture-card .el-icon) {
  font-size: 20px;
  color: #8c939d;
}

:deep(.post-uploader .el-upload--picture-card) {
  line-height: 90px;
}

@media (max-width: 768px) {
  .publisher-avatar {
    width: 48px;
    height: 48px;
    flex: 0 0 48px;
  }

  .publisher-avatar :deep(img) {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .publisher-avatar :deep(.el-avatar) {
    overflow: hidden;
  }
}
</style>
