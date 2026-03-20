import request from '@/utils/request'

/**
 * 用户注册
 * @param {string} username 学号/邮箱
 * @param {string} password 密码
 */
export function register(username, password) {
  return request.post('/auth/register', { username, password })
}

/**
 * 用户登录
 * @param {string} username 学号/邮箱
 * @param {string} password 密码
 */
export function login(username, password) {
  return request.post('/auth/login', { username, password })
}

/**
 * 获取咨询师列表（公开接口）
 */
export function getTeachers() {
  return request.get('/auth/teachers')
}

/**
 * 获取当前用户信息
 */
export function getMyProfile() {
  return request.get('/user/profile')
}

/**
 * 更新个人信息
 */
export function updateProfile(data) {
  return request.put('/user/profile', data)
}

/**
 * 修改密码
 */
export function changePassword(data) {
  return request.put('/user/password', data)
}
