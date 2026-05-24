import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/login',
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: '/article/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/ArticleDetail.vue')
      },
      {
        path: '/publish',
        name: 'Publish',
        component: () => import('@/views/Publish.vue')
      },
      {
        path: '/user',
        name: 'User',
        component: () => import('@/views/User.vue')
      },
      {
        path: '/user/:userId',
        name: 'UserProfile',
        component: () => import('@/views/UserProfile.vue')
      },
      {
        path: '/categoryManage',
        name: 'CategoryManage',
        component: () => import('@/views/CategoryManage.vue')
      },
      {
        path: '/tagManage',
        name: 'TagManage',
        component: () => import('@/views/TagManage.vue')
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：登录拦截
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const publicPaths = ['/login', '/register']

  // 允许未登录访问用户公开主页
  const isPublicPath = publicPaths.includes(to.path) || to.path.startsWith('/user/')

  // 已登录用户访问登录/注册页则跳转到主页
  if (publicPaths.includes(to.path) && token) {
    return next('/home')
  }

  // 未登录用户访问受保护页面则跳转到登录页
  if (!isPublicPath && !token) {
    return next('/login')
  }

  next()
})

export default router