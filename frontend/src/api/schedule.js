import request from '@/utils/request'

export function publishSchedule(data) {
  return request.post('/schedule/publish', data)
}

export function getMySchedule(params) {
  return request.get('/schedule/my', { params })
}

export function getTeacherSchedule(teacherId, params) {
  return request.get(`/schedule/teacher/${teacherId}`, { params })
}
