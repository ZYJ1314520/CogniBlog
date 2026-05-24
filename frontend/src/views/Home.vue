<template>
  <div class="home-container">
    <div class="content-wrapper">
      <aside class="sidebar">
        <el-card>
          <template #header>
            <span>分类筛选</span>
          </template>
          <el-radio-group v-model="selectedCategory" @change="loadArticles">
            <el-radio-button :label="null">全部</el-radio-button>
            <el-radio-button v-for="cat in categories" :key="cat.id" :label="cat.id">
              {{ cat.name }}
            </el-radio-button>
          </el-radio-group>
        </el-card>
        <el-card style="margin-top: 20px">
          <template #header>
            <span>标签筛选</span>
          </template>
          <el-tag
            v-for="tag in tags"
            :key="tag.id"
            :type="selectedTag === tag.id ? 'primary' : 'info'"
            class="tag-item"
            @click="selectedTag = selectedTag === tag.id ? null : tag.id; loadArticles()"
          >
            {{ tag.name }}
          </el-tag>
        </el-card>
      </aside>
      <main class="article-list">
        <div class="search-bar">
          <el-input
            v-model="keyword"
            placeholder="搜索文章标题或内容..."
            clearable
            @clear="loadArticles"
            @keyup.enter="loadArticles"
          >
            <template #append>
              <el-button icon="Search" @click="loadArticles" />
            </template>
          </el-input>
        </div>
        <el-card v-for="article in articles" :key="article.id" class="article-card" shadow="hover">
          <template #header>
            <div class="article-header" @click="goDetail(article.id)">
              <h3>{{ article.title }}</h3>
              <div class="article-meta">
                <span>作者：{{ article.user?.nickname }}</span>
                <span>分类：{{ article.category?.name }}</span>
                <span>浏览：{{ article.viewCount }}</span>
                <span>{{ article.createTime }}</span>
              </div>
            </div>
          </template>
          <div class="article-tags">
            <el-tag v-for="tag in article.tags" :key="tag.id" size="small" style="margin-right: 5px">
              {{ tag.name }}
            </el-tag>
          </div>
        </el-card>
        <el-pagination
          v-if="total > 0"
          v-model:current-page="current"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadArticles"
          @current-change="loadArticles"
        />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const articles = ref([])
const categories = ref([])
const tags = ref([])
const selectedCategory = ref(null)
const selectedTag = ref(null)
const keyword = ref('')
const current = ref(1)
const size = ref(10)
const total = ref(0)

const loadArticles = async () => {
  try {
    const params = {
      current: current.value,
      size: size.value
    }
    if (selectedCategory.value) params.categoryId = selectedCategory.value
    if (selectedTag.value) params.tagId = selectedTag.value
    if (keyword.value) params.keyword = keyword.value
    const res = await request.get('/article/list', { params })
    articles.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  }
}

const loadCategories = async () => {
  try {
    const res = await request.get('/category/list')
    categories.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadTags = async () => {
  try {
    const res = await request.get('/tag/list')
    tags.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const goDetail = (id) => {
  router.push(`/article/${id}`)
}

onMounted(() => {
  loadArticles()
  loadCategories()
  loadTags()
})
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.content-wrapper {
  display: flex;
  gap: 20px;
}

.sidebar {
  width: 240px;
  flex-shrink: 0;
}

:deep(.el-radio-group) {
  display: flex;
  flex-direction: column;
}

:deep(.el-radio-button) {
  margin-bottom: 8px;
}

.tag-item {
  margin-bottom: 8px;
  margin-right: 8px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}

.article-list {
  flex: 1;
}

.search-bar {
  margin-bottom: 20px;
}

.article-card {
  margin-bottom: 15px;
  cursor: pointer;
}

.article-header h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.article-meta {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #999;
}

.article-tags {
  margin-top: 10px;
}

.tag-item {
  cursor: pointer;
  margin-bottom: 5px;
}
</style>