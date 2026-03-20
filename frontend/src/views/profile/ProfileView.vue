<template>
  <div class="profile-page page-wrapper">
    <h1 class="section-title">个人中心</h1>

    <div class="profile-grid">
      <!-- 个人信息卡片 -->
      <div class="card profile-card">
        <el-avatar :size="64" class="profile-avatar">{{ authStore.username?.charAt(0)?.toUpperCase() }}</el-avatar>
        <h3>{{ authStore.username }}</h3>
        <el-tag>{{ roleText }}</el-tag>
        <el-button text type="primary" @click="showPwdDialog = true" style="margin-top:12px">修改密码</el-button>
      </div>

      <!-- 我的测评历史 -->
      <div class="card history-card">
        <h3 class="card-title">📊 测评历史</h3>
        <div v-if="quizResults.length" class="result-list">
          <div v-for="r in quizResults" :key="r.id" class="result-item" @click="$router.push(`/quiz/result/${r.id}`)">
            <span class="result-name">{{ r.quizTitle || '量表测评' }}</span>
            <span class="result-score">{{ r.totalScore }} 分</span>
            <span class="result-date">{{ r.createTime?.substring(0,10) }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无测评记录" :image-size="60" />
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPwdDialog" title="修改密码" width="400px">
      <el-form :model="pwdForm">
        <el-form-item label="旧密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPwdDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePwd">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/auth'
import { getMyResults } from '@/api/quiz'

const authStore = useAuthStore()
const roleText = computed(() => ({ STUDENT: '学生', TEACHER: '咨询师', ADMIN: '管理员' }[authStore.role] || '用户'))
const quizResults = ref([])
const showPwdDialog = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

async function handleChangePwd() {
  if (!pwdForm.newPassword || pwdForm.newPassword.length < 6) { ElMessage.warning('新密码至少6位'); return }
  try { await changePassword(pwdForm); ElMessage.success('密码修改成功'); showPwdDialog.value = false } catch { /* ignore */ }
}

onMounted(async () => { try { const r = await getMyResults(); quizResults.value = r.data || [] } catch { /* ignore */ } })
</script>

<style scoped>
.profile-grid { display: grid; grid-template-columns: 280px 1fr; gap: 20px; }
.profile-card { text-align: center; padding: 32px; }
.profile-avatar { background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple)); color: #fff; font-size: 24px; font-weight: 700; margin-bottom: 12px; }
.profile-card h3 { font-size: 18px; margin-bottom: 8px; }
.history-card { padding: 24px; }
.card-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.result-list { display: flex; flex-direction: column; gap: 8px; }
.result-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: var(--color-bg); border-radius: var(--radius-sm); cursor: pointer; transition: background var(--transition-fast); }
.result-item:hover { background: var(--color-primary-lighter); }
.result-name { font-weight: 500; }
.result-score { color: var(--color-primary); font-weight: 600; }
.result-date { font-size: 12px; color: var(--color-text-muted); }
@media (max-width: 768px) { .profile-grid { grid-template-columns: 1fr; } }
</style>
