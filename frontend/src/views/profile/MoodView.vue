<template>
  <div class="mood-page page-wrapper">
    <h1 class="section-title">心情打卡 🌈</h1>

    <!-- 今日打卡区 -->
    <div class="checkin-section card">
      <div class="checkin-header">
        <h3>{{ todayChecked ? '🎉 今天已打卡！' : '今天心情怎么样？' }}</h3>
        <span class="checkin-date">{{ todayStr }}</span>
      </div>

      <div class="mood-selector">
        <div
          v-for="item in moods"
          :key="item.key"
          class="mood-item"
          :class="{ active: selectedMood === item.key, checked: todayChecked && todayMood === item.key }"
          @click="selectMood(item)"
        >
          <span class="mood-emoji">{{ item.emoji }}</span>
          <span class="mood-label">{{ item.label }}</span>
        </div>
      </div>

      <div class="note-section" v-if="selectedMood">
        <el-input
          v-model="note"
          type="textarea"
          :rows="2"
          placeholder="记录一下今天的心情吧（可选）..."
          maxlength="300"
          show-word-limit
        />
        <el-button
          type="primary"
          round
          class="checkin-btn"
          :loading="submitting"
          @click="handleCheckIn"
        >
          {{ todayChecked ? '更新打卡' : '✨ 打卡' }}
        </el-button>
      </div>
    </div>

    <!-- 月度统计区 -->
    <div class="stats-section card">
      <div class="stats-header">
        <h3>月度情绪报告</h3>
        <el-date-picker
          v-model="selectedMonth"
          type="month"
          placeholder="选择月份"
          format="YYYY年MM月"
          value-format="YYYY-MM"
          :clearable="false"
          @change="fetchStats"
          style="width: 160px"
        />
      </div>

      <div class="stats-overview" v-if="stats">
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalDays }}</div>
          <div class="stat-label">打卡天数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ avgEmoji }}</div>
          <div class="stat-label">平均心情</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.avgScore }}</div>
          <div class="stat-label">心情指数</div>
        </div>
      </div>

      <!-- 情绪趋势折线 -->
      <div class="trend-section" v-if="stats && stats.trend && stats.trend.length > 0">
        <h4>情绪趋势</h4>
        <div class="trend-chart">
          <div class="trend-y-axis">
            <span>😄</span>
            <span>😊</span>
            <span>😐</span>
            <span>😞</span>
            <span>😢</span>
          </div>
          <div class="trend-graph">
            <div class="trend-grid">
              <div class="grid-line" v-for="i in 5" :key="i" :style="{ bottom: (i - 1) * 25 + '%' }"></div>
            </div>
            <svg class="trend-svg" :viewBox="`0 0 ${stats.trend.length * 40} 120`" preserveAspectRatio="none">
              <polyline
                :points="trendPoints"
                fill="none"
                stroke="#5CB8A5"
                stroke-width="2"
                stroke-linejoin="round"
                stroke-linecap="round"
              />
              <circle
                v-for="(p, i) in trendPointsArray"
                :key="i"
                :cx="p.x"
                :cy="p.y"
                r="4"
                fill="#5CB8A5"
                stroke="#fff"
                stroke-width="2"
              />
            </svg>
            <div class="trend-x-labels">
              <span v-for="item in stats.trend" :key="item.date" class="x-label">
                {{ item.date.substring(8) }}日
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 心情日历 -->
      <div class="calendar-section" v-if="monthlyLogs.length > 0">
        <h4>打卡日历</h4>
        <div class="calendar-grid">
          <div
            v-for="log in monthlyLogs"
            :key="log.id"
            class="calendar-day"
            :title="log.note || log.mood"
          >
            <span class="day-num">{{ log.logDate.substring(8) }}</span>
            <span class="day-emoji">{{ log.emoji }}</span>
          </div>
        </div>
      </div>

      <el-empty v-if="stats && stats.totalDays === 0" description="本月还没有打卡记录哦~" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { moodCheckIn, getTodayMood, getMonthlyMoods, getMoodStats } from '@/api/mood'

const moods = [
  { key: 'HAPPY', emoji: '😄', label: '开心' },
  { key: 'GOOD', emoji: '😊', label: '不错' },
  { key: 'NEUTRAL', emoji: '😐', label: '一般' },
  { key: 'SAD', emoji: '😞', label: '低落' },
  { key: 'TERRIBLE', emoji: '😢', label: '很差' }
]

const selectedMood = ref('')
const selectedEmoji = ref('')
const note = ref('')
const submitting = ref(false)
const todayChecked = ref(false)
const todayMood = ref('')

const now = new Date()
const todayStr = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
const selectedMonth = ref(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`)

const stats = ref(null)
const monthlyLogs = ref([])

const avgEmoji = computed(() => {
  if (!stats.value || !stats.value.avgScore) return '—'
  const score = stats.value.avgScore
  if (score >= 4.5) return '😄'
  if (score >= 3.5) return '😊'
  if (score >= 2.5) return '😐'
  if (score >= 1.5) return '😞'
  return '😢'
})

const trendPointsArray = computed(() => {
  if (!stats.value || !stats.value.trend) return []
  return stats.value.trend.map((item, i) => ({
    x: i * 40 + 20,
    y: 120 - (item.score / 5) * 100 - 10
  }))
})

const trendPoints = computed(() => {
  return trendPointsArray.value.map(p => `${p.x},${p.y}`).join(' ')
})

function selectMood(item) {
  selectedMood.value = item.key
  selectedEmoji.value = item.emoji
}

async function handleCheckIn() {
  submitting.value = true
  try {
    await moodCheckIn({
      mood: selectedMood.value,
      emoji: selectedEmoji.value,
      note: note.value
    })
    ElMessage.success('打卡成功！今天也要元气满满哦 ✨')
    todayChecked.value = true
    todayMood.value = selectedMood.value
    fetchStats()
  } catch (e) { /* ignore */ }
  submitting.value = false
}

async function fetchToday() {
  try {
    const res = await getTodayMood()
    if (res.data) {
      todayChecked.value = true
      todayMood.value = res.data.mood
      selectedMood.value = res.data.mood
      selectedEmoji.value = res.data.emoji
      note.value = res.data.note || ''
    }
  } catch { /* ignore */ }
}

async function fetchStats() {
  const [year, month] = selectedMonth.value.split('-').map(Number)
  try {
    const [statsRes, logsRes] = await Promise.all([
      getMoodStats(year, month),
      getMonthlyMoods(year, month)
    ])
    stats.value = statsRes.data || { totalDays: 0, avgScore: 0, trend: [], distribution: {} }
    monthlyLogs.value = logsRes.data || []
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchToday()
  fetchStats()
})
</script>

<style scoped>
.checkin-section {
  max-width: 700px;
  margin: 0 auto 32px;
}

.checkin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.checkin-header h3 {
  margin: 0;
  font-size: 18px;
}

.checkin-date {
  font-size: 14px;
  color: var(--color-text-muted);
}

.mood-selector {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 20px;
}

.mood-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  background: var(--color-bg);
}

.mood-item:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.mood-item.active {
  border-color: var(--color-primary);
  background: rgba(92, 184, 165, 0.08);
  transform: translateY(-4px);
}

.mood-item.checked {
  border-color: var(--color-accent-orange);
  background: rgba(232, 168, 124, 0.1);
}

.mood-emoji {
  font-size: 36px;
  line-height: 1;
}

.mood-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.note-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.checkin-btn {
  align-self: flex-end;
}

/* 月度统计 */
.stats-section {
  max-width: 700px;
  margin: 0 auto;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-header h3 {
  margin: 0;
}

.stats-overview {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  flex: 1;
  text-align: center;
  padding: 20px 12px;
  background: var(--color-bg);
  border-radius: var(--radius-md);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-muted);
}

/* 趋势图 */
.trend-section {
  margin-bottom: 24px;
}

.trend-section h4 {
  margin: 0 0 12px;
  font-size: 15px;
}

.trend-chart {
  display: flex;
  gap: 8px;
}

.trend-y-axis {
  display: flex;
  flex-direction: column-reverse;
  justify-content: space-between;
  padding: 0 4px;
  font-size: 16px;
}

.trend-graph {
  flex: 1;
  position: relative;
  height: 150px;
  overflow-x: auto;
}

.trend-grid {
  position: absolute;
  inset: 0;
}

.grid-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--color-border-light);
}

.trend-svg {
  width: 100%;
  height: 120px;
}

.trend-x-labels {
  display: flex;
  padding-top: 4px;
}

.x-label {
  width: 40px;
  text-align: center;
  font-size: 11px;
  color: var(--color-text-muted);
}

/* 日历 */
.calendar-section h4 {
  margin: 0 0 12px;
  font-size: 15px;
}

.calendar-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.calendar-day {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  width: 48px;
  padding: 8px 4px;
  border-radius: var(--radius-sm);
  background: var(--color-bg);
  cursor: default;
}

.day-num {
  font-size: 12px;
  color: var(--color-text-muted);
}

.day-emoji {
  font-size: 20px;
}

@media (max-width: 768px) {
  .mood-selector { flex-wrap: wrap; }
  .mood-item { padding: 12px 14px; }
  .mood-emoji { font-size: 28px; }
  .stats-overview { flex-direction: column; }
}
</style>
