import request from '@/utils/request'

/**
 * 获取评论列表（按帖子ID）
 */
export function getComments(postId) {
  return request.get(`/comment/list/${postId}`)
}

/**
 * 获取树形评论
 */
export function getCommentsTree(postId) {
  return request.get(`/comment/tree/${postId}`)
}

/**
 * 发表评论
 */
export function publishComment(data) {
  return request.post('/comment/publish', data)
}

/**
 * 点赞评论
 */
export function likeComment(id) {
  return request.post(`/comment/${id}/like`)
}
