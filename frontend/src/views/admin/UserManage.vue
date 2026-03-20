<template>
  <div class="user-manage-page">
    <h1 class="section-title">用户管理</h1>
    <el-table :data="users" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">{{ { STUDENT: '学生', TEACHER: '咨询师', ADMIN: '管理员' }[row.role] || row.role }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '封禁' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="danger" @click="handleStatus(row.id, 0)">封禁</el-button>
          <el-button v-else size="small" type="success" @click="handleStatus(row.id, 1)">解禁</el-button>
          <el-button size="small" @click="handleReset(row.id)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, toggleUserStatus, resetPassword } from '@/api/admin'

const users = ref([])
const loading = ref(false)

async function fetchUsers() {
  loading.value = true
  try { const r = await getUserList(); users.value = r.data?.records || r.data || [] } catch { /* ignore */ }
  loading.value = false
}

async function handleStatus(id, status) {
  try { await toggleUserStatus(id, status); ElMessage.success('操作成功'); fetchUsers() } catch { /* ignore */ }
}

async function handleReset(id) {
  await ElMessageBox.confirm('确定重置该用户密码吗？', '提示')
  try { await resetPassword(id); ElMessage.success('密码已重置') } catch { /* ignore */ }
}

onMounted(fetchUsers)
</script>
