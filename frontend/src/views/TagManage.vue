<template>
  <div class="manage-container">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>标签管理</span>
          <el-button type="primary" size="small" @click="handleAdd">新增标签</el-button>
        </div>
      </template>
      <el-table :data="tags" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新增标签'" width="400px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tags = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = ref({ id: null, name: '' })
const rules = { name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }] }

const loadTags = async () => {
  const res = await request.get('/tag/list')
  tags.value = res.data
}

const handleAdd = () => {
  isEdit.value = false
  form.value = { id: null, name: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { id: row.id, name: row.name }
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value.validate()
  if (isEdit.value) {
    await request.put('/tag/update', form.value)
    ElMessage.success('更新成功')
  } else {
    await request.post('/tag/add', form.value)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  loadTags()
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该标签吗？', '提示', { type: 'warning' })
    await request.delete('/tag/delete/' + id)
    ElMessage.success('删除成功')
    loadTags()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

onMounted(loadTags)
</script>

<style scoped>
.manage-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>