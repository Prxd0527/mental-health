<template>
  <div class="auth-page">
    <div class="auth-container">
      <!-- 左侧装饰 -->
      <div class="auth-illustration">
        <div class="illustration-content">
          <div class="illustration-icon">🌱</div>
          <h2>心灵树洞</h2>
          <p>一个安全、温暖的心理健康平台<br/>用心倾听每一个声音</p>
          <div class="floating-shapes">
            <span class="shape shape-1">🍃</span>
            <span class="shape shape-2">🌸</span>
            <span class="shape shape-3">☁️</span>
            <span class="shape shape-4">🦋</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="auth-form-wrapper">
        <div class="auth-form-inner">
          <h1 class="form-title">欢迎回来</h1>
          <p class="form-subtitle">登录你的账号，开始心灵之旅</p>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="rules"
            size="large"
            @submit.prevent="handleLogin"
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
            <router-link to="/register" class="link-register">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { login } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入学号或邮箱', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const form = loginFormRef.value
  if (!form) return
  await form.validate()

  loading.value = true
  try {
    const res = await login(loginForm.username, loginForm.password)
    authStore.setToken(res.data.token || res.data)
    ElMessage.success('登录成功！')
    // 如果有重定向地址则跳转，否则跳首页
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    // 错误已被 Axios 拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  padding: 20px;
}

.auth-container {
  display: flex;
  max-width: 900px;
  width: 100%;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  background: #fff;
  min-height: 500px;
}

/* --- 左侧装饰面板 --- */
.auth-illustration {
  flex: 1;
  background: linear-gradient(135deg, #5CB8A5 0%, #3D9E8B 50%, #B8A9C9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.illustration-content {
  text-align: center;
  color: #fff;
  z-index: 1;
  padding: 40px;
}

.illustration-icon {
  font-size: 64px;
  margin-bottom: 16px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.illustration-content h2 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
}

.illustration-content p {
  font-size: 14px;
  opacity: 0.9;
  line-height: 1.8;
}

.floating-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  font-size: 24px;
  opacity: 0.3;
  animation: drift 6s ease-in-out infinite;
}

.shape-1 { top: 15%; left: 10%; animation-delay: 0s; }
.shape-2 { top: 60%; right: 15%; animation-delay: 1.5s; }
.shape-3 { bottom: 20%; left: 20%; animation-delay: 3s; }
.shape-4 { top: 30%; right: 25%; animation-delay: 4.5s; }

@keyframes drift {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  25% { transform: translate(10px, -15px) rotate(5deg); }
  50% { transform: translate(-5px, 10px) rotate(-3deg); }
  75% { transform: translate(8px, 5px) rotate(2deg); }
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

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.form-subtitle {
  color: var(--color-text-muted);
  font-size: 14px;
  margin-bottom: 32px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  color: var(--color-text-muted);
  font-size: 14px;
}

.link-register {
  color: var(--color-primary);
  font-weight: 600;
}

@media (max-width: 768px) {
  .auth-illustration {
    display: none;
  }
}
</style>
