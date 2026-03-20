import request from '@/utils/request'

/**
 * 科普文章列表（分页+搜索）
 */
export function getArticleList(params) {
  return request.get('/article/list', { params })
}

/**
 * 文章详情
 */
export function getArticleDetail(id) {
  return request.get(`/article/${id}`)
}
