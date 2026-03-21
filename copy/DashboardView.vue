<template>
  <div class="dashboard-page">
    <h1 class="section-title">管理面板</h1>
    <div class="stats-grid">
      <div class="stat-card card" v-for="s in stats" :key="s.label">
        <div class="stat-icon" :style="{ background: s.bg }">{{ s.icon }}</div>
        <div class="stat-info">
          <span class="stat-value">{{ s.value }}</span>
          <span class="stat-label">{{ s.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboardStats } from '@/api/admin'

const stats = ref([
  { icon: '👤', label: '用户总数', value: '—', bg: 'linear-gradient(135deg,#5CB8A5,#7ECFBF)' },
  { icon: '🌳', label: '树洞总数', value: '—', bg: 'linear-gradient(135deg,#B8A9C9,#D4C5E2)' },
  { icon: '📊', label: '测评次数', value: '—', bg: 'linear-gradient(135deg,#E8A87C,#F0C8A8)' },
  { icon: '📅', label: '预约总数', value: '—', bg: 'linear-gradient(135deg,#7EB8D4,#A8D8EA)' },
])

onMounted(async () => {
  try {
    const r = await getDashboardStats()
    if (r.data) {
      stats.value[0].value = r.data.userCount ?? '—'
      stats.value[1].value = r.data.postCount ?? '—'
      stats.value[2].value = r.data.quizResultCount ?? '—'
      stats.value[3].value = r.data.appointmentCount ?? '—'
    }
  } catch { /* ignore */ }
})
</script>

<style scoped>
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.stat-card { display: flex; align-items: center; gap: 16px; padding: 24px; }
.stat-icon { width: 56px; height: 56px; border-radius: 16px; display: flex; align-items: center; justify-content: center; font-size: 28px; flex-shrink: 0; }
.stat-value { display: block; font-size: 28px; font-weight: 800; color: var(--color-text-primary); }
.stat-label { display: block; font-size: 13px; color: var(--color-text-muted); margin-top: 2px; }
@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
