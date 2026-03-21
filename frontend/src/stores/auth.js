import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 用户认证 Store
 * - 管理 Token 持久化
 * - 解析 JWT payload 获取用户信息
 * - 提供角色判断计算属性
 */
export const useAuthStore = defineStore('auth', () => {
  // --- State ---
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // --- Getters ---
  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => userInfo.value.userId || null)
  const username = computed(() => userInfo.value.username || '')
  const role = computed(() => userInfo.value.role || '')
  const isStudent = computed(() => role.value === 'STUDENT')
  const isTeacher = computed(() => role.value === 'TEACHER')
  const isAdmin = computed(() => role.value === 'ADMIN')
  const hasAgreedPrivacy = computed(() => {
    const key = `privacy_agreed_${userId.value}`
    return localStorage.getItem(key) === 'true'
  })

  // --- Actions ---

  /**
   * 登录成功后设置 Token 并解析用户信息
   */
  function setToken(newToken, extraInfo = {}) {
    token.value = newToken
    localStorage.setItem('token', newToken)

    // 解析 JWT payload（Base64Url 编码的第二段）
    try {
      const payload = JSON.parse(atob(newToken.split('.')[1]))
      const info = {
        userId: payload.userId,
        username: payload.sub,
        role: payload.role,
        realName: extraInfo.realName || '',
        avatar: extraInfo.avatar || ''
      }
      userInfo.value = info
      localStorage.setItem('userInfo', JSON.stringify(info))
    } catch (e) {
      console.error('JWT 解析失败:', e)
    }
  }

  /**
   * 更新用户信息（修改资料后同步 store）
   */
  function updateUserInfo(partial) {
    userInfo.value = { ...userInfo.value, ...partial }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  /**
   * 退出登录 - 清除所有状态
   */
  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function agreePrivacy() {
    const key = `privacy_agreed_${userId.value}`
    localStorage.setItem(key, 'true')
  }

  return {
    token, userInfo,
    isLoggedIn, userId, username, role,
    isStudent, isTeacher, isAdmin, hasAgreedPrivacy,
    setToken, updateUserInfo, logout, agreePrivacy
  }
})
