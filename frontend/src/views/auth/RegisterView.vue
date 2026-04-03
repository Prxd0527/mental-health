<template>
  <div class="auth-page">
    <!-- 动态发光背景 -->
    <div class="blob blob-1"></div>
    <div class="blob blob-2"></div>
    <div class="blob blob-3"></div>

    <div class="auth-container glass-panel">
      <!-- 左侧装饰 -->
      <div class="auth-illustration">
        <div class="illustration-content">
          <div class="illustration-icon">🌱</div>
          <h2>加入空间</h2>
          <p>成为树洞的一员<br/>开始记录你的成长轨迹</p>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="auth-form-wrapper">
        <div class="auth-form-inner">
          <div class="form-header">
            <h1 class="form-title">创建账号</h1>
            <p class="form-subtitle">使用学号或邮箱安全注册</p>
          </div>

          <el-form
            ref="regFormRef"
            :model="regForm"
            :rules="rules"
            size="large"
            @submit.prevent="handleRegister"
            class="custom-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="regForm.username"
                placeholder="学号 / 邮箱"
                prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="regForm.password"
                type="password"
                placeholder="密码（至少 6 位）"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="regForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleRegister"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                round
                class="submit-btn"
                @click="handleRegister"
              >
                {{ loading ? '连接中...' : '建立连接' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            已有通行证？
            <router-link to="/login" class="link-login">去登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const regFormRef = ref(null)
const loading = ref(false)

const regForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== regForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请填写标识(学号或邮箱)', trigger: 'blur' }],
  password: [
    { required: true, message: '记得设置密码', trigger: 'blur' },
    { min: 6, message: '密码安全些，至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '再确认一次吧', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleRegister() {
  const form = regFormRef.value
  if (!form) return
  await form.validate()

  loading.value = true
  try {
    await register(regForm.username, regForm.password)
    ElMessage.success('注册成功！快去登录吧')
    router.push('/login')
  } catch (e) {
    // 拦截器内通知
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
  background: var(--color-accent-purple);
  top: -100px;
  left: -100px;
}

.blob-2 {
  width: 500px;
  height: 500px;
  background: var(--color-primary-light);
  bottom: -200px;
  right: -100px;
  animation-delay: -4s;
}

.blob-3 {
  width: 350px;
  height: 350px;
  background: var(--color-accent-orange);
  top: 40%;
  left: 30%;
  animation-duration: 16s;
  animation-delay: -2s;
  opacity: 0.35;
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
  padding: 0;
}

.auth-illustration {
  flex: 1;
  background: linear-gradient(135deg, rgba(177, 168, 217, 0.9) 0%, rgba(42, 157, 143, 0.9) 100%);
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

.link-login {
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
