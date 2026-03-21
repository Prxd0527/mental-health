import request from '@/utils/request'

/**
 * 提交举报
 */
export function submitReport(data) {
  return request.post('/report/submit', data)
}

/**
 * 获取举报列表（管理员）
 */
export function getReports(params) {
  return request.get('/report/admin/list', { params })
}

/**
 * 处理举报（管理员）
 */
export function processReport(id, data) {
  return request.post(`/report/admin/${id}/process`, null, { params: data })
}
