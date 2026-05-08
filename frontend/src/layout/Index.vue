<template>
  <div class="app-wrapper">

    <template v-if="route.path !== '/onboarding'">
      <el-menu v-if="!isMobile" mode="horizontal" :default-active="route.path" router class="custom-top-nav"
        :ellipsis="false">
        <div class="brand-logo">
          <img src="/爱健身.png" alt="AiTrainer Logo" class="logo-img" />
          <span class="logo-text">AiTrainer</span>
        </div>

        <div class="flex-grow"></div>

        <el-menu-item index="/workout">项目大厅</el-menu-item>
        <el-menu-item index="/community">健身社区</el-menu-item>
        <el-menu-item index="/diet">营养膳食</el-menu-item>
        <el-menu-item index="/dashboard">数据看板</el-menu-item>
        <el-menu-item index="/coach" class="ai-nav-item">
          <el-icon>
            <Microphone />
          </el-icon> AI 私教
        </el-menu-item>
        <el-menu-item index="/profile">个人主页</el-menu-item>

        <div class="flex-grow"></div>

        <div class="nav-user-profile">
          <el-dropdown placement="bottom-end">
            <span class="user-dropdown-link">
              <el-avatar :size="32" :src="avatar">
                <el-icon>
                  <UserFilled />
                </el-icon>
              </el-avatar>
              <span class="username">{{ nickname }}</span>
              <el-icon>
                <ArrowDown />
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">进入主页</el-dropdown-item>
                <el-dropdown-item @click="goToMySpace">我的空间</el-dropdown-item>
                <el-dropdown-item @click="router.push('/settings')">账号设置</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-menu>

      <div v-else class="mobile-topbar">
        <div class="mobile-left">
          <el-button class="mobile-menu-btn" text @click="mobileDrawerVisible = true">
            <el-icon>
              <Menu />
            </el-icon>
          </el-button>
          <div class="brand-logo mobile-brand">
            <img src="/爱健身.png" alt="AiTrainer Logo" class="logo-img mobile-logo" />
            <span class="logo-text mobile-logo-text">AiTrainer</span>
          </div>
        </div>

        <div class="nav-user-profile mobile-user">
          <el-dropdown placement="bottom-end">
            <span class="user-dropdown-link">
              <el-avatar :size="28" :src="avatar">
                <el-icon>
                  <UserFilled />
                </el-icon>
              </el-avatar>
              <el-icon>
                <ArrowDown />
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">进入主页</el-dropdown-item>
                <el-dropdown-item @click="goToMySpace">我的空间</el-dropdown-item>
                <el-dropdown-item @click="router.push('/settings')">账号设置</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <el-drawer v-model="mobileDrawerVisible" class="mobile-drawer" direction="ttb" :with-header="false" size="100%">
          <div class="mobile-drawer-header">
            <div class="mobile-drawer-title">全部功能</div>
            <el-button text class="mobile-drawer-close" @click="mobileDrawerVisible = false">
              <el-icon>
                <Close />
              </el-icon>
            </el-button>
          </div>
          <el-menu :default-active="route.path" class="mobile-drawer-menu" @select="handleMobileSelect">
            <el-menu-item index="/workout">项目大厅</el-menu-item>
            <el-menu-item index="/community">健身社区</el-menu-item>
            <el-menu-item index="/diet">营养膳食</el-menu-item>
            <el-menu-item index="/dashboard">数据看板</el-menu-item>
            <el-menu-item index="/coach">AI 私教</el-menu-item>
            <el-menu-item index="/profile">个人主页</el-menu-item>
            <el-menu-item index="/settings">账号设置</el-menu-item>
            <el-menu-item index="__my_space">我的空间</el-menu-item>
            <el-menu-item index="__logout">退出登录</el-menu-item>
          </el-menu>
        </el-drawer>
      </div>
    </template>

    <div class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Microphone, ArrowDown, UserFilled, Menu, Close } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/store/userStore'

const route = useRoute()
const router = useRouter()

const userStore = useUserStore()
const { avatar, nickname } = storeToRefs(userStore)

const isMobile = ref(false)
const mobileDrawerVisible = ref(false)

const updateIsMobile = () => {
  isMobile.value = window.matchMedia('(max-width: 768px)').matches
}

const handleMobileSelect = async (index) => {
  mobileDrawerVisible.value = false
  if (index === '__logout') {
    handleLogout()
    return
  }
  if (index === '__my_space') {
    await goToMySpace()
    return
  }
  router.push(index)
}

const goToMySpace = async () => {
  if (!userStore.userId) {
    await userStore.fetchNavUser()
  }
  if (!userStore.userId) return
  router.push({ name: 'UserSpace', params: { id: String(userStore.userId) } })
}

// App.vue 中的 handleProfileUpdated
const handleProfileUpdated = async (event) => {
  console.log('收到资料更新信号:', event.detail)

  // 方案 A：如果事件传了具体数据，直接同步到 Store
  if (event.detail && (event.detail.nickname || event.detail.avatar)) {
    userStore.applyProfileUpdated(event.detail)
  } else {
    // 方案 B：如果没有明细数据，直接重新请求后端接口（最稳妥）
    await userStore.fetchNavUser()
  }
}

onMounted(() => {
  updateIsMobile()
  window.addEventListener('resize', updateIsMobile)
  userStore.fetchNavUser()
  window.addEventListener('profile:updated', handleProfileUpdated)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateIsMobile)
  window.removeEventListener('profile:updated', handleProfileUpdated)
})

const handleLogout = () => {
  localStorage.removeItem('jwt_token')
  router.push('/login')
}
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.custom-top-nav {
  height: 60px;
  display: flex;
  align-items: center;
  border-bottom: none !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 1000;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

/* ================= 修改点：Logo 区域样式 ================= */
.brand-logo {
  display: flex;
  align-items: center;
  padding-left: 10px;
  /* 已经删除了 cursor: pointer 和悬浮效果 */
}

.logo-img {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  margin-right: 10px;
  object-fit: contain;
}

.logo-text {
  font-size: 22px;
  font-weight: 900;
  letter-spacing: 0.5px;
  background: linear-gradient(45deg, #409EFF, #8a2be2);
  -webkit-background-clip: text;
  color: transparent;
}

/* 占位符弹簧 */
.flex-grow {
  flex-grow: 1;
}

.custom-top-nav .el-menu-item {
  font-size: 15px;
  font-weight: 500;
  color: #606266;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.custom-top-nav .el-menu-item.is-active {
  font-weight: bold;
  color: #409EFF !important;
  border-bottom: 2px solid #409EFF !important;
  background-color: transparent !important;
}

.ai-nav-item {
  color: #8a2be2 !important;
}

.ai-nav-item.is-active {
  border-bottom-color: #8a2be2 !important;
}

.nav-user-profile {
  display: flex;
  align-items: center;
  margin-left: 20px;
  padding-right: 10px;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.main-content {
  padding: 24px 40px;
  box-sizing: border-box;
}

.mobile-topbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  position: sticky;
  top: 0;
  z-index: 1000;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.mobile-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.mobile-menu-btn {
  padding: 6px;
}

.mobile-brand {
  padding-left: 0;
}

.mobile-logo {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  margin-right: 8px;
}

.mobile-logo-text {
  font-size: 18px;
}

.mobile-user {
  margin-left: 0;
  padding-right: 0;
}

.mobile-drawer-menu {
  border-right: none;
}

.mobile-drawer :deep(.el-drawer__body) {
  padding: 0;
  display: flex;
  flex-direction: column;
}

.mobile-drawer-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  box-sizing: border-box;
  border-bottom: 1px solid #ebeef5;
}

.mobile-drawer-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.mobile-drawer-close {
  padding: 6px;
}

.mobile-drawer-menu {
  flex: 1;
  overflow: auto;
}

.mobile-drawer-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
}

@media (max-width: 768px) {
  .main-content {
    padding: 12px 12px;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
