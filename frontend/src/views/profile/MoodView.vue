<template>
  <div class="mood-page page-wrapper">
    <div class="page-header">
      <h1 class="page-title">心情晴雨表 ✨</h1>
      <p class="page-subtitle">感知你的情绪，接纳所有的自己。每一个平凡的日子都值得标记。</p>
    </div>

    <!-- 今日打卡区 -->
    <div class="checkin-section card glass-panel">
      <div class="checkin-header">
        <h3 class="checkin-title">{{ todayChecked ? '🎉 这一刻，也已被时光铭记！' : '这一刻，你的心情是怎样的色彩？' }}</h3>
        <span class="checkin-date">{{ todayStr }}</span>
      </div>

      <div class="mood-selector">
        <div
          v-for="(item, idx) in moods"
          :key="item.key"
          class="mood-item"
          :class="{ active: selectedMood === item.key, checked: todayChecked && todayMood === item.key }"
          @click="selectMood(item)"
          :style="{ 'animation-delay': `${idx * 0.1}s` }"
        >
          <div class="mood-emoji-wrap">
            <span class="mood-emoji">{{ item.emoji }}</span>
            <div class="emoji-glow" :style="selectedMood === item.key ? `background-color: ${item.color}` : ''"></div>
          </div>
          <span class="mood-label" :style="selectedMood === item.key ? `color: ${item.color}; font-weight: 700;` : ''">{{ item.label }}</span>
        </div>
      </div>

      <transition name="fade-slide">
        <div class="note-section" v-if="selectedMood">
          <el-input
            v-model="note"
            type="textarea"
            :rows="3"
            placeholder="为这份心情写下注脚吧… (可选)"
            maxlength="300"
            show-word-limit
            class="custom-textarea"
          />
          <el-button
            type="primary"
            round
            size="large"
            class="checkin-btn shadow-md"
            :loading="submitting"
            @click="handleCheckIn"
            :style="`background-color: ${currentChoiceColor}; border-color: ${currentChoiceColor}`"
          >
            {{ todayChecked ? '修改今日记忆' : '将心情封存 🏷️' }}
          </el-button>
        </div>
      </transition>
    </div>

    <!-- 档案下半部：网格数据 -->
    <div class="stats-grid">
      <!-- 左侧趋势与报告 -->
      <div class="stats-left">
        <div class="stats-section card glass-panel-light flex-col h-full">
          <div class="stats-header">
            <h3 class="card-title">情绪光谱报告</h3>
            <el-date-picker
              v-model="selectedMonth"
              type="month"
              placeholder="时光列车"
              format="YYYY年MM月"
              value-format="YYYY-MM"
              :clearable="false"
              @change="fetchStats"
              class="custom-picker"
              style="width: 140px;"
            />
          </div>

          <div class="stats-overview" v-if="stats">
            <div class="stat-card">
              <div class="stat-value text-gradient-primary">{{ stats.totalDays }} <span class="text-sm">天</span></div>
              <div class="stat-label">留存印迹</div>
            </div>
            <div class="stat-card">
              <div class="stat-value emoji-large">{{ avgEmoji }}</div>
              <div class="stat-label">色彩均值</div>
            </div>
            <div class="stat-card">
              <div class="stat-value text-gradient-orange">{{ stats.avgScore }} <span class="text-sm">Idx</span></div>
              <div class="stat-label">波动频段</div>
            </div>
          </div>

          <!-- 情绪趋势折线 -->
          <div class="trend-section flex-1" v-if="stats && stats.trend && stats.trend.length > 0">
            <h4 class="sub-title">情感周期波浪</h4>
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
                <!-- 丝滑的阴影与折线 -->
                <svg class="trend-svg" :viewBox="`0 0 ${Math.max(stats.trend.length * 50, 400)} 120`" preserveAspectRatio="none">
                  <!-- SVG Filter for Glow -->
                  <defs>
                    <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                      <feGaussianBlur stdDeviation="3" result="blur" />
                      <feComposite in="SourceGraphic" in2="blur" operator="over" />
                    </filter>
                  </defs>
                  
                  <!-- Line Curve -->
                  <path
                    :d="roundedTrendPath"
                    fill="none"
                    stroke="var(--color-primary)"
                    stroke-width="3"
                    filter="url(#glow)"
                  />
                  <!-- Points -->
                  <circle
                    v-for="(p, i) in trendPointsArray"
                    :key="i"
                    :cx="p.x"
                    :cy="p.y"
                    r="5"
                    fill="#fff"
                    stroke="var(--color-primary)"
                    stroke-width="3"
                    class="trend-point"
                  />
                </svg>
                <div class="trend-x-labels">
                  <span v-for="item in stats.trend" :key="item.date" class="x-label" :style="{ left: `${(item.idx+0.5) * 50}px` }">
                    {{ item.date.substring(8) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-if="stats && stats.totalDays === 0" description="选中的月份里时光平淡，还没有画下痕迹" :image-size="100" />
        </div>
      </div>

      <!-- 右侧心情日历 -->
      <div class="stats-right">
        <div class="calendar-section card glass-panel-light h-full">
          <h3 class="card-title mb-4">留影日历图</h3>
          <p class="calendar-desc" v-if="monthlyLogs.length">每一格都承载了那时那刻的情愫，鼠标悬停可回顾随笔。</p>
          <div class="calendar-container" v-if="monthlyLogs.length > 0">
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
          <el-empty v-else description="暂无打卡网格" :image-size="80" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { moodCheckIn, getTodayMood, getMonthlyMoods, getMoodStats } from '@/api/mood'

const moods = [
  { key: 'HAPPY', emoji: '😄', label: '耀眼喜悦', color: '#f59e0b' },
  { key: 'GOOD', emoji: '😊', label: '温和顺意', color: '#10b981' },
  { key: 'NEUTRAL', emoji: '😐', label: '如水般静', color: '#94a3b8' },
  { key: 'SAD', emoji: '😞', label: '略带低气压', color: '#6366f1' },
  { key: 'TERRIBLE', emoji: '😢', label: '需要抱抱', color: '#ec4899' }
]

const selectedMood = ref('')
const selectedEmoji = ref('')
const note = ref('')
const submitting = ref(false)
const todayChecked = ref(false)
const todayMood = ref('')

const currentChoiceColor = computed(() => {
  const f = moods.find(m => m.key === selectedMood.value)
  return f ? f.color : 'var(--color-primary)'
})

const now = new Date()
const todayStr = `${now.getFullYear()}年${String(now.getMonth() + 1).padStart(2, '0')}月${String(now.getDate()).padStart(2, '0')}日`
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

// 计算趋势点 (加入索引以便X偏移算法统一)
const trendPointsArray = computed(() => {
  if (!stats.value || !stats.value.trend) return []
  return stats.value.trend.map((item, i) => {
    item.idx = i
    return {
      x: i * 50 + 25,
      y: 120 - (item.score / 5) * 100 - 10
    }
  })
})

// 绘制平滑曲线的算法 (Catmull-Rom 或 Quadratic Bezier)
const roundedTrendPath = computed(() => {
  const points = trendPointsArray.value
  if (!points || points.length === 0) return ''
  if (points.length === 1) return `M ${points[0].x} ${points[0].y} L ${points[0].x+1} ${points[0].y}`
  
  let path = `M ${points[0].x} ${points[0].y}`
  // 简易的两相邻点中点取控制点来平滑连通
  for (let i = 0; i < points.length - 1; i++) {
    const curr = points[i]
    const next = points[i+1]
    const midX = (curr.x + next.x) / 2
    path += ` C ${midX} ${curr.y}, ${midX} ${next.y}, ${next.x} ${next.y}`
  }
  return path
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
    ElMessage.success({ message: '回忆已成功珍藏。', center: true })
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
.page-header { text-align: center; margin-bottom: 40px; }
.page-title { font-size: 32px; font-weight: 800; color: var(--color-text-primary); margin-bottom: 12px; letter-spacing: -0.02em; }
.page-subtitle { font-size: 15px; color: var(--color-text-secondary); max-width: 600px; margin: 0 auto; line-height: 1.6; }

.mb-4 { margin-bottom: 16px; }
.h-full { height: 100%; }
.flex-col { display: flex; flex-direction: column; }
.flex-1 { flex: 1; min-height: 0; }
.text-sm { font-size: 14px; font-weight: 600; opacity: 0.6; }

.checkin-section { max-width: 800px; margin: 0 auto 32px; padding: 40px; border-radius: 24px; position: relative; overflow: hidden; border: 1px solid rgba(255,255,255,0.8); }

.checkin-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 32px; }
.checkin-title { font-size: 22px; font-weight: 700; color: var(--color-text-primary); margin: 0; }
.checkin-date { font-size: 15px; font-weight: 600; color: var(--color-primary); background: rgba(42, 157, 143, 0.1); padding: 4px 12px; border-radius: 16px; }

.mood-selector { display: flex; justify-content: space-between; gap: 16px; margin-bottom: 32px; }

.mood-item { 
  flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px;
  padding: 24px 12px; border-radius: 20px; cursor: pointer; 
  background: rgba(255, 255, 255, 0.5); border: 2px solid transparent; 
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: slideInUp 0.6s backwards;
}
@keyframes slideInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.mood-item:hover { transform: translateY(-8px); background: #fff; box-shadow: 0 16px 32px rgba(0,0,0,0.06); }
.mood-item.active { border-color: rgba(255,255,255,0.4); background: #fff; transform: translateY(-8px) scale(1.02); box-shadow: 0 16px 40px rgba(0,0,0,0.1); }
.mood-item.checked::after { content: '✓'; position: absolute; top: -10px; right: -10px; width: 24px; height: 24px; background: #10b981; color:#fff; border-radius: 50%; font-size: 14px; display: flex; align-items: center; justify-content: center; font-weight: bold; border: 3px solid #fff; }

.mood-emoji-wrap { position: relative; width: 64px; height: 64px; display: flex; align-items: center; justify-content: center; }
.mood-emoji { font-size: 48px; line-height: 1; z-index: 10; position: relative; transition: transform 0.3s; }
.mood-item:hover .mood-emoji { transform: scale(1.15); }
.emoji-glow { position: absolute; inset: 10px; border-radius: 50%; filter: blur(16px); opacity: 0; transition: opacity 0.4s; z-index: 0; }
.mood-item.active .emoji-glow { opacity: 0.4; }
.mood-label { font-size: 14px; color: var(--color-text-secondary); font-weight: 500; transition: color 0.3s; }

.note-section { display: flex; flex-direction: column; gap: 20px; background: rgba(0,0,0,0.015); padding: 24px; border-radius: 20px; border: 1px dashed rgba(0,0,0,0.06); }
.custom-textarea :deep(.el-textarea__inner) { background: #fff; border: 1px solid rgba(0,0,0,0.05); border-radius: 12px; padding: 16px; font-size: 15px; resize: none; transition: all 0.3s; }
.custom-textarea :deep(.el-textarea__inner:focus) { box-shadow: 0 0 0 3px rgba(42, 157, 143, 0.1); border-color: var(--color-primary-light); }
.checkin-btn { align-self: flex-end; padding: 0 32px; font-weight: 700; font-size: 16px; letter-spacing: 0.05em; transition: all 0.4s; }
.checkin-btn:hover { transform: translateY(-2px); filter: brightness(1.1); }

/* 下半部网格 */
.stats-grid { display: grid; grid-template-columns: 3fr 2fr; gap: 24px; max-width: 1000px; margin: 0 auto; align-items: stretch; }

.card-title { font-size: 18px; font-weight: 700; margin: 0; color: var(--color-text-primary); }
.sub-title { font-size: 15px; font-weight: 600; color: var(--color-text-secondary); margin-bottom: 20px; }

/* 趋势报告卡片 */
.stats-section { padding: 32px; border-radius: 24px; }
.stats-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.custom-picker :deep(.el-input__wrapper) { background: rgba(255,255,255,0.6); border-radius: 20px; box-shadow: none; border: 1px solid rgba(0,0,0,0.05); font-weight: 600; }

.stats-overview { display: flex; gap: 16px; margin-bottom: 36px; }
.stat-card { flex: 1; text-align: center; padding: 24px 16px; background: #fff; border-radius: 20px; border: 1px solid rgba(0,0,0,0.02); box-shadow: 0 4px 16px rgba(0,0,0,0.03); transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-4px); }
.stat-value { font-size: 32px; font-weight: 800; font-family: 'Inter', sans-serif; margin-bottom: 8px; line-height: 1; }
.text-gradient-primary { background: linear-gradient(135deg, var(--color-primary), #10b981); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.text-gradient-orange { background: linear-gradient(135deg, var(--color-accent-orange), #f59e0b); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.emoji-large { font-size: 36px; -webkit-text-fill-color: initial; }
.stat-label { font-size: 13px; color: var(--color-text-muted); font-weight: 600; }

/* SVG Graph */
.trend-chart { display: flex; gap: 12px; background: rgba(255,255,255,0.5); border-radius: 16px; padding: 20px 20px 20px 10px; }
.trend-y-axis { display: flex; flex-direction: column-reverse; justify-content: space-between; font-size: 14px; opacity: 0.7; }
.trend-graph { flex: 1; position: relative; height: 160px; overflow-x: auto; overflow-y: hidden; }
.trend-graph::-webkit-scrollbar { height: 6px; }
.trend-graph::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); border-radius: 3px; }

.trend-grid { position: absolute; inset: 0 0 40px 0; z-index: 0; }
.grid-line { position: absolute; left: 0; right: 0; height: 1px; background: linear-gradient(90deg, transparent, rgba(0,0,0,0.04) 10%, rgba(0,0,0,0.04) 90%, transparent); }
.trend-svg { position: absolute; left: 0; top: 0; width: 100%; height: 120px; z-index: 10; overflow: visible; }
.trend-point { transition: stroke-width 0.2s, r 0.2s; cursor: pointer; }
.trend-point:hover { stroke-width: 5; r: 6; }

.trend-x-labels { position: absolute; bottom: 0; left: 0; width: 100%; height: 30px; }
.x-label { position: absolute; bottom: 0; transform: translateX(-50%); font-size: 11px; font-weight: 600; color: var(--color-text-muted); }

/* 日历卡片 */
.calendar-section { padding: 32px; border-radius: 24px; display: flex; flex-direction: column; }
.calendar-desc { font-size: 13px; color: var(--color-text-secondary); line-height: 1.5; margin-bottom: 24px; }
.calendar-container { flex: 1; overflow-y: auto; padding-right: 4px; }
.calendar-grid { display: flex; flex-wrap: wrap; gap: 12px; }
.calendar-day { 
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px;
  width: 56px; height: 64px; border-radius: 16px; 
  background: #fff; cursor: pointer; transition: all 0.2s;
  border: 1px solid rgba(0,0,0,0.03); box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.calendar-day:hover { transform: scale(1.1); box-shadow: 0 8px 16px rgba(0,0,0,0.08); z-index: 10; border-color: var(--color-primary-light); }
.day-num { font-size: 13px; font-weight: 700; color: var(--color-text-muted); font-family: 'Inter', sans-serif; }
.day-emoji { font-size: 24px; }

@media (max-width: 900px) {
  .stats-grid { grid-template-columns: 1fr; }
  .mood-selector { flex-wrap: wrap; gap: 12px; }
  .mood-item { flex: 0 0 calc(33.333% - 12px); }
}
@media (max-width: 500px) {
  .mood-item { flex: 0 0 calc(50% - 12px); }
  .stats-overview { flex-direction: column; }
  .trend-graph { height: 140px; }
  .trend-svg { height: 100px; }
}
</style>
