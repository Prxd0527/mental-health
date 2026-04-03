<template>
  <div class="profile-page page-wrapper">
    <div class="page-header">
      <h1 class="page-title">专属时空档案</h1>
      <p class="page-subtitle">定义你的身份，回顾你的心理发展轨迹</p>
    </div>

    <div class="profile-grid">
      <!-- 个人信息卡片 -->
      <div class="card profile-card glass-panel flex-col">
        <!-- 艺术感背板 -->
        <div class="profile-backdrop"></div>

        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-wrapper shadow-lg" @click="triggerUpload">
            <el-avatar :size="96" :src="avatarUrl" class="profile-avatar">
              {{ profileForm.realName?.charAt(0) || profileForm.username?.charAt(0)?.toUpperCase() || '?' }}
            </el-avatar>
            <div class="avatar-overlay">
              <span>📷</span>
            </div>
          </div>
          <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleUpload" />
          <p class="avatar-hint">轻触更换头像</p>
        </div>

        <!-- 信息表单 -->
        <el-form :model="profileForm" label-position="top" class="profile-form">
          <el-form-item label="用户名 (唯一标识)">
            <el-input :model-value="profileForm.username" disabled class="custom-input glass-input">
              <template #prefix><span class="input-icon">🆔</span></template>
            </el-input>
          </el-form-item>

          <el-form-item label="姓名 / 昵称">
            <el-input v-model="profileForm.realName" placeholder="怎么称呼您？" clearable class="custom-input">
              <template #prefix><span class="input-icon">✏️</span></template>
            </el-input>
          </el-form-item>

          <el-form-item label="联系邮箱">
            <el-input v-model="profileForm.email" placeholder="将用于重要通知" clearable class="custom-input">
              <template #prefix><span class="input-icon">📧</span></template>
            </el-input>
          </el-form-item>

          <el-form-item label="性别认同">
            <el-radio-group v-model="profileForm.gender" class="custom-radio">
              <el-radio :label="1">‍♂️ 男</el-radio>
              <el-radio :label="0">‍♀️ 女</el-radio>
              <el-radio :label="2">🌈 保密</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="心情签名">
            <el-input 
              v-model="profileForm.intro" 
              type="textarea" 
              :rows="4" 
              placeholder="记录当下的自己，写下喜欢的诗句..." 
              maxlength="200" 
              show-word-limit 
              class="custom-textarea"
            />
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" round size="large" :loading="saving" @click="handleSave" class="save-btn shadow-md">
              💾 保存我的档案
            </el-button>
          </div>
        </el-form>

        <div class="security-action">
          <el-button type="info" plain text round size="small" @click="showPwdDialog = true">
            <span style="font-size: 14px; margin-right: 4px;">🔑</span> 修改访问密码
          </el-button>
        </div>
      </div>

      <!-- 右侧：角色 + 测评历史 -->
      <div class="right-panel">
        <!-- 身份凭证卡片 -->
        <div class="card role-card glass-panel-light">
          <div class="role-bg-decorator"></div>
          <div class="role-content">
            <div class="role-icon-wrapper shadow-sm">
              <span class="role-icon">{{ { STUDENT: '🎓', TEACHER: '👨‍🏫', ADMIN: '🛡️' }[authStore.role] || '👤' }}</span>
            </div>
            <div class="role-text">
              <h3 class="role-name">{{ profileForm.realName || profileForm.username }}</h3>
              <el-tag :type="{ STUDENT: '', TEACHER: 'success', ADMIN: 'danger' }[authStore.role]" round effect="dark" class="role-tag shadow-sm">
                {{ { STUDENT: '心理探索者 (学生)', TEACHER: '专业指导者 (咨询师)', ADMIN: '系统守护者 (管理)' }[authStore.role] || '用户' }}
              </el-tag>
            </div>
          </div>
          <div class="role-meta">
            <span class="meta-item"><i class="meta-icon">📧</i> {{ profileForm.email || '未绑定邮箱' }}</span>
            <span class="meta-item"><i class="meta-icon">📅</i> 加入于 {{ profileForm.createTime?.substring(0, 10) || '初次相见' }}</span>
          </div>
        </div>

        <!-- 测评生命线 -->
        <div class="card history-card glass-panel-light flex-col">
          <div class="history-header">
            <h3 class="card-title">📊 我的心理量表足迹</h3>
            <el-button text size="small" @click="$router.push('/quizzes')">探索更多</el-button>
          </div>
          
          <div v-if="quizResults.length" class="result-list scroll-invisible">
            <transition-group name="fade-slide">
              <div v-for="(r, idx) in quizResults" :key="r.id" class="result-item" @click="$router.push(`/quiz/result/${r.id}`)" :style="{ 'animation-delay': `${idx * 0.05}s` }">
                <div class="result-icon-box">
                  <span>📝</span>
                </div>
                <div class="result-item-main">
                  <span class="result-name">{{ r.quizTitle || '量表测评' }}</span>
                  <span class="result-date">{{ r.createTime?.substring(0, 10) }}</span>
                </div>
                <div class="result-item-score">
                  <span class="score-val">{{ r.totalScore }}</span>
                  <span class="score-unit">分</span>
                </div>
              </div>
            </transition-group>
          </div>
          <div class="empty-state-small" v-else>
             <el-empty description="当前还未完成任何心理自测，去试试吧！" :image-size="80" />
          </div>
        </div>
      </div>
    </div>

    <!-- 弹出的科技感修改密码框 -->
    <el-dialog v-model="showPwdDialog" title="安全设置" width="420px" class="glass-dialog custom-dialog" :close-on-click-modal="false">
      <p style="color: var(--color-text-secondary); margin-bottom: 20px; font-size: 14px;">定期修改密码有助于保护你的隐私信息安全。</p>
      <el-form :model="pwdForm" label-position="top">
        <el-form-item label="当前秘密凭证">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="输入原密码" class="custom-input" />
        </el-form-item>
        <el-form-item label="新的秘密凭证">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="输入新密码 (至少6位)" class="custom-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPwdDialog = false" round plain>暂不修改</el-button>
        <el-button type="primary" @click="handleChangePwd" round>确认重铸密码</el-button>
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

const avatarUrl = computed(() => {
  if (!profileForm.avatar) return ''
  if (profileForm.avatar.startsWith('/uploads/') || profileForm.avatar.startsWith('http')) return profileForm.avatar
  return `/uploads/${profileForm.avatar}`
})

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

function triggerUpload() { fileInput.value?.click() }

async function handleUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning('图片尽量精简，请保持在 5MB 以下'); return }
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/common/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    if (res.code === 200 && res.data) {
      profileForm.avatar = res.data
      ElMessage.success({ message: '全新形象已就绪，请点击应用保存。', center: true })
    }
  } catch { ElMessage.error('哦呀，图片上传途中失散了') }
  event.target.value = ''
}

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
    ElMessage.success({ message: '时光档案记录完毕。', center: true })
    authStore.updateUserInfo({ realName: profileForm.realName, avatar: profileForm.avatar })
  } catch { /* ignore */ }
  saving.value = false
}

async function handleChangePwd() {
  if (!pwdForm.newPassword || pwdForm.newPassword.length < 6) { ElMessage.warning('为了安全，新密码需至少拥有 6 位字符'); return }
  try {
    await changePassword(pwdForm)
    ElMessage.success('凭证变更成功，未来请用新凭证访问')
    showPwdDialog.value = false
  } catch { /* ignore */ }
}

onMounted(() => {
  loadProfile()
  getMyResults().then(r => { quizResults.value = r.data || [] }).catch(() => {})
})
</script>

<style scoped>
.page-header { text-align: center; margin-bottom: 40px; }
.page-title { font-size: 32px; font-weight: 800; color: var(--color-text-primary); margin-bottom: 8px; letter-spacing: -0.02em; }
.page-subtitle { font-size: 15px; color: var(--color-text-secondary); }

.profile-grid { display: grid; grid-template-columns: 420px 1fr; gap: 32px; align-items: start; }
.flex-col { display: flex; flex-direction: column; }
.scroll-invisible::-webkit-scrollbar { width: 0; }

/* 左侧档案卡 */
.profile-card { 
  position: relative; overflow: hidden; padding: 0 0 24px; 
  border-radius: 20px; border: 1px solid rgba(255,255,255,0.6);
}
.profile-backdrop { 
  height: 140px; background: linear-gradient(135deg, var(--color-primary-light), #e2e8f0, var(--color-accent-pink)); 
  position: absolute; top: 0; left: 0; right: 0; z-index: 0;
  opacity: 0.6;
}

.avatar-section { position: relative; z-index: 10; margin-top: 80px; display: flex; flex-direction: column; align-items: center; }
.avatar-wrapper { 
  position: relative; display: inline-block; cursor: pointer; border-radius: 50%; 
  transition: all var(--transition-spring); border: 4px solid #fff; background: #fff;
}
.avatar-wrapper:hover { transform: scale(1.05) translateY(-4px); box-shadow: 0 16px 32px rgba(42, 157, 143, 0.2); }
.profile-avatar { background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple)); color: #fff; font-size: 36px; font-weight: 800; }
.avatar-overlay { 
  position: absolute; inset: 0; border-radius: 50%; background: rgba(0, 0, 0, 0.4); 
  display: flex; align-items: center; justify-content: center; font-size: 28px; opacity: 0; transition: opacity 0.3s;
}
.avatar-wrapper:hover .avatar-overlay { opacity: 1; }
.avatar-hint { font-size: 13px; color: var(--color-text-muted); margin-top: 12px; font-weight: 500; }

.profile-form { padding: 24px 32px 0; z-index: 10; position: relative; }
.profile-form :deep(.el-form-item__label) { font-weight: 600; font-size: 14px; color: var(--color-text-primary); padding-bottom: 8px; }

.input-icon { opacity: 0.8; font-size: 16px; margin-right: 4px; }
.custom-input :deep(.el-input__wrapper) { background: rgba(255, 255, 255, 0.6); box-shadow: 0 0 0 1px rgba(0,0,0,0.05); border-radius: 8px; transition: all 0.3s; }
.custom-input :deep(.el-input__wrapper.is-focus) { background: #fff; box-shadow: 0 0 0 2px rgba(42,157,143,0.3); }
.glass-input :deep(.el-input__wrapper) { background: transparent; box-shadow: none; border-bottom: 1px dashed rgba(0,0,0,0.1); border-radius: 0; }

.custom-textarea :deep(.el-textarea__inner) { background: rgba(255, 255, 255, 0.6); border: 1px solid rgba(0,0,0,0.05); border-radius: 12px; padding: 12px; transition: all 0.3s; resize: none; }
.custom-textarea :deep(.el-textarea__inner:focus) { background: #fff; border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(42,157,143,0.1); }

.form-actions { margin-top: 32px; text-align: center; }
.save-btn { width: 100%; font-weight: 700; height: 48px; font-size: 16px; }
.save-btn:hover { box-shadow: 0 8px 24px rgba(42, 157, 143, 0.3); }

.security-action { text-align: center; padding-top: 16px; border-top: 1px dashed rgba(0,0,0,0.06); margin: 24px 32px 0; }

/* 右侧区域 */
.right-panel { display: flex; flex-direction: column; gap: 24px; }

.role-card { 
  padding: 32px; border-radius: 20px; position: relative; overflow: hidden;
  border: 1px solid rgba(255,255,255,0.7);
}
.role-bg-decorator { position: absolute; right: -40px; top: -40px; width: 160px; height: 160px; background: radial-gradient(circle, var(--color-primary-light) 0%, transparent 70%); opacity: 0.15; z-index: 0; }
.role-content { display: flex; align-items: center; gap: 20px; margin-bottom: 20px; position: relative; z-index: 10; }
.role-icon-wrapper { width: 72px; height: 72px; border-radius: 20px; background: #fff; display: flex; align-items: center; justify-content: center; }
.role-icon { font-size: 36px; }
.role-text { display: flex; flex-direction: column; gap: 8px; align-items: flex-start; }
.role-name { font-size: 24px; font-weight: 800; color: var(--color-text-primary); margin: 0; letter-spacing: -0.01em; line-height: 1; }
.role-tag { font-weight: 600; padding: 0 12px; height: 26px; }

.role-meta { display: flex; gap: 24px; font-size: 14px; color: var(--color-text-secondary); position: relative; z-index: 10; padding-top: 16px; border-top: 1px solid rgba(0,0,0,0.04); }
.meta-item { display: flex; align-items: center; gap: 6px; font-weight: 500; }
.meta-icon { font-style: normal; opacity: 0.8; }

.history-card { flex: 1; padding: 32px; border-radius: 20px; border: 1px solid rgba(255,255,255,0.7); min-height: 380px; }
.history-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.history-header .card-title { font-size: 18px; font-weight: 700; margin: 0; color: var(--color-text-primary); }

.result-list { display: flex; flex-direction: column; gap: 12px; overflow-y: auto; flex: 1; padding-right: 4px; margin-right: -4px; }
.result-item { 
  display: flex; align-items: center; gap: 16px; padding: 16px; 
  background: rgba(255,255,255,0.6); border-radius: 16px; cursor: pointer; 
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); animation: fadeSlideUp 0.6s backwards;
  border: 1px solid rgba(255,255,255,0.4);
}
.result-item:hover { background: #fff; transform: translateX(6px) scale(1.01); box-shadow: 0 8px 24px rgba(0,0,0,0.04); border-color: var(--color-primary-light); }

.result-icon-box { width: 44px; height: 44px; border-radius: 12px; background: rgba(42, 157, 143, 0.1); display: flex; align-items: center; justify-content: center; font-size: 20px; }
.result-item:hover .result-icon-box { background: var(--color-primary); color: #fff; }
.result-item-main { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.result-name { font-weight: 600; font-size: 16px; color: var(--color-text-primary); }
.result-date { font-size: 13px; color: var(--color-text-muted); }
.result-item-score { display: flex; align-items: baseline; gap: 2px; }
.score-val { font-size: 24px; font-weight: 800; color: var(--color-primary); }
.score-unit { font-size: 12px; font-weight: 600; color: var(--color-text-muted); }

.empty-state-small { flex: 1; display: flex; align-items: center; justify-content: center; opacity: 0.8; }

@keyframes fadeSlideUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 900px) {
  .profile-grid { grid-template-columns: 1fr; }
  .profile-card { max-width: 500px; margin: 0 auto 32px; width: 100%; }
}
</style>
