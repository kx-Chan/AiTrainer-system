<template>
  <el-card ref="rootCardRef" class="publisher-card" shadow="never">
    <div class="publisher-layout">
      <el-avatar :size="48" :src="viewerAvatar" class="publisher-avatar" style="cursor: pointer;" @click="goToMine" />
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
              list-type="picture-card"
            >
              <el-icon><Picture /></el-icon>
            </el-upload>

            <el-popover v-model:visible="isTopicPopoverVisible" placement="bottom-start" :width="320" trigger="click">
              <div class="topic-popover">
                <div class="topic-suggest-title">推荐话题</div>
                <div class="topic-suggest-list">
                  <el-tag
                    v-for="name in recommendedTopics"
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
                <el-button link type="primary">
                  <el-icon size="18"><CollectionTag /></el-icon> 话题
                </el-button>
              </template>
            </el-popover>
          </div>

          <el-button
            type="primary"
            round
            class="publish-btn"
            :loading="isPublishing"
            :disabled="!newPostText.trim()"
            @click="publishPost"
          >
            发布动态
          </el-button>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { CollectionTag, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/userStore'

const props = defineProps({
  recommendedTopics: { type: Array, default: () => [] },
  device: { type: String, default: 'Web 端' }
})

const emit = defineEmits(['published', 'go-to-space'])

const userStore = useUserStore()
const { avatar, nickname } = storeToRefs(userStore)
const viewerId = computed(() => userStore.userId)
const viewerAvatar = computed(() => avatar.value)

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
  if (!content) return
  if (isPublishing.value) return

  try {
    isPublishing.value = true
    const created = await request.post('/posts', {
      content,
      topic: selectedTopic.value || null,
      device: props.device,
      imageKeys: uploadedImages.value.map(x => x.key)
    })

    const newPost = {
      id: created?.id ?? Date.now(),
      author: created?.author || nickname.value || '用户',
      authorId: viewerId.value,
      avatar: created?.avatar || viewerAvatar.value,
      time: created?.time || '刚刚',
      device: created?.device || props.device,
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

    emit('published', newPost)
    newPostText.value = ''
    clearTopic()
    uploadedImages.value = []
    uploadedFileList.value = []
    ElMessage.success('发布成功！')
  } finally {
    isPublishing.value = false
  }
}

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
</style>
