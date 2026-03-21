import request from '@/utils/request'

/**
 * 每日心情打卡
 */
export function moodCheckIn(data) {
  return request.post('/mood/checkin', data)
}

/**
 * 获取今日打卡记录
 */
export function getTodayMood() {
  return request.get('/mood/today')
}

/**
 * 获取某月的心情记录
 */
export function getMonthlyMoods(year, month) {
  return request.get('/mood/monthly', { params: { year, month } })
}

/**
 * 获取月度情绪统计
 */
export function getMoodStats(year, month) {
  return request.get('/mood/stats', { params: { year, month } })
}
