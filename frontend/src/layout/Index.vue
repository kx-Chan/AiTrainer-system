<template>
  <div class="app-wrapper">
    
    <el-menu 
      v-if="route.path !== '/onboarding'"
      mode="horizontal" 
      :default-active="route.path" 
      router 
      class="custom-top-nav"
      :ellipsis="false"
    >
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
        <el-icon><Microphone /></el-icon> AI 私教
      </el-menu-item>
      <el-menu-item index="/profile">个人主页</el-menu-item>

      <div class="flex-grow"></div>

      <div class="nav-user-profile">
        <el-dropdown placement="bottom-end">
          <span class="user-dropdown-link">
            <el-avatar :size="32" :src="navUser.avatar" />
            <span class="username">{{ navUser.nickname }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/profile')">进入主页</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-menu>

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
import { onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Microphone, ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const DEFAULT_AVATAR_URL = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const navUser = reactive({
  avatar: DEFAULT_AVATAR_URL,
  nickname: '用户'
})

const fetchNavUser = async () => {
  try {
    const data = await request.get('/profile/info')
    navUser.avatar = data?.avatar || DEFAULT_AVATAR_URL
    navUser.nickname = data?.nickname || '用户'
  } catch (error) {
    navUser.avatar = DEFAULT_AVATAR_URL
    navUser.nickname = '用户'
  }
}

onMounted(() => {
  fetchNavUser()
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
.flex-grow { flex-grow: 1; }

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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
