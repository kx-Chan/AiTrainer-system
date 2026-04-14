<template>
  <div class="profile-container">
    <el-card class="profile-header-card" shadow="never">
      <div class="user-info-wrapper">
        <div class="avatar-section">
          <el-avatar :size="100" :src="userInfo.avatar" />
        </div>

        <div class="info-section">
          <div class="name-row">
            <h2 class="nickname">{{ userInfo.nickname }}</h2>
            <el-tag type="warning" effect="light" round size="small" style="margin-left: 8px;">
              目标: {{ GOAL_LABELS[userInfo.goal] || userInfo.goal || '未设置' }}
            </el-tag>

            <el-button type="primary" :icon="Edit" plain size="small" class="edit-btn"
              @click="openEditModal">编辑资料</el-button>
          </div>
          <p class="bio">{{ userInfo.bio }}</p>

          <div class="stats-row">
            <div class="stat-item clickable" @click="openFollowDialog('following')">
              <span class="stat-value">{{ userInfo.following }}</span>
              <span class="stat-label">关注</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item clickable" @click="openFollowDialog('followers')">
              <span class="stat-value">{{ userInfo.followers }}</span>
              <span class="stat-label">粉丝</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">{{ userInfo.totalLikes }}</span>
              <span class="stat-label">
                累计获赞
                <el-icon class="like-icon">
                  <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                    <path fill="currentColor"
                      d="M512 896a32 32 0 0 1-22.624-9.376l-320-320a320 320 0 1 1 452.624-452.624L512 204.032l90.016-90.032a320 320 0 1 1 452.624 452.624l-320 320A32 32 0 0 1 512 896z">
                    </path>
                  </svg>
                </el-icon>
              </span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="content-card" shadow="never">
      <el-tabs v-model="activeTab" class="custom-tabs">

        <el-tab-pane label="我的推文" name="posts">
          <div v-loading="myPostsLoading" class="post-list">
            <div class="my-posts-search-bar">
              <el-input v-model="myPostsSearchKeyword" placeholder="输入关键字，搜索我的历史动态..." clearable
                @keyup.enter="handleMyPostsSearch" @clear="handleMyPostsSearch">
                <template #prefix>
                  <el-icon>
                    <Search />
                  </el-icon>
                </template>
                <template #append>
                  <el-button @click="handleMyPostsSearch">搜索</el-button>
                </template>
              </el-input>
            </div>

            <el-card v-for="post in myPosts" :key="post.id" class="post-item clickable-post" shadow="hover"
              style="margin-bottom: 12px;" @click="openMyPostInCommunity(post)">
              <div style="display:flex; gap:12px; align-items:center;">
                <el-avatar :size="32" :src="userInfo.avatar" />
                <div style="flex:1;">
                  <div style="font-weight:600;">
                    {{ userInfo.nickname }}
                    <el-tag v-if="userInfo.isPro" type="warning" size="small" effect="dark" round
                      class="pro-tag">PRO</el-tag>
                  </div>
                  <div style="color:#909399; font-size:12px;">
                    {{ formatDate(post.time) }} · {{ post.device || '来自 AiTrainer' }}
                  </div>
                </div>
                <div style="display:flex; align-items:center; gap:10px;">
                  <el-tag :type="post.type === 'AI战报' ? 'warning' : 'info'" size="small">
                    {{ post.type }}
                  </el-tag>
                  <el-button link type="danger" :icon="Delete" :loading="Number(deletingMyPostId) === Number(post.id)"
                    @click.stop="handleDeleteMyPost(post)">删除</el-button>
                </div>
              </div>

              <div style="margin-top:12px; font-size:14px; color:#303133; line-height:1.6;">
                <span v-if="post.topic" style="color:#409EFF; margin-right:4px;">#{{ post.topic }}#</span>
                {{ post.content }}
              </div>
            </el-card>

            <el-empty v-if="myPosts.length === 0" description="你还没有发布过动态，快去社区分享吧！" />

            <div style="display:flex; justify-content:center; margin-top:16px;">
              <el-pagination v-model:current-page="myPostsPage.page" v-model:page-size="myPostsPage.size"
                :total="myPostsPage.total" layout="prev, pager, next" small @current-change="handleMyPostsPageChange" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的足迹" name="footprints">
          <div class="footprint-filters">
            <el-radio-group v-model="footprintFilter" size="medium">
              <el-radio-button label="liked">我赞过的</el-radio-button>
              <el-radio-button label="commented">我评论的</el-radio-button>
            </el-radio-group>
          </div>
          <div class="post-list" style="margin-top: 16px;">
            <el-card v-for="post in footprintPosts" :key="post.id" class="post-item clickable-post" shadow="hover"
              style="margin-bottom: 12px;" @click="openFootprintPostInCommunity(post)">
              <div style="display:flex;gap:12px;align-items:center;">
                <el-avatar :size="32" :src="post.avatar" />
                <div style="flex:1;">
                  <div style="font-weight:600;">{{ post.author }} <el-tag v-if="post.isPro" type="warning" size="small"
                      effect="dark" round class="pro-tag">PRO</el-tag></div>
                  <div style="color:#909399;font-size:12px;">{{ formatDate(post.time) }} · {{ post.device }}</div>
                </div>
              </div>
              <div style="margin-top:8px;">
                <span v-if="post.topic" style="color:#409EFF;margin-right:4px;">{{ post.topic }}</span>{{ post.content
                }}
              </div>
            </el-card>
            <div style="display:flex;justify-content:center;margin-top:12px;">
              <el-pagination v-model:current-page="footprintPage.page" v-model:page-size="footprintPage.size"
                :total="footprintPage.total" layout="prev, pager, next" @current-change="fetchFootprints" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的收藏" name="collections">
          <div class="collection-toolbar">
            <div class="toolbar-left">
              <el-icon class="title-icon">
                <FolderOpened />
              </el-icon>
              <span class="toolbar-title">我的收藏夹</span>
            </div>

            <div class="toolbar-right">
              <el-input v-model="folderSearchKeyword" placeholder="输入名称搜索..." class="search-input-group" clearable
                @keyup.enter="handleFolderSearch" @clear="handleFolderSearch">
                <template #prefix>
                  <el-icon>
                    <Search />
                  </el-icon>
                </template>
                <template #append>
                  <el-button :icon="Search" @click="handleFolderSearch">搜索</el-button>
                </template>
              </el-input>

              <el-button type="primary" class="create-btn" @click="startCreateFolder">
                <el-icon style="margin-right: 4px;">
                  <Plus />
                </el-icon>
                新建收藏夹
              </el-button>
            </div>
          </div>

          <el-row :gutter="20" class="collection-grid">
            <el-col :span="8" v-for="folder in collectionFolders" :key="folder.id">
              <el-card class="folder-card" shadow="hover" @click="goToFolderDetail(folder)">

                <div class="folder-tags">
                  <el-tag v-if="folder.isDefault === 1" size="small" type="warning" effect="dark" round>默认</el-tag>
                  <el-tag :type="folder.isPublic === 1 ? 'success' : 'info'" size="small" effect="plain" round>
                    {{ folder.isPublic === 1 ? '公开' : '私密' }}
                  </el-tag>
                </div>

                <div class="folder-visual">
                  <el-icon class="folder-main-icon">
                    <FolderOpened />
                  </el-icon>
                </div>

                <div class="folder-info">
                  <div class="folder-name">{{ folder.name }}</div>
                  <div class="folder-count">{{ folder.itemCount || 0 }} 篇内容</div>
                </div>

                <div class="folder-footer" @click.stop>
                  <el-button link type="primary" @click.stop="handleEditFolder(folder)">编辑</el-button>

                  <template v-if="folder.isDefault !== 1">
                    <el-divider direction="vertical" />
                    <el-button link type="warning" @click.stop="handleSetDefault(folder)">设为默认</el-button>
                    <el-divider direction="vertical" />
                    <el-button link type="danger" @click.stop="handleDeleteFolder(folder)">删除</el-button>
                  </template>
                </div>

              </el-card>
            </el-col>
          </el-row>

          <el-empty v-if="collectionFolders.length === 0" description="空空如也，快去创建一个收藏夹吧！" />
        </el-tab-pane>

      </el-tabs>
    </el-card>

    <el-dialog v-model="isEditVisible" title="编辑个人资料" width="450px" destroy-on-close class="edit-dialog"
      @close="cancelEdit">
      <el-form label-width="80px" :model="editForm">
        <el-form-item label="用户头像">
          <el-upload class="avatar-uploader" action="#" :show-file-list="false" :auto-upload="false"
            :on-change="handleAvatarChange">
            <img v-if="editForm.avatar" :src="editForm.avatar" class="uploaded-avatar" />
            <el-icon v-else class="avatar-uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
          <div class="upload-tip">点击头像可重新上传本地图片 (支持 jpg/png)</div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="健身目标">
          <el-radio-group v-model="editForm.goal">
            <el-radio-button label="lose">减脂</el-radio-button>
            <el-radio-button label="gain">增肌</el-radio-button>
            <el-radio-button label="maintain">保持</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身高 (cm)">
          <el-input-number v-model="editForm.height" :min="100" :max="250" />
        </el-form-item>
        <el-form-item label="体重 (kg)">
          <el-input-number v-model="editForm.weight" :min="30" :max="200" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="体脂率 (%)">
          <el-input-number v-model="editForm.bodyFat" :min="1" :max="50" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="editForm.age" :min="10" :max="120" />
        </el-form-item>
        <el-form-item label="个性签名">
          <el-input v-model="editForm.bio" type="textarea" :rows="3" placeholder="写一句激励自己的话吧..." maxlength="100"
            show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="isAvatarUploading" @click="saveProfile">保存修改</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="isFollowVisible" :title="followDialogType === 'followers' ? '我的粉丝' : '我的关注'" width="440px">
      <div v-loading="followListLoading" class="follow-list">
        <el-empty v-if="!followListLoading && followList.length === 0" description="暂无数据" />

        <div v-for="user in followList" :key="user.id" class="follow-item">
          <el-avatar :size="44" :src="user.avatar" @click="handleUserClick(user.id)" class="follow-avatar" />
          <div class="follow-info">
            <div class="follow-name" @click="handleUserClick(user.id)">{{ user.name }}</div>
            <div v-if="user.bio" class="follow-bio">{{ user.bio }}</div>
          </div>
          <el-button :type="user.isFollowing ? 'default' : 'primary'" size="small" plain round
            :loading="followActionLoadingId === user.id" @click="toggleFollow(user)">
            {{ user.isFollowing ? '已关注' : '+ 关注' }}
          </el-button>
        </div>
      </div>
      <div style="display: flex; justify-content: center; margin-top: 16px;">
        <el-pagination v-model:current-page="followPage.page" v-model:page-size="followPage.size"
          :total="followPage.total" :pager-count="5" layout="prev, pager, next" @current-change="fetchFollowList" />
      </div>
    </el-dialog>

    <el-dialog v-model="folderDialogVisible" :title="folderDialogMode === 'create' ? '新建收藏夹' : '编辑收藏夹'" width="420px"
      destroy-on-close>
      <el-form label-width="90px" :model="folderForm">
        <el-form-item label="收藏夹名称">
          <el-input v-model="folderForm.name" placeholder="请输入名称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="可见性">
          <el-radio-group v-model="folderForm.isPublic">
            <el-radio-button :label="0">私密</el-radio-button>
            <el-radio-button :label="1">公开</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="folderDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="folderDialogLoading" @click="submitFolder">
            {{ folderDialogMode === 'create' ? '创建' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { Edit, FolderOpened, Plus, Search, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useRouter, useRoute } from 'vue-router'
import { folderApi } from '@/api/collection'


// Profile.vue
const GOAL_LABELS = {
  lose: '减脂降重',
  gain: '增肌塑形',
  maintain: '保持身材'
}
// ================= 1. 核心状态定义 =================

const router = useRouter()
const route = useRoute()

const myPostsSearchKeyword = ref('') // 存储我的推文搜索词

const userInfo = reactive({
  avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
  nickname: '', gender: '', goal: '', bio: '', following: 0, followers: 0, totalLikes: 0, height: null, weight: null, bodyFat: null
})

const activeTab = ref('posts')

// 我的推文
const myPosts = ref([])
const myPostsLoading = ref(false)
const myPostsPage = reactive({ page: 1, size: 10, total: 0 })
const deletingMyPostId = ref(null)

// 我的足迹
const footprintFilter = ref('liked')
const footprintPosts = reactive([])
const footprintPage = reactive({ page: 1, size: 10, total: 0 })

// 弹窗控制
const isEditVisible = ref(false)
const isFollowVisible = ref(false)

// 编辑表单与头像上传
const editForm = reactive({ avatar: '', nickname: '', gender: '', goal: '', bio: '', height: null, weight: null, bodyFat: null, age: null })
const pendingAvatarFile = ref(null)
const pendingAvatarPreviewUrl = ref('')
const isAvatarUploading = ref(false)

// 关注/粉丝
const followDialogType = ref('followers')
const followList = ref([])
const followActionLoadingId = ref(null)
const followPage = reactive({ page: 1, size: 10, total: 0 })
const followListLoading = ref(false)

// ================= 2. 工具函数 =================
const formatDate = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.length > 10 ? timeStr.substring(0, 10) : timeStr
}

const openPostInCommunity = (post, tab) => {
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
  router.push({ name: 'Community', query: { postId: String(post.id), from: 'profile', tab: tab || 'posts' } })
}

const openMyPostInCommunity = (post) => {
  if (!post?.id) return
  const patched = {
    ...post,
    author: post.author || userInfo.nickname || '我',
    avatar: post.avatar || userInfo.avatar,
    authorId: post.authorId || userInfo.id || userInfo.userId || 0,
    device: post.device || 'AiTrainer'
  }
  openPostInCommunity(patched, 'posts')
}

const openFootprintPostInCommunity = (post) => {
  openPostInCommunity(post, 'footprints')
}

const handleUserClick = (userId) => {
  // 计科细节：获取当前登录用户的 ID（假设你在 store 里存了）
  // 如果点的是自己，直接关闭弹窗即可，或者跳到个人主页
  // 这里我们统一走跳转逻辑，路由会自动处理

  // 1. 先关闭当前的关注/粉丝弹窗，防止遮挡跳转后的页面
  isFollowVisible.value = false

  // 2. 执行跳转
  router.push({
    name: 'UserSpace', // 确保你的路由表中这个页面的 name 叫 UserSpace
    params: { id: String(userId) }
  })
}

// ================= 3. 数据抓取逻辑 (API) =================
const fetchProfile = async () => {
  try {
    const data = await request.get('/profile/info')
    if (data) Object.assign(userInfo, data)
  } catch (e) { console.error(e) }
}

const fetchMyPosts = async () => {
  myPostsLoading.value = true
  try {
    const keywordParam = myPostsSearchKeyword.value ? myPostsSearchKeyword.value.trim() : ''
    const data = await request.get('/posts/me', {
      params: {
        page: myPostsPage.page,
        size: myPostsPage.size,
        keyword: keywordParam // 传给后端的干净的字符串
      }
    })
    myPostsPage.total = data?.total ?? 0
    myPosts.value = data?.records || []
  } finally {
    myPostsLoading.value = false
  }
}

const fetchFootprints = async () => {
  const api = footprintFilter.value === 'commented' ? '/posts/me/commented' : '/posts/me/liked'
  try {
    const data = await request.get(api, {
      params: { page: footprintPage.page, size: footprintPage.size }
    })
    footprintPage.total = data?.total ?? 0
    footprintPosts.splice(0, footprintPosts.length, ...(data?.records || []))
  } catch (e) { console.error(e) }
}

const fetchFollowList = async () => {
  try {
    followListLoading.value = true
    const data = await request.get(`/follow/${followDialogType.value}`, {
      params: { page: followPage.page, size: followPage.size }
    })
    followList.value = data?.records || []
    followPage.total = data?.total || 0
  } catch (e) { console.error(e) }
  finally {
    followListLoading.value = false
  }
}

// ================= 4. UI 交互逻辑 =================
const handleMyPostsSearch = () => {
  myPostsPage.page = 1 // 搜索时，必须把页码重置为 1
  fetchMyPosts()
}

const handleMyPostsPageChange = (val) => {
  myPostsPage.page = val
  fetchMyPosts()
}

const handleDeleteMyPost = async (post) => {
  if (!post?.id) return
  try {
    await ElMessageBox.confirm('确定要删除这条推文吗？删除后无法恢复', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    deletingMyPostId.value = post.id
    await request.delete(`/posts/${post.id}`)
    ElMessage.success('删除成功')

    if (myPosts.value.length <= 1 && myPostsPage.page > 1) {
      myPostsPage.page -= 1
    }
    await fetchMyPosts()
  } catch (e) {
  } finally {
    deletingMyPostId.value = null
  }
}

const openEditModal = () => {
  Object.assign(editForm, userInfo)
  isEditVisible.value = true
}

const handleAvatarChange = (uploadFile) => {
  const file = uploadFile?.raw
  if (!file) return
  const previewUrl = URL.createObjectURL(file)
  pendingAvatarPreviewUrl.value = previewUrl
  pendingAvatarFile.value = file
  editForm.avatar = previewUrl
}

const saveProfile = async () => {
  try {
    if (pendingAvatarFile.value) {
      isAvatarUploading.value = true
      const formData = new FormData()
      formData.append('file', pendingAvatarFile.value)
      const avatarUrl = await request.post('/common/upload/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      editForm.avatar = avatarUrl
    }

    // 1. 发送更新请求
    await request.post('/profile/update', editForm)

    // 2. ✅ 核心修改：手动通知 App.vue (导航栏) 更新
    window.dispatchEvent(new CustomEvent('profile:updated', {
      detail: {
        nickname: editForm.nickname,
        avatar: editForm.avatar
      }
    }))

    ElMessage.success('保存成功')
    isEditVisible.value = false
    fetchProfile() // 刷新当前页面的头部信息
  } catch (e) {
    console.error('更新失败:', e)
  } finally {
    isAvatarUploading.value = false
  }
}

const cancelEdit = () => { isEditVisible.value = false }

const openFollowDialog = (type) => {
  followDialogType.value = type
  followPage.page = 1
  isFollowVisible.value = true
  fetchFollowList()
}

const toggleFollow = async (user) => {
  try {
    followActionLoadingId.value = user.id
    if (user.isFollowing) {
      await request.delete(`/follow/${user.id}`)
      user.isFollowing = false
    } else {
      await request.post(`/follow/${user.id}`)
      user.isFollowing = true
    }
    fetchProfile() // 刷新头部的粉丝数
  } finally {
    followActionLoadingId.value = null
  }
}

const folderSearchKeyword = ref('')

// 1. 搜索逻辑（带防抖效果更好）
const handleFolderSearch = () => {
  fetchFolders()
}

// 获取收藏夹
const collectionFolders = ref([])

const folderDialogVisible = ref(false)
const folderDialogMode = ref('create')
const folderDialogLoading = ref(false)
const editingFolderId = ref(null)
const folderForm = reactive({ name: '', isPublic: 0 })

// 2. 加载数据
const fetchFolders = async () => {
  try {
    // ✅ 统一使用封装好的 folderApi
    // 这样如果你以后要改 URL，只需要改 api/collection.js 一个地方
    const data = await folderApi.list(folderSearchKeyword.value)
    collectionFolders.value = data
  } catch (e) {
    console.error("获取收藏夹失败", e)
  }
}

// 3. 设为默认
const handleSetDefault = async (folder) => {
  try {
    await folderApi.setDefault(folder.id)
    ElMessage.success('已更改默认收藏夹')
    fetchFolders() // 刷新列表，看到“默认”标签转移
  } catch (e) { }
}

const startCreateFolder = () => {
  folderDialogMode.value = 'create'
  editingFolderId.value = null
  folderForm.name = ''
  folderForm.isPublic = 0
  folderDialogVisible.value = true
}

const handleEditFolder = (folder) => {
  if (!folder?.id) return
  folderDialogMode.value = 'edit'
  editingFolderId.value = folder.id
  folderForm.name = String(folder.name || '')
  folderForm.isPublic = Number(folder.isPublic) === 1 ? 1 : 0
  folderDialogVisible.value = true
}

const submitFolder = async () => {
  const name = String(folderForm.name || '').trim()
  if (!name) return
  folderDialogLoading.value = true
  try {
    const payload = { name, isPublic: Number(folderForm.isPublic) === 1 ? 1 : 0 }
    if (folderDialogMode.value === 'create') {
      await folderApi.create(payload)
      ElMessage.success('创建成功')
    } else {
      if (!editingFolderId.value) return
      await folderApi.update(editingFolderId.value, payload)
      ElMessage.success('保存成功')
    }
    folderDialogVisible.value = false
    fetchFolders()
  } finally {
    folderDialogLoading.value = false
  }
}

// 删除收藏夹
const handleDeleteFolder = async (folder) => {
  try {
    await ElMessageBox.confirm(`确认删除收藏夹「${folder?.name || ''}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await folderApi.remove(folder.id)
    ElMessage.success('删除成功')
    fetchFolders()
  } catch (e) { }
}

// 跳转收藏夹详情页（带回退所需的标记）
const goToFolderDetail = (folder) => {
  if (!folder?.id) return
  try {
    sessionStorage.setItem('profile:returnTo', 'collections')
  } catch (e) { }
  router.push({ name: 'CollectionDetail', params: { id: String(folder.id) }, query: { from: 'profile', tab: 'collections' } })
}

// ================= 5. 监听与初始化 =================
watch([footprintFilter, () => footprintPage.page], () => fetchFootprints())
watch(() => myPostsPage.page, () => fetchMyPosts())

onMounted(() => {
  fetchProfile()
  fetchMyPosts()
  fetchFootprints()
  fetchFolders()
  const queryTab = String(route.query?.tab || '').trim()
  if (queryTab) {
    activeTab.value = queryTab
    try { sessionStorage.setItem('profile:lastTab', queryTab) } catch (e) { }
  } else {
    let fallback = ''
    try {
      fallback = sessionStorage.getItem('profile:returnTo') || sessionStorage.getItem('profile:lastTab') || ''
    } catch (e) { }
    if (fallback) {
      activeTab.value = fallback
      try { sessionStorage.removeItem('profile:returnTo') } catch (e) { }
    }
  }
  const handlePopState = () => {
    if (route.name !== 'Profile') return
    let tab = ''
    try {
      tab = sessionStorage.getItem('profile:returnTo') || sessionStorage.getItem('profile:lastTab') || ''
    } catch (e) { }
    if (tab) {
      activeTab.value = tab
      try { sessionStorage.removeItem('profile:returnTo') } catch (e) { }
    }
  }
  window.addEventListener('popstate', handlePopState)
    // 存到实例上以便卸载时移除
    ; (window).__profile_popstate_handler__ = handlePopState
})

onUnmounted(() => {
  const handler = (window).__profile_popstate_handler__
  if (handler) window.removeEventListener('popstate', handler)
})

watch(() => route.query?.tab, (val) => {
  const tab = String(val || '').trim()
  if (tab) {
    activeTab.value = tab
    try { sessionStorage.setItem('profile:lastTab', tab) } catch (e) { }
  }
})

watch(activeTab, (n) => {
  try { sessionStorage.setItem('profile:lastTab', String(n || '')) } catch (e) { }
  if (route.name === 'Profile') {
    const nextQuery = { ...(route.query || {}), tab: String(n || '') }
    router.replace({ name: 'Profile', query: nextQuery })
  }
})

</script>

<style scoped>
/* ================= 1. 基础布局与动画 ================= */
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ================= 2. 个人信息头部卡片 ================= */
.profile-header-card {
  border-radius: 16px;
  background: linear-gradient(135deg, #ffffff 0%, #f8faff 100%);
  margin-bottom: 30px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.user-info-wrapper {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 10px;
}

.avatar-section .el-avatar {
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.info-section {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.nickname {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.edit-btn {
  margin-left: auto;
  border-radius: 20px;
}

.bio {
  color: #606266;
  font-size: 14px;
  margin-top: 0;
  margin-bottom: 20px;
}

/* 数据统计栏 */
.stats-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 16px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.stat-item.clickable {
  cursor: pointer;
}

.stat-item.clickable:hover {
  background-color: #f0f4f8;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-divider {
  width: 1px;
  height: 30px;
  background-color: #ebeef5;
}

/* ================= 3. 内容区与足迹过滤器 ================= */
.content-card {
  border-radius: 16px;
  min-height: 400px;
  border: none;
}

.custom-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
}

.clickable-post {
  cursor: pointer;
}

.follow-list {
  min-height: 200px;
}

.follow-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  background: #fff;
  transition: background-color 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}

.follow-item+.follow-item {
  margin-top: 12px;
}

.follow-item:hover {
  background-color: #f8faff;
  border-color: #dcdfe6;
  transform: translateY(-1px);
}

.follow-avatar {
  cursor: pointer;
  flex: 0 0 auto;
}

.follow-info {
  flex: 1;
  min-width: 0;
}

.follow-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.follow-bio {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 足迹过滤器加大 */
.footprint-filters :deep(.el-radio-button__inner) {
  padding: 12px 28px;
  font-size: 15px;
  font-weight: 500;
}

/* ================= 4. 收藏夹管理栏 (核心修复) ================= */
.collection-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 10px 0 25px 0;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f2f5;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 20px;
  color: #409EFF;
}

.toolbar-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.toolbar-right {
  display: flex;
  gap: 20px;
  align-items: center;
}

/* ✅ 重点修复：搜索框与按钮无缝衔接 */
.search-input-group {
  width: 300px;
}

/* 消除 Append 区域的默认样式影响 */
.search-input-group :deep(.el-input-group__append) {
  background-color: #409EFF !important;
  border: none;
  padding: 0;
  /* 必须清空 padding */
  overflow: hidden;
}

/* 强制内部按钮填满空间并修正高度 */
.search-input-group :deep(.el-input-group__append .el-button) {
  background-color: transparent !important;
  color: white !important;
  border: none;
  margin: 0;
  height: 40px;
  /* 强制对齐 Element Plus 默认高度 */
  padding: 0 20px;
  border-radius: 0;
  /* 消除左侧圆角 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-input-group :deep(.el-input-group__append .el-button:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

.create-btn {
  font-weight: 600;
  height: 40px;
  border-radius: 8px;
}

/* ================= 5. 收藏夹卡片样式 ================= */
.folder-card {
  position: relative;
  border-radius: 12px;
  margin-bottom: 20px;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #f0f2f5;
}

.folder-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  border-color: #409EFF;
}

.folder-tags {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  gap: 4px;
}

.folder-tags :deep(.el-tag) {
  transform: scale(0.85);
  transform-origin: right center;
}

.folder-visual {
  margin-top: 25px;
  margin-bottom: 10px;
}

.folder-main-icon {
  font-size: 52px;
  color: #409EFF;
  opacity: 0.7;
}

.folder-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 6px;
  padding: 0 15px;
}

.folder-count {
  font-size: 13px;
  color: #909399;
}

.folder-footer {
  margin-top: 20px;
  padding: 12px 0;
  background-color: #fafafa;
  border-top: 1px solid #f0f2f5;
  display: flex;
  justify-content: center;
  align-items: center;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}

.folder-card {
  cursor: pointer;
  /* 鼠标悬停变小手 */
  transition: all 0.3s;
}

.folder-card:hover {
  transform: translateY(-5px);
  /* 悬停时稍微往上浮动，增加交互感 */
  border-color: #a0cfff;
}

/* 确保页脚按钮区域不要让用户觉得那是背景点击区 */
.folder-footer {
  cursor: default;
  /* 按钮区域恢复默认指针 */
}

.like-icon {
  color: #f56c6c;
  /* 漂亮的爱心红 */
  vertical-align: middle;
  margin-left: 4px;
}

/* ================= 6. 其他 UI 组件 ================= */
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed #dcdfe6;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: 0.3s;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 80px;
  height: 80px;
  line-height: 80px;
  text-align: center;
}

.uploaded-avatar {
  width: 80px;
  height: 80px;
  display: block;
  object-fit: cover;
}
</style>
