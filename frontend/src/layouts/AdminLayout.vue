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
        class="aside-menu"
        background-color="transparent"
        text-color="#E8E4DF"
        active-text-color="#5CB8A5"
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
  User, Document, Notebook, ArrowDown, Expand, Fold
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
  background: linear-gradient(180deg, #2C3E50 0%, #1a2332 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
}

.aside-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
}

.aside-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
}

.aside-menu :deep(.el-menu-item-group__title) {
  color: #6B7B8D;
  font-size: 12px;
  padding-left: 20px;
}

.aside-collapse-btn {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #A0AEC0;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  transition: color 0.2s;
  flex-shrink: 0;
}

.aside-collapse-btn:hover {
  color: #5CB8A5;
}

.admin-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid var(--color-border-light);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
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
