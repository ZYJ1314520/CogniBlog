<template>
  <div class="article-detail-container">
    <el-button @click="goBack" style="margin-bottom: 15px">
      <el-icon style="margin-right: 5px"><ArrowLeft /></el-icon>返回
    </el-button>

    <el-card v-if="article.id">
      <template #header>
        <h1>{{ article.title }}</h1>
        <div class="article-meta">
          <span>作者：{{ article.user?.nickname }}</span>
          <span>分类：{{ article.category?.name }}</span>
          <span>浏览：{{ article.viewCount }}</span>
          <span>发布时间：{{ article.createTime }}</span>
        </div>
      </template>
      <div class="article-content" v-html="article.content"></div>
      <div class="article-tags">
        <el-tag v-for="tag in article.tags" :key="tag.id" size="small" style="margin-right: 5px">
          {{ tag.name }}
        </el-tag>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>评论</span>
        <el-button type="primary" size="small" @click="showCommentDialog = true" style="float: right">
         发表评论
        </el-button>
      </template>
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-header">
          <div class="comment-user" @click="goToUserProfile(comment.user?.id)" style="cursor: pointer;">
            <el-avatar :size="32">
              <img v-if="comment.user?.avatar" :src="getAvatarUrl(comment.user.avatar)" />
              <span v-else>{{ comment.user?.nickname?.[0] }}</span>
            </el-avatar>
            <strong>{{ comment.user?.nickname }}</strong>
          </div>
          <span>{{ comment.createTime }}</span>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
        <div v-if="comment.children?.length > 0" class="comment-children">
          <div v-for="child in comment.children" :key="child.id" class="comment-item child">
            <div class="comment-header">
              <div class="comment-user" @click="goToUserProfile(child.user?.id)" style="cursor: pointer;">
                <el-avatar :size="28">
                  <img v-if="child.user?.avatar" :src="getAvatarUrl(child.user.avatar)" />
                  <span v-else>{{ child.user?.nickname?.[0] }}</span>
                </el-avatar>
                <strong>{{ child.user?.nickname }}</strong>
              </div>
              <span>{{ child.createTime }}</span>
            </div>
            <div class="comment-content">{{ child.content }}</div>
          </div>
        </div>
      </div>
      <el-empty v-if="comments.length === 0" description="暂无评论" />
    </el-card>

    <el-dialog v-model="showCommentDialog" title="发表评论" width="500px">
      <el-input v-model="commentContent" type="textarea" :rows="4" placeholder="请输入评论内容" />
      <template #footer>
        <el-button @click="showCommentDialog = false">取消</el-button>
        <el-button type="primary" @click="submitComment">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const BASE_URL = 'http://localhost:8080/api'

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  return BASE_URL + avatar
}

const route = useRoute()
const article = ref({})
const comments = ref([])
const showCommentDialog = ref(false)
const commentContent = ref('')

const loadArticle = async () => {
  try {
    const res = await request.get(`/article/${route.params.id}`)
    article.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadComments = async () => {
  try {
    const res = await request.get(`/comment/list`, { params: { articleId: route.params.id } })
    comments.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const submitComment = async () => {
  try {
    await request.post('/comment/add', {
      articleId: route.params.id,
      content: commentContent.value
    })
    ElMessage.success('评论成功')
    showCommentDialog.value = false
    commentContent.value = ''
    loadComments()
  } catch (error) {
    console.error(error)
  }
}

const goToUserProfile = (userId) => {
  if (userId) {
    router.push(`/user/${userId}`)
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
  loadArticle()
  loadComments()
})
</script>

<style scoped>
.article-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.article-meta {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 14px;
  margin-top: 10px;
}

.article-content {
  line-height: 1.8;
  margin: 20px 0;
}

.article-tags {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.comment-item.child {
  margin-left: 30px;
  padding-left: 15px;
  border-left: 2px solid #409eff;
  border-bottom: none;
}

.comment-children {
  margin-top: 10px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
  color: #666;
  font-size: 13px;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-content {
  color: #333;
  line-height: 1.6;
}
</style>
