<template>
  <el-container class="app-wrapper">
    <el-header class="header">
      <div class="logo-container">
        <el-icon :size="24" color="#409EFF"><Fitness /></el-icon>
        <span class="logo-text">AiTrainer 爱健身</span>
      </div>
      
      <el-menu 
        :default-active="route.path" 
        class="nav-menu" 
        mode="horizontal" 
        router
        :ellipsis="false"
      >
        <el-menu-item index="/community">健身社区</el-menu-item>
        <el-menu-item index="/workout">项目大厅</el-menu-item>
        <el-menu-item index="/dashboard">数据看板</el-menu-item>
        <el-menu-item index="/profile">个人主页</el-menu-item>
      </el-menu>

      <div class="user-actions">
        <el-dropdown trigger="click">
          <span class="el-dropdown-link avatar-wrapper">
            <el-avatar size="small" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            <span class="username">陈同学</span>
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/profile')">我的主页</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const handleLogout = () => {
  // 模拟退出逻辑
  localStorage.removeItem('jwt_token')
  router.push('/login')
}
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 0 40px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.nav-menu {
  flex: 1;
  justify-content: center;
  border-bottom: none; /* 覆盖默认边框 */
}

.user-actions {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.main-content {
  padding: 24px 40px;
  box-sizing: border-box;
}

/* 简单的路由切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>