<template>
  <div class="dashboard-page">
    <h1 class="section-title">管理面板</h1>

    <!-- 顶部统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card card" v-for="s in statCards" :key="s.label">
        <div class="stat-icon" :style="{ background: s.bg }">{{ s.icon }}</div>
        <div class="stat-info">
          <span class="stat-value">{{ s.value }}</span>
          <span class="stat-label">{{ s.label }}</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <!-- 用户角色分布 - 饼图 -->
      <div class="chart-card card">
        <h3 class="chart-title">👥 用户角色分布</h3>
        <div ref="pieChartRef" class="chart-container"></div>
      </div>
      <!-- 近7天注册趋势 - 折线图 -->
      <div class="chart-card card">
        <h3 class="chart-title">📈 近7天注册趋势</h3>
        <div ref="lineChartRef" class="chart-container"></div>
      </div>
    </div>

    <div class="charts-row">
      <!-- 近7天发帖趋势 - 柱状图 -->
      <div class="chart-card card">
        <h3 class="chart-title">📊 近7天树洞发帖</h3>
        <div ref="barChartRef" class="chart-container"></div>
      </div>
      <!-- 最近动态 -->
      <div class="chart-card card">
        <h3 class="chart-title">🕐 最新动态</h3>
        <div class="activity-list">
          <div v-for="(act, idx) in activities" :key="idx" class="activity-item">
            <span class="activity-tag" :class="act.type === '注册' ? 'tag-reg' : 'tag-post'">{{ act.type }}</span>
            <span class="activity-content">{{ act.content }}</span>
            <span class="activity-time">{{ formatTime(act.time) }}</span>
          </div>
          <el-empty v-if="!activities.length" description="暂无动态" :image-size="60" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/admin'

/* ---- 响应式数据 ---- */
const statCards = ref([
  { icon: '👤', label: '用户总数', value: '—', bg: 'linear-gradient(135deg,#5CB8A5,#7ECFBF)' },
  { icon: '🧑‍🎓', label: '学生', value: '—', bg: 'linear-gradient(135deg,#6CB4EE,#A0D2F0)' },
  { icon: '🧑‍⚕️', label: '咨询师', value: '—', bg: 'linear-gradient(135deg,#B8A9C9,#D4C5E2)' },
  { icon: '🌳', label: '树洞总数', value: '—', bg: 'linear-gradient(135deg,#81C784,#A5D6A7)' },
  { icon: '📰', label: '文章总数', value: '—', bg: 'linear-gradient(135deg,#E8A87C,#F0C8A8)' },
  { icon: '📊', label: '量表总数', value: '—', bg: 'linear-gradient(135deg,#7EB8D4,#A8D8EA)' },
  { icon: '💬', label: '评论总数', value: '—', bg: 'linear-gradient(135deg,#F48FB1,#F8BBD0)' },
])
const activities = ref([])

/* ---- ECharts 实例 ---- */
const pieChartRef = ref(null)
const lineChartRef = ref(null)
const barChartRef = ref(null)
let pieChart = null
let lineChart = null
let barChart = null

/* ---- 页面初始化 ---- */
onMounted(async () => {
  try {
    const r = await getDashboardStats()
    if (!r.data) return
    const d = r.data

    // 填充顶部卡片
    statCards.value[0].value = d.totalUsers ?? '—'
    statCards.value[1].value = d.totalStudents ?? '—'
    statCards.value[2].value = d.totalTeachers ?? '—'
    statCards.value[3].value = d.totalPosts ?? '—'
    statCards.value[4].value = d.totalArticles ?? '—'
    statCards.value[5].value = d.totalQuizzes ?? '—'
    statCards.value[6].value = d.totalComments ?? '—'

    // 最新动态
    activities.value = d.recentActivities || []

    // 初始化图表（必须在 DOM 就绪后）
    await nextTick()
    initPieChart(d.roleDistribution || [])
    initLineChart(d.registrationTrend || [])
    initBarChart(d.postTrend || [])
  } catch (e) {
    console.error('仪表板数据加载失败', e)
  }
})

onUnmounted(() => {
  pieChart?.dispose()
  lineChart?.dispose()
  barChart?.dispose()
  window.removeEventListener('resize', handleResize)
})

/* ---- 窗口自适应 ---- */
function handleResize() {
  pieChart?.resize()
  lineChart?.resize()
  barChart?.resize()
}
window.addEventListener('resize', handleResize)

/* ---- 饼图：用户角色分布 ---- */
function initPieChart(data) {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#666' } },
    color: ['#6CB4EE', '#B8A9C9', '#E8A87C'],
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: true, fontSize: 18, fontWeight: 'bold' }
      },
      data
    }]
  })
}

/* ---- 折线图：近7天注册趋势 ---- */
function initLineChart(trend) {
  if (!lineChartRef.value) return
  lineChart = echarts.init(lineChartRef.value)
  const dates = trend.map(t => t.date.slice(5))  // MM-DD
  const counts = trend.map(t => t.count)
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates, axisLabel: { color: '#999' }, axisLine: { lineStyle: { color: '#eee' } } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#999' }, splitLine: { lineStyle: { color: '#f5f5f5' } } },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    series: [{
      type: 'line',
      data: counts,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { width: 3, color: '#5CB8A5' },
      itemStyle: { color: '#5CB8A5' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(92,184,165,0.3)' },
        { offset: 1, color: 'rgba(92,184,165,0.02)' }
      ])}
    }]
  })
}

/* ---- 柱状图：近7天发帖趋势 ---- */
function initBarChart(trend) {
  if (!barChartRef.value) return
  barChart = echarts.init(barChartRef.value)
  const dates = trend.map(t => t.date.slice(5))
  const counts = trend.map(t => t.count)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates, axisLabel: { color: '#999' }, axisLine: { lineStyle: { color: '#eee' } } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#999' }, splitLine: { lineStyle: { color: '#f5f5f5' } } },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    series: [{
      type: 'bar',
      data: counts,
      barWidth: '40%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#7EB8D4' },
          { offset: 1, color: '#A8D8EA' }
        ])
      }
    }]
  })
}

/* ---- 工具函数：格式化时间 ---- */
function formatTime(timeStr) {
  if (!timeStr) return ''
  try {
    const d = new Date(timeStr)
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    const hh = String(d.getHours()).padStart(2, '0')
    const mi = String(d.getMinutes()).padStart(2, '0')
    return `${mm}-${dd} ${hh}:${mi}`
  } catch { return timeStr }
}
</script>

<style scoped>
/* ---- 统计卡片网格 ---- */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.08);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 800;
  color: var(--color-text-primary);
}
.stat-label {
  display: block;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 2px;
}

/* ---- 图表容器 ---- */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}
.chart-card {
  padding: 20px;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--color-text-primary);
}
.chart-container {
  width: 100%;
  height: 280px;
}

/* ---- 最新动态列表 ---- */
.activity-list {
  max-height: 280px;
  overflow-y: auto;
}
.activity-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border-light, #f0f0f0);
  font-size: 13px;
}
.activity-item:last-child { border-bottom: none; }
.activity-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}
.tag-reg { background: #e8f5e9; color: #388e3c; }
.tag-post { background: #e3f2fd; color: #1976d2; }
.activity-content {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--color-text-primary);
}
.activity-time {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--color-text-muted);
}

/* ---- 响应式 ---- */
@media (max-width: 900px) {
  .charts-row { grid-template-columns: 1fr; }
}
@media (max-width: 600px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
