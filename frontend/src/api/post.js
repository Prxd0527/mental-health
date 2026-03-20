import request from '@/utils/request'

/**
 * 树洞广场列表
 */
export function getPostSquare(params) {
  return request.get('/post/square', { params })
}

/**
 * 树洞详情
 */
export function getPostDetail(id) {
  return request.get(`/post/${id}`)
}

/**
 * 发布树洞
 */
export function publishPost(data) {
  return request.post('/post/publish', data)
}

/**
 * 点赞树洞
 */
export function likePost(id) {
  return request.post(`/post/${id}/like`)
}
