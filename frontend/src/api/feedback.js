import request from '@/utils/request'

/**
 * 提交咨询评价
 */
export function submitFeedback(data) {
  return request.post('/feedback/submit', data)
}

/**
 * 获取教师评价列表
 */
export function getTeacherFeedbacks(teacherId) {
  return request.get(`/feedback/teacher/${teacherId}`)
}

/**
 * 检查预约是否已评价
 */
export function checkEvaluated(appointmentId) {
  return request.get(`/feedback/check/${appointmentId}`)
}
