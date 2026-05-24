<template>
  <div class="user-profile-container">
    <el-button @click="goBack" style="margin-bottom: 15px">
      <el-icon style="margin-right: 5px"><ArrowLeft /></el-icon>返回
    </el-button>

    <el-card v-if="userInfo.id">
      <template #header>
        <div class="user-header">
          <el-avatar :size="60">
            <img v-if="userInfo.avatar" :src="fullAvatarUrl" />
            <span v-else>{{ userInfo.nickname?.[0] }}</span>
          </el-avatar>
          <div class="user-info">
            <h2>{{ userInfo.nickname }}</h2>
            <p>@{{ userInfo.username }}</p>
          </div>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="昵称">{{ userInfo.nickname }}</el-descriptions-item>
        <el-descriptions-item label="文章数">{{ total }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>他的文章</span>
      </template>
      <el-table :data="articles" stripe>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="category.name" label="分类" width="100" />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="$router.push(`/article/${row.id}`)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > 0"
        style="margin-top: 20px"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="current"
        @current-change="handlePageChange"
      />
    </el-card>

    <el-empty v-if="!loading && articles.length === 0" description="该用户暂无文章" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'

const BASE_URL = 'http://localhost:8080/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const userInfo = ref({})
const articles = ref([])
const current = ref(1)
const size = ref(10)
const total = ref(0)

const fullAvatarUrl = computed(() => {
  if (!userInfo.value.avatar) return ''
  if (userInfo.value.avatar.startsWith('http')) return userInfo.value.avatar
  return BASE_URL + userInfo.value.avatar
})

const loadUserInfo = async (userId) => {
  try {
    const res = await request.get(`/user/${userId}`)
    userInfo.value = res.data || {}
  } catch (error) {
    console.error(error)
    ElMessage.error('用户不存在')
    router.push('/home')
  }
}

const loadUserArticles = async (userId) => {
  loading.value = true
  try {
    const res = await request.get('/article/list', {
      params: { current: current.value, size: size.value, userId: userId }
    })
    articles.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  current.value = page
  const userId = route.params.userId
  if (userId) {
    loadUserArticles(userId)
  }
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/home')
  }
}

onMounted(() => {
  const userId = route.params.userId
  if (userId) {
    loadUserInfo(userId)
    loadUserArticles(userId)
  }
})
</script>

<style scoped>
.user-profile-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info h2 {
  margin: 0 0 5px 0;
}

.user-info p {
  margin: 0;
  color: #999;
}
</style>
