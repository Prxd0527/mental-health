import request from '@/utils/request'

export function toggleUserStatus(id, status) {
  return request.post(`/admin/user/${id}/status/${status}`)
}

export function saveArticle(data) {
  return request.post('/admin/article/save', data)
}

export function toggleArticleStatus(id, status) {
  return request.post(`/admin/article/${id}/status/${status}`)
}

export function saveQuiz(data) {
  return request.post('/admin/quiz/save', data)
}

export function toggleQuizStatus(id, status) {
  return request.post(`/admin/quiz/${id}/status/${status}`)
}

export function getUserList(params) {
  return request.get('/admin/user/list', { params })
}

export function resetPassword(userId) {
  return request.post(`/admin/user/${userId}/reset-password`)
}

export function getDashboardStats() {
  return request.get('/admin/dashboard/stats')
}

export function getArticleManageList(params) {
  return request.get('/admin/article/list', { params })
}

export function getQuizManageList(params) {
  return request.get('/admin/quiz/list', { params })
}

// ---- 题目管理 ----
export function getQuizQuestions(quizId) {
  return request.get(`/admin/quiz/${quizId}/questions`)
}

export function saveQuestion(data) {
  return request.post('/admin/question/save', data)
}

export function deleteQuestion(id) {
  return request.delete(`/admin/question/${id}`)
}
