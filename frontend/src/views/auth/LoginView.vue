<template>
  <div class="auth-page">
    <!-- 动态发光背景 -->
    <div class="blob blob-1"></div>
    <div class="blob blob-2"></div>
    <div class="blob blob-3"></div>

    <div class="auth-container glass-panel">
      <!-- 左侧装饰 (使用渐变加柔滑背景) -->
      <div class="auth-illustration">
        <div class="illustration-content">
          <div class="illustration-icon">✨</div>
          <h2>心灵树洞</h2>
          <p>遇见更真实的自己<br/>发现生活中的小确幸</p>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="auth-form-wrapper">
        <div class="auth-form-inner">
          <div class="form-header">
            <h1 class="form-title">欢迎回来</h1>
            <p class="form-subtitle">期待与你相遇，开始心灵之旅</p>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="rules"
            size="large"
            @submit.prevent="handleLogin"
            class="custom-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="学号 / 邮箱"
                prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                round
                class="submit-btn"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            还没有账号？
            <router-link to="/register" class="link-register">创造新连接</router-link>
          </div>
        </div>
      </div>
    </div>

    <PrivacyAgreement ref="privacyRef" @agreed="onPrivacyAgreed" />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { login } from '@/api/auth'
import PrivacyAgreement from '@/components/PrivacyAgreement.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loginFormRef = ref(null)
const loading = ref(false)
const privacyRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入学号/邮箱', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '最少 6 位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const form = loginFormRef.value
  if (!form) return
  await form.validate()

  loading.value = true
  try {
    const res = await login(loginForm.username, loginForm.password)
    authStore.setToken(res.data.token || res.data, {
      realName: res.data.realName || '',
      avatar: res.data.avatar || ''
    })
    ElMessage.success('欢迎回来！')

    if (!authStore.hasAgreedPrivacy) {
      privacyRef.value?.open()
    } else {
      navigateAfterLogin()
    }
  } catch (e) {
    // 拦截器处理错误
  } finally {
    loading.value = false
  }
}

function onPrivacyAgreed() {
  authStore.agreePrivacy()
  navigateAfterLogin()
}

function navigateAfterLogin() {
  const redirect = route.query.redirect || '/'
  router.push(redirect)
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f7f9fc;
  position: relative;
  overflow: hidden;
  padding: 24px;
}

/* 动态发光背景效果 */
.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  z-index: 0;
  animation: blobFloat 12s infinite alternate ease-in-out;
}

.blob-1 {
  width: 400px;
  height: 400px;
  background: var(--color-primary-light);
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.blob-2 {
  width: 500px;
  height: 500px;
  background: var(--color-accent-purple);
  bottom: -200px;
  right: -100px;
  animation-delay: -3s;
}

.blob-3 {
  width: 300px;
  height: 300px;
  background: var(--color-accent-orange);
  top: 30%;
  left: 40%;
  animation-duration: 15s;
  animation-delay: -5s;
  opacity: 0.4;
}

@keyframes blobFloat {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -50px) scale(1.1); }
  100% { transform: translate(-20px, 20px) scale(0.9); }
}

.auth-container {
  display: flex;
  max-width: 900px;
  width: 100%;
  z-index: 10;
  overflow: hidden;
  min-height: 540px;
  /* 玻璃拟物通过全局 .glass-panel 提供，移除白色硬底色 */
  padding: 0;
}

/* --- 左侧装饰面板 --- */
.auth-illustration {
  flex: 1;
  background: linear-gradient(135deg, rgba(42, 157, 143, 0.9) 0%, rgba(33, 131, 118, 0.95) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  color: #fff;
}

.auth-illustration::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI4IiBoZWlnaHQ9IjgiPgo8cmVjdCB3aWR0aD0iOCIgaGVpZ2h0PSI4IiBmaWxsPSIjZmZmIiBmaWxsLW9wYWNpdHk9IjAuMDUiPjwvcmVjdD4KPHBhdGggZD0iTTAgMEw4IDhaTTAgOEw4IDBaIiBzdHJva2U9IiNmZmYiIHN0cm9rZS1vcGFjaXR5PSIwLjA1IiBzdHJva2Utd2lkdGg9IjEiPjwvcGF0aD4KPC9zdmc+') repeat;
  opacity: 0.3;
}

.illustration-content {
  text-align: center;
  z-index: 1;
  padding: 40px;
  position: relative;
}

.illustration-icon {
  font-size: 72px;
  margin-bottom: 24px;
  animation: float 4s var(--transition-spring) infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

.illustration-content h2 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 16px;
  letter-spacing: -0.02em;
}

.illustration-content p {
  font-size: 15px;
  opacity: 0.9;
  line-height: 1.8;
  font-weight: 300;
}

/* --- 右侧表单区域 --- */
.auth-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.auth-form-inner {
  width: 100%;
  max-width: 340px;
}

.form-header {
  margin-bottom: 40px;
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 8px;
  letter-spacing: -0.02em;
}

.form-subtitle {
  color: var(--color-text-secondary);
  font-size: 15px;
}

.custom-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 12px;
  box-shadow: 0 0 0 1px var(--color-border) inset;
  background-color: rgba(255, 255, 255, 0.6);
  transition: all var(--transition-fast);
}

.custom-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
  background-color: #fff;
}

.custom-form :deep(.el-input__inner) {
  height: 40px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(42, 157, 143, 0.3);
  margin-top: 12px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(42, 157, 143, 0.4);
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  color: var(--color-text-muted);
  font-size: 14px;
}

.link-register {
  color: var(--color-primary);
  font-weight: 600;
  margin-left: 4px;
}

@media (max-width: 768px) {
  .auth-illustration {
    display: none;
  }
}
</style>
