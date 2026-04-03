<template>
  <header class="app-header">
    <div class="header-inner container">
      <router-link to="/" class="header-brand">
        <span class="brand-icon">🌿</span>
        <span class="brand-text">心灵树洞</span>
      </router-link>

      <nav class="header-nav" :class="{ 'nav-open': mobileMenuOpen }">
        <router-link to="/" class="nav-link" active-class="" exact-active-class="nav-active" @click="mobileMenuOpen = false">首页</router-link>
        <router-link to="/articles" class="nav-link" active-class="nav-active" @click="mobileMenuOpen = false">心理科普</router-link>
        <router-link to="/treehole" class="nav-link" active-class="nav-active" @click="mobileMenuOpen = false">树洞广场</router-link>
        <router-link to="/quiz" class="nav-link" active-class="nav-active" @click="mobileMenuOpen = false">心理测评</router-link>
        <router-link to="/counselors" class="nav-link" active-class="nav-active" @click="mobileMenuOpen = false">预约咨询</router-link>
      </nav>

      <div class="header-actions">
        <template v-if="authStore.isLoggedIn">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-trigger">
              <el-avatar :size="34" :src="headerAvatarUrl" class="user-avatar">
                {{ displayName?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="user-name hide-mobile">{{ displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="appointments">
                  <el-icon><Calendar /></el-icon>我的预约
                </el-dropdown-item>
                <el-dropdown-item command="chat">
                  <el-icon><ChatDotRound /></el-icon>在线咨询
                </el-dropdown-item>
                <el-dropdown-item command="mood">
                  <el-icon><Sunrise /></el-icon>心情打卡
                </el-dropdown-item>
                <el-dropdown-item v-if="authStore.isTeacher || authStore.isAdmin" command="teacher" divided>
                  <el-icon><Setting /></el-icon>工作台
                </el-dropdown-item>
                <el-dropdown-item v-if="authStore.isAdmin" command="admin">
                  <el-icon><Monitor /></el-icon>管理后台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login">
            <el-button type="primary" round size="small">登录</el-button>
          </router-link>
          <router-link to="/register" class="hide-mobile">
            <el-button round size="small">注册</el-button>
          </router-link>
        </template>

        <!-- 移动端菜单按钮 -->
        <div class="mobile-toggle" @click="mobileMenuOpen = !mobileMenuOpen">
          <el-icon :size="22"><component :is="mobileMenuOpen ? 'Close' : 'Menu'" /></el-icon>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  ArrowDown, User, Calendar, ChatDotRound, Sunrise,
  Setting, Monitor, SwitchButton, Close, Menu
} from '@element-plus/icons-vue'

const authStore = useAuthStore()
const router = useRouter()
const mobileMenuOpen = ref(false)

// 显示名称：优先昵称，fallback 到用户名
const displayName = computed(() => authStore.userInfo?.realName || authStore.username)

// 头像 URL：兼容完整路径和纯文件名
const headerAvatarUrl = computed(() => {
  const av = authStore.userInfo?.avatar
  if (!av) return ''
  if (av.startsWith('/uploads/') || av.startsWith('http')) return av
  return `/uploads/${av}`
})

function handleCommand(cmd) {
  switch (cmd) {
    case 'profile': router.push('/profile'); break
    case 'appointments': router.push('/appointments'); break
    case 'chat': router.push('/chat'); break
    case 'mood': router.push('/mood'); break
    case 'teacher': router.push('/teacher'); break
    case 'admin': router.push('/admin'); break
    case 'logout':
      authStore.logout()
      router.push('/login')
      break
  }
}
</script>

<style scoped>
.app-header {
  height: 64px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border-light);
  position: sticky;
  top: 0;
  z-index: 999;
}

.header-inner {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  flex-shrink: 0;
}

.brand-icon {
  font-size: 26px;
}

.brand-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 6px;
}

.nav-link {
  position: relative;
  padding: 8px 12px;
  color: var(--color-text-secondary);
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s ease;
  text-decoration: none;
  background: transparent;
}

.nav-link:hover {
  color: var(--color-primary);
  background: rgba(42, 157, 143, 0.04);
  border-radius: 8px;
}

.nav-link.nav-active {
  color: var(--color-primary);
  font-weight: 600;
  background: transparent;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 3px;
  border-radius: 2px;
  background: var(--color-primary);
  transition: width 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.nav-link.nav-active::after {
  width: 18px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background var(--transition-fast);
}

.user-trigger:hover {
  background: var(--color-bg-hover);
}

.user-avatar {
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple));
  color: #fff;
  font-weight: 700;
  font-size: 14px;
}

.user-name {
  font-size: 14px;
  color: var(--color-text-primary);
  font-weight: 500;
}

.mobile-toggle {
  display: none;
  cursor: pointer;
  padding: 6px;
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .header-nav {
    display: none;
    position: absolute;
    top: 64px;
    left: 0;
    right: 0;
    background: #fff;
    flex-direction: column;
    padding: 12px;
    border-bottom: 1px solid var(--color-border);
    box-shadow: var(--shadow-md);
  }

  .header-nav.nav-open {
    display: flex;
  }

  .mobile-toggle {
    display: block;
  }

  .hide-mobile {
    display: none !important;
  }
}
</style>
