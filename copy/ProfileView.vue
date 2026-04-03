<template>
  <div class="profile-page page-wrapper">
    <h1 class="section-title">个人中心</h1>

    <div class="profile-grid">
      <!-- 个人信息卡片 -->
      <div class="card profile-card">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="triggerUpload">
            <el-avatar :size="80" :src="avatarUrl" class="profile-avatar">
              {{ profileForm.realName?.charAt(0) || profileForm.username?.charAt(0)?.toUpperCase() || '?' }}
            </el-avatar>
            <div class="avatar-overlay">
              <span>📷</span>
            </div>
          </div>
          <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleUpload" />
          <p class="avatar-hint">点击头像更换</p>
        </div>

        <!-- 信息表单 -->
        <el-form :model="profileForm" label-position="top" class="profile-form">
          <el-form-item label="用户名">
            <el-input :model-value="profileForm.username" disabled>
              <template #prefix><span>🆔</span></template>
            </el-input>
          </el-form-item>

          <el-form-item label="昵称 / 姓名">
            <el-input v-model="profileForm.realName" placeholder="请输入昵称或真实姓名" clearable>
              <template #prefix><span>✏️</span></template>
            </el-input>
          </el-form-item>

          <el-form-item label="邮箱">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" clearable>
              <template #prefix><span>📧</span></template>
            </el-input>
          </el-form-item>

          <el-form-item label="性别">
            <el-radio-group v-model="profileForm.gender">
              <el-radio :label="1">♂ 男</el-radio>
              <el-radio :label="0">♀ 女</el-radio>
              <el-radio :label="2">🌈 其他</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="个人简介">
            <el-input v-model="profileForm.intro" type="textarea" :rows="3" placeholder="介绍一下自己吧..." maxlength="200" show-word-limit />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" round :loading="saving" @click="handleSave" style="width:100%">
              💾 保存资料
            </el-button>
          </el-form-item>
        </el-form>

        <el-divider style="margin: 8px 0 16px" />
        <el-button type="primary" plain round size="small" @click="showPwdDialog = true">🔑 修改密码</el-button>
      </div>

      <!-- 右侧：角色 + 测评历史 -->
      <div class="right-panel">
        <!-- 角色卡片 -->
        <div class="card role-card">
          <div class="role-info">
            <span class="role-icon">{{ { STUDENT: '🎓', TEACHER: '👨‍🏫', ADMIN: '🛡️' }[authStore.role] || '👤' }}</span>
            <div>
              <h3 class="role-name">{{ profileForm.realName || profileForm.username }}</h3>
              <el-tag :type="{ STUDENT: '', TEACHER: 'success', ADMIN: 'danger' }[authStore.role]" round>
                {{ { STUDENT: '学生', TEACHER: '咨询师', ADMIN: '管理员' }[authStore.role] || '用户' }}
              </el-tag>
            </div>
          </div>
          <div class="role-meta">
            <span v-if="profileForm.email">📧 {{ profileForm.email }}</span>
            <span>📅 注册于 {{ profileForm.createTime?.substring(0, 10) || '—' }}</span>
          </div>
        </div>

        <!-- 测评历史 -->
        <div class="card history-card">
          <h3 class="card-title">📊 测评历史</h3>
          <div v-if="quizResults.length" class="result-list">
            <div v-for="r in quizResults" :key="r.id" class="result-item" @click="$router.push(`/quiz/result/${r.id}`)">
              <span class="result-name">{{ r.quizTitle || '量表测评' }}</span>
              <span class="result-score">{{ r.totalScore }} 分</span>
              <span class="result-date">{{ r.createTime?.substring(0, 10) }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无测评记录" :image-size="60" />
        </div>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { getMyProfile, updateProfile, changePassword } from '@/api/auth'
import { getMyResults } from '@/api/quiz'
import request from '@/utils/request'

const authStore = useAuthStore()
const quizResults = ref([])
const showPwdDialog = ref(false)
const saving = ref(false)
const fileInput = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

const profileForm = reactive({
  username: '',
  realName: '',
  email: '',
  gender: 2,
  intro: '',
  avatar: '',
  createTime: ''
})

// 头像 URL 兼容处理：完整路径或仅文件名
const avatarUrl = computed(() => {
  if (!profileForm.avatar) return ''
  if (profileForm.avatar.startsWith('/uploads/') || profileForm.avatar.startsWith('http')) {
    return profileForm.avatar
  }
  return `/uploads/${profileForm.avatar}`
})

// 加载个人信息
async function loadProfile() {
  try {
    const res = await getMyProfile()
    if (res.data) {
      Object.assign(profileForm, {
        username: res.data.username || '',
        realName: res.data.realName || '',
        email: res.data.email || '',
        gender: res.data.gender ?? 2,
        intro: res.data.intro || '',
        avatar: res.data.avatar || '',
        createTime: res.data.createTime || ''
      })
    }
  } catch { /* ignore */ }
}

// 触发文件选择
function triggerUpload() {
  fileInput.value?.click()
}

// 头像上传
async function handleUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/common/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200 && res.data) {
      // 后端返回 /uploads/xxx.jpg 格式，直接使用
      profileForm.avatar = res.data
      ElMessage.success('头像上传成功，请点击保存资料')
    }
  } catch {
    ElMessage.error('上传失败')
  }
  // 重置 input 以便重复选同一文件
  event.target.value = ''
}

// 保存资料
async function handleSave() {
  saving.value = true
  try {
    await updateProfile({
      realName: profileForm.realName,
      email: profileForm.email,
      gender: profileForm.gender,
      intro: profileForm.intro,
      avatar: profileForm.avatar
    })
    ElMessage.success('资料保存成功')
    // 同步更新 store 中的用户信息
    authStore.updateUserInfo({
      realName: profileForm.realName,
      avatar: profileForm.avatar
    })
  } catch { /* ignore */ }
  saving.value = false
}

// 修改密码
async function handleChangePwd() {
  if (!pwdForm.newPassword || pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  try {
    await changePassword(pwdForm)
    ElMessage.success('密码修改成功')
    showPwdDialog.value = false
  } catch { /* ignore */ }
}

onMounted(() => {
  loadProfile()
  getMyResults().then(r => { quizResults.value = r.data || [] }).catch(() => {})
})
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 20px;
}
.profile-card {
  text-align: center;
  padding: 32px 24px;
}

/* 头像区域 */
.avatar-section {
  margin-bottom: 20px;
}
.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
  transition: transform 0.2s;
}
.avatar-wrapper:hover {
  transform: scale(1.05);
}
.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}
.profile-avatar {
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple));
  color: #fff;
  font-size: 28px;
  font-weight: 700;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  opacity: 0;
  transition: opacity 0.2s;
}
.avatar-hint {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 6px;
}

/* 表单 */
.profile-form {
  text-align: left;
}
.profile-form :deep(.el-form-item__label) {
  font-weight: 600;
  font-size: 13px;
}

/* 右侧 */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 角色卡片 */
.role-card {
  padding: 24px;
}
.role-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}
.role-icon {
  font-size: 40px;
}
.role-name {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}
.role-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: var(--color-text-muted);
}

/* 测评历史 */
.history-card {
  padding: 24px;
  flex: 1;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}
.result-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.result-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: var(--color-bg);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.result-item:hover {
  background: var(--color-primary-lighter);
}
.result-name {
  font-weight: 500;
}
.result-score {
  color: var(--color-primary);
  font-weight: 600;
}
.result-date {
  font-size: 12px;
  color: var(--color-text-muted);
}

@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
