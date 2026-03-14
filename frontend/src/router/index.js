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
        path: '/coach',
        name: 'Coach',
        component: () => import('../views/coach/Index.vue'),
        meta: { title: 'AI 私教' }
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
  },
  {
    path: '/onboarding',
    name: 'Onboarding',
    component: () => import('../views/onboarding/Index.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 假设你登录成功后把后端返回的 isFirstLogin 存到了 localStorage
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('jwt_token')
  const isFirstLogin = localStorage.getItem('is_first_login') // 'true' 或 'false'

  // 1. 如果没登录，去登录页
  if (!token && to.path !== '/login') {
    return next('/login')
  }

  // 2. 如果已登录且是首次登录，且当前不是引导页 -> 强制去引导页
  if (token && isFirstLogin === 'true' && to.path !== '/onboarding') {
    return next('/onboarding')
  }

  next()
})

export default router