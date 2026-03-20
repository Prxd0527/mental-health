<template>
  <div class="quiz-manage-page">
    <h1 class="section-title">题库管理</h1>
    <el-button type="primary" @click="openEditor()" style="margin-bottom:16px">新增量表</el-button>
    <el-table :data="quizzes" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="量表名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'上线':'下线' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openEditor(row)">编辑</el-button>
          <el-button v-if="row.status===1" size="small" type="warning" @click="handleToggle(row.id,0)">下线</el-button>
          <el-button v-else size="small" type="success" @click="handleToggle(row.id,1)">上线</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showEditor" :title="editForm.id?'编辑量表':'新增量表'" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="editForm.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="3" /></el-form-item>
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
import { getQuizManageList, saveQuiz, toggleQuizStatus } from '@/api/admin'

const quizzes = ref([])
const showEditor = ref(false)
const editForm = reactive({ id: null, title: '', description: '' })

function openEditor(row) {
  if (row) { Object.assign(editForm, row) } else { editForm.id = null; editForm.title = ''; editForm.description = '' }
  showEditor.value = true
}

async function handleSave() {
  try { await saveQuiz(editForm); ElMessage.success('保存成功'); showEditor.value = false; fetchData() } catch { /* ignore */ }
}

async function handleToggle(id, status) {
  try { await toggleQuizStatus(id, status); ElMessage.success('操作成功'); fetchData() } catch { /* ignore */ }
}

async function fetchData() {
  try { const r = await getQuizManageList(); quizzes.value = r.data?.records || r.data || [] } catch { /* ignore */ }
}

onMounted(fetchData)
</script>
