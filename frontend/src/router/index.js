import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { title: '登录 - AiTrainer' }
  },
  {
    path: '/',
    component: Layout, // 这四个核心模块都被 Layout 外壳包裹
    redirect: '/community',
    children: [
      {
        path: 'community',
        name: 'Community',
        component: () => import('../views/community/Index.vue'),
        meta: { title: '健身社区' }
      },
      {
        path: 'workout',
        name: 'WorkoutLobby',
        component: () => import('../views/workout/Lobby.vue'),
        meta: { title: '项目大厅' }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Index.vue'),
        meta: { requiresAuth: true, title: '数据看板' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/Index.vue'),
        meta: { requiresAuth: true, title: '个人主页' }
      }
    ]
  },
  // 独立全屏显示的 AI 战报页 (也可以选择放在 Layout 下，看你想要全屏还是带导航的沉浸感)
  {
    path: '/workout/report/:id',
    name: 'WorkoutReport',
    component: () => import('../views/workout/Report.vue'),
    meta: { title: 'AI 训练结算报告' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router