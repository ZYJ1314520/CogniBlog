<template>
  <div class="publish-container">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑文章' : '发布文章' }}</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tagIds" multiple placeholder="请选择标签" style="width: 100%">
            <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="文章内容" prop="content">
          <div ref="editorRef" class="editor-container"></div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handlePublish">
            {{ isEdit ? '保存' : '发布' }}
          </el-button>
          <el-button @click="handleAISummary">AI 生成摘要</el-button>
          <el-button @click="handleAITags">AI 自动打标</el-button>
          <el-button @click="handleAIRewrite">AI 内容润色</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import E from '@wangeditor/editor'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)
const isEdit = ref(false)
const editorRef = ref()
let editor = null
const categories = ref([])
const tags = ref([])

const form = ref({
  title: '',
  categoryId: null,
  tagIds: [],
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

const initEditor = () => {
  editor = new E(editorRef.value)
  editor.create()
  if (form.value.content) {
    editor.setHtml(form.value.content)
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

const handlePublish = async () => {
  form.value.content = editor.getHtml()
  try {
    await formRef.value.validate()
    loading.value = true
    if (isEdit.value) {
      await request.put('/article/update', form.value)
      ElMessage.success('保存成功')
    } else {
      await request.post('/article/add', form.value)
      ElMessage.success('发布成功')
    }
    router.push('/home')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAISummary = async () => {
  if (!form.value.content && !editor.getText()) {
    ElMessage.warning('请先输入文章内容')
    return
  }
  const content = editor.getText() || form.value.content
  try {
    const res = await request.post('/ai/summary', { content })
    ElMessage.success('AI 摘要：' + res.data)
  } catch (error) {
    console.error(error)
  }
}

const handleAITags = async () => {
  if (!form.value.title || (!form.value.content && !editor.getText())) {
    ElMessage.warning('请先输入标题和内容')
    return
  }
  const content = editor.getText() || form.value.content
  try {
    const res = await request.post('/ai/tag', {
      title: form.value.title,
      content
    })
    ElMessage.success('AI 推荐标签：' + res.data.join(', '))
  } catch (error) {
    console.error(error)
  }
}

const handleAIRewrite = async () => {
  if (!form.value.content && !editor.getText()) {
    ElMessage.warning('请先输入文章内容')
    return
  }
  const content = editor.getText() || form.value.content
  try {
    const res = await request.post('/ai/rewrite', { content })
    editor.setHtml(res.data)
    form.value.content = res.data
    ElMessage.success('内容润色完成')
  } catch (error) {
    console.error(error)
  }
}

onMounted(async () => {
  loadCategories()
  loadTags()
  const articleId = route.query.id
  if (articleId) {
    isEdit.value = true
    try {
      const res = await request.get('/article/' + articleId)
      form.value = {
        id: res.data.id,
        title: res.data.title,
        categoryId: res.data.category?.id,
        tagIds: res.data.tags?.map(t => t.id) || [],
        content: res.data.content
      }
    } catch (error) {
      console.error(error)
    }
  }
  initEditor()
})

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy()
  }
})
</script>

<style scoped>
.publish-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.editor-container {
  min-height: 400px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

:deep(.w-e-panel-container) {
  width: 100% !important;
}
</style>