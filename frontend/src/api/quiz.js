import request from '@/utils/request'

export function getQuizList(params) {
  return request.get('/quiz/list', { params })
}

export function getQuizQuestions(id) {
  return request.get(`/quiz/${id}/questions`)
}

export function submitQuiz(id, answers) {
  return request.post(`/quiz/${id}/submit`, answers)
}

export function getMyResults() {
  return request.get('/quiz/my-results')
}
