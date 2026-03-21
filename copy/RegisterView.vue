<template>
  <div class="auth-page">
    <div class="auth-container">
      <!-- 左侧装饰 -->
      <div class="auth-illustration">
        <div class="illustration-content">
          <div class="illustration-icon">🌿</div>
          <h2>加入我们</h2>
          <p>注册账号，开启你的<br/>心理健康成长之旅</p>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="auth-form-wrapper">
        <div class="auth-form-inner">
          <h1 class="form-title">创建账号</h1>
          <p class="form-subtitle">使用学号或邮箱注册</p>

          <el-form
            ref="regFormRef"
            :model="regForm"
            :rules="rules"
            size="large"
            @submit.prevent="handleRegister"
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
                {{ loading ? '注册中...' : '注 册' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            已有账号？
            <router-link to="/login" class="link-login">返回登录</router-link>
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
  username: [{ required: true, message: '请输入学号或邮箱', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
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
    ElMessage.success('注册成功！请登录')
    router.push('/login')
  } catch (e) {
    // 错误已被拦截器处理
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
  min-height: 520px;
}

.auth-illustration {
  flex: 1;
  background: linear-gradient(135deg, #B8A9C9 0%, #8B7BAF 50%, #5CB8A5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.illustration-content {
  text-align: center;
  color: #fff;
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

.link-login {
  color: var(--color-primary);
  font-weight: 600;
}

@media (max-width: 768px) {
  .auth-illustration {
    display: none;
  }
}
</style>
