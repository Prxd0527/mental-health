<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="admin-aside">
      <div class="aside-logo" @click="$router.push('/')">
        <span class="logo-icon">🌿</span>
        <span v-show="!isCollapsed" class="logo-text">心灵树洞</span>
      </div>
      <el-menu
        :default-active="currentRoute"
        :collapse="isCollapsed"
        router
        class="aside-menu modern-menu"
      >
        <!-- 教师端菜单 -->
        <template v-if="authStore.isTeacher || authStore.isAdmin">
          <el-menu-item-group title="咨询师工作台">
            <el-menu-item index="/teacher/schedule">
              <el-icon><Calendar /></el-icon>
              <span>排班管理</span>
            </el-menu-item>
            <el-menu-item index="/teacher/approval">
              <el-icon><Tickets /></el-icon>
              <span>预约审批</span>
            </el-menu-item>
            <el-menu-item index="/teacher/chat">
              <el-icon><ChatDotRound /></el-icon>
              <span>在线咨询</span>
            </el-menu-item>
            <el-menu-item index="/teacher/articles">
              <el-icon><EditPen /></el-icon>
              <span>文章发表</span>
            </el-menu-item>
          </el-menu-item-group>
        </template>
        <!-- 管理端菜单 -->
        <template v-if="authStore.isAdmin">
          <el-menu-item-group title="系统管理">
            <el-menu-item index="/admin/dashboard">
              <el-icon><DataAnalysis /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            <el-menu-item index="/admin/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/articles">
              <el-icon><Document /></el-icon>
              <span>文章管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/quizzes">
              <el-icon><Notebook /></el-icon>
              <span>题库管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/reports">
              <el-icon><Warning /></el-icon>
              <span>举报管理</span>
            </el-menu-item>
          </el-menu-item-group>
        </template>
      </el-menu>

      <div class="aside-collapse-btn" @click="isCollapsed = !isCollapsed">
        <el-icon><component :is="isCollapsed ? 'Expand' : 'Fold'" /></el-icon>
      </div>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <span class="header-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" class="user-avatar">
                {{ authStore.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="user-name">{{ authStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">返回首页</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Calendar, Tickets, ChatDotRound, DataAnalysis,
  User, Document, Notebook, Warning, ArrowDown, Expand, Fold, EditPen
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapsed = ref(false)
const currentRoute = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '管理面板')

function handleCommand(cmd) {
  if (cmd === 'logout') {
    authStore.logout()
    router.push('/login')
  } else if (cmd === 'home') {
    router.push('/')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.admin-aside {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 2px 0 16px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  z-index: 10;
}

.aside-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.logo-icon {
  font-size: 24px;
  filter: drop-shadow(0 2px 4px rgba(42, 157, 143, 0.2));
}

.logo-text {
  font-size: 18px;
  font-weight: 800;
  color: var(--color-text-primary);
  white-space: nowrap;
  letter-spacing: -0.01em;
}

.aside-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
}

/* 现代化悬浮轻量菜单 */
.modern-menu {
  background: transparent;
  padding: 12px 0;
}
.modern-menu :deep(.el-menu-item) {
  color: var(--color-text-secondary);
  font-weight: 500;
  margin: 4px 12px;
  border-radius: 12px;
  height: 44px;
  line-height: 44px;
  transition: all var(--transition-fast);
}
.modern-menu :deep(.el-menu-item:hover) {
  background-color: rgba(42, 157, 143, 0.06);
  color: var(--color-primary);
  transform: translateX(2px);
}
.modern-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(42, 157, 143, 0.12), rgba(42, 157, 143, 0.04));
  color: var(--color-primary-dark);
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(42, 157, 143, 0.1);
}
.modern-menu :deep(.el-menu-item-group__title) {
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 600;
  padding: 16px 20px 8px;
  letter-spacing: 0.05em;
}

.aside-collapse-btn {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--color-text-muted);
  border-top: 1px dashed rgba(0, 0, 0, 0.06);
  transition: all var(--transition-fast);
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.5);
}

.aside-collapse-btn:hover {
  color: var(--color-primary);
  background: rgba(42, 157, 143, 0.05);
}

.admin-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.02);
  z-index: 5;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: -0.01em;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--color-text-secondary);
}

.user-avatar {
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple));
  color: #fff;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
}

.admin-main {
  background: var(--color-bg);
  padding: 24px;
  overflow-y: auto;
}
</style>
