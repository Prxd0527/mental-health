<template>
  <div class="article-manage-page">
    <h1 class="section-title">文章管理</h1>
    <el-button type="primary" @click="openEditor()" style="margin-bottom:16px">新增文章</el-button>
    <el-table :data="articles" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="views" label="阅读量" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'发布':'下架' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openEditor(row)">编辑</el-button>
          <el-button v-if="row.status===1" size="small" type="warning" @click="handleToggle(row.id,0)">下架</el-button>
          <el-button v-else size="small" type="success" @click="handleToggle(row.id,1)">发布</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showEditor" :title="editForm.id?'编辑文章':'新增文章'" width="600px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="editForm.title" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="editForm.category" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="editForm.content" type="textarea" :rows="8" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditor=false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getArticleManageList, saveArticle, toggleArticleStatus } from '@/api/admin'

const articles = ref([])
const showEditor = ref(false)
const editForm = reactive({ id: null, title: '', category: '', content: '' })

function openEditor(row) {
  if (row) { Object.assign(editForm, row) } else { editForm.id = null; editForm.title = ''; editForm.category = ''; editForm.content = '' }
  showEditor.value = true
}

async function handleSave() {
  try { await saveArticle(editForm); ElMessage.success('保存成功'); showEditor.value = false; fetchData() } catch { /* ignore */ }
}

async function handleToggle(id, status) {
  try { await toggleArticleStatus(id, status); ElMessage.success('操作成功'); fetchData() } catch { /* ignore */ }
}

async function fetchData() {
  try { const r = await getArticleManageList(); articles.value = r.data?.records || r.data || [] } catch { /* ignore */ }
}

onMounted(fetchData)
</script>
