import request from '@/utils/request'

export function submitAppointment(data) {
  return request.post('/appointment/submit', data)
}

export function getMyAppointments() {
  return request.get('/appointment/my')
}

export function getTeacherAppointments(params) {
  return request.get('/appointment/teacher/list', { params })
}

export function processAppointment(params) {
  return request.post('/appointment/teacher/process', null, { params })
}

export function cancelAppointment(id) {
  return request.post(`/appointment/${id}/cancel`)
}
