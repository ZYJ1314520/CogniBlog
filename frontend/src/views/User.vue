<template>
  <div class="user-container">
    <el-card>
      <template #header>
        <span>个人信息</span>
      </template>
      <el-form ref="formRef" :model="userInfo" :rules="rules" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="userInfo.username" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userInfo.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
          >
            <img v-if="userInfo.avatar" :src="fullAvatarUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleUpdate">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>修改密码</span>
      </template>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>我的文章</span>
      </template>
      <el-table :data="myArticles" stripe>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="category.name" label="分类" width="100" />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="$router.push(`/publish?id=${row.id}`)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const BASE_URL = 'http://localhost:8080/api'

const fullAvatarUrl = computed(() => {
  if (!userInfo.value.avatar) return ''
  if (userInfo.value.avatar.startsWith('http')) return userInfo.value.avatar
  return BASE_URL + userInfo.value.avatar
})

const handleAvatarChange = async (file) => {
  const isImage = file.raw.type.startsWith('image/')
  const isLt2M = file.raw.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }

  const formData = new FormData()
  formData.append('file', file.raw)
  try {
    const res = await request.post('/upload/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    userInfo.value.avatar = res.data
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error(error)
  }
}

const formRef = ref()
const loading = ref(false)
const userInfo = ref({
  id: null,
  username: '',
  nickname: '',
  avatar: ''
})
const myArticles = ref([])

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const pwdFormRef = ref()
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }]
}

const loadUserInfo = async () => {
  try {
    const res = await request.get('/user/info')
    userInfo.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadMyArticles = async () => {
  try {
    const res = await request.get('/article/my')
    myArticles.value = res.data.records || []
  } catch (error) {
    console.error(error)
  }
}

const handleUpdate = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    await request.put('/user/update', userInfo.value)
    ElMessage.success('保存成功')
    localStorage.setItem('username', userInfo.value.nickname)
    localStorage.setItem('avatar', userInfo.value.avatar || '')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await request.delete(`/article/delete/${id}`)
    ElMessage.success('删除成功')
    loadMyArticles()
  } catch (error) {
    console.error(error)
  }
}

const handleChangePassword = async () => {
  try {
    await pwdFormRef.value.validate()
    await request.post('/user/changePassword', pwdForm.value)
    ElMessage.success('密码修改成功')
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadUserInfo()
  loadMyArticles()
})
</script>

<style scoped>
.user-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c8c8c;
}

.avatar {
  width: 100px;
  height: 100px;
  object-fit: cover;
}
</style>