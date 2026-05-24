<template>
  <el-container>
    <el-header>
      <div class="header-content">
        <h1 class="logo">CogniBlog</h1>
        <el-menu mode="horizontal" :router="true" default-active="/home">
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/publish">发布文章</el-menu-item>
          <el-menu-item v-if="role === 'admin'" index="/categoryManage">分类管理</el-menu-item>
          <el-menu-item v-if="role === 'admin'" index="/tagManage">标签管理</el-menu-item>
          <el-menu-item index="/user">个人中心</el-menu-item>
        </el-menu>
        <div class="user-info">
          <template v-if="token">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                <el-avatar v-if="avatar" :src="fullAvatarUrl" size="small" style="margin-right: 8px" />
                <el-avatar v-else size="small" style="margin-right: 8px">{{ username }}</el-avatar>
                {{ username }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const token = ref(localStorage.getItem('token'))
const username = ref(localStorage.getItem('username') || '用户')
const avatar = ref(localStorage.getItem('avatar') || '')
const role = ref(localStorage.getItem('role') || 'user')
const BASE_URL = 'http://localhost:8080/api'

const fullAvatarUrl = computed(() => {
  if (!avatar.value) return ''
  if (avatar.value.startsWith('http')) return avatar.value
  return BASE_URL + avatar.value
})

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('avatar')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/user')
  }
}

onMounted(() => {
  window.addEventListener('storage', () => {
    token.value = localStorage.getItem('token')
    username.value = localStorage.getItem('username') || '用户'
    avatar.value = localStorage.getItem('avatar') || ''
    role.value = localStorage.getItem('role') || 'user'
  })
})
</script>

<style scoped>
.el-header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.logo {
  margin: 0 30px 0 0;
  font-size: 24px;
  color: #409eff;
}

.el-menu {
  flex: 1;
  border-bottom: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.el-dropdown-link {
  cursor: pointer;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>