import request from '@/utils/request'

export function getChatHistory(targetId) {
  return request.get('/chat/history', { params: { targetId } })
}

export function getConversationList() {
  return request.get('/chat/conversations')
}
