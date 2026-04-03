<template>
  <div class="schedule-page">
    <!-- 发布排班 -->
    <h1 class="section-title">排班管理</h1>
    <div class="publish-card card">
      <el-form :model="form" label-width="100px">
        <el-form-item label="工作日期">
          <el-date-picker
            v-model="form.workDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            :disabled-date="disablePast"
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="可用时段">
          <div class="slot-grid">
            <div
              v-for="s in allSlots"
              :key="s"
              class="slot-chip"
              :class="{ active: selectedSlots.includes(s) }"
              @click="toggleSlot(s)"
            >
              {{ s }}
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handlePublish" :loading="submitting" round>发布排班</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 我的排班 -->
    <h2 class="section-title" style="margin-top:40px">我的排班</h2>

    <div v-if="scheduleList.length" class="schedule-timeline">
      <div v-for="item in scheduleList" :key="item.id" class="timeline-item card">
        <div class="timeline-date">
          <span class="date-day">{{ formatDay(item.workDate) }}</span>
          <span class="date-weekday">{{ formatWeekday(item.workDate) }}</span>
          <span class="date-full">{{ item.workDate }}</span>
        </div>
        <div class="timeline-slots">
          <span
            v-for="slot in parseSlots(item.availableSlots)"
            :key="slot"
            class="slot-tag"
          >{{ slot }}</span>
        </div>
        <el-button text type="danger" size="small" class="del-btn" @click="handleDelete(item)">删除</el-button>
      </div>
    </div>
    <div v-else class="empty-state card">
      <el-empty description="暂无排班记录，请在上方发布" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { publishSchedule, getMySchedule } from '@/api/schedule'
import request from '@/utils/request'

const allSlots = ['09:00-10:00','10:00-11:00','11:00-12:00','14:00-15:00','15:00-16:00','16:00-17:00']
const selectedSlots = ref([])
const form = reactive({ workDate: '' })
const scheduleList = ref([])
const submitting = ref(false)

function disablePast(date) {
  return date < new Date(new Date().setHours(0,0,0,0))
}

function toggleSlot(slot) {
  const idx = selectedSlots.value.indexOf(slot)
  if (idx >= 0) selectedSlots.value.splice(idx, 1)
  else selectedSlots.value.push(slot)
}

function parseSlots(str) {
  if (!str) return []
  return str.split(',').map(s => s.trim()).filter(Boolean).sort()
}

function formatDay(dateStr) {
  if (!dateStr) return ''
  return dateStr.substring(8, 10) // 取日
}

function formatWeekday(dateStr) {
  if (!dateStr) return ''
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[new Date(dateStr).getDay()]
}

async function handlePublish() {
  if (!form.workDate || !selectedSlots.value.length) { ElMessage.warning('请选择日期和时段'); return }
  submitting.value = true
  try {
    await publishSchedule({ workDate: form.workDate, availableSlots: selectedSlots.value.join(',') })
    ElMessage.success('排班发布成功')
    selectedSlots.value = []
    fetchSchedule()
  } catch { /* ignore */ }
  submitting.value = false
}

async function handleDelete(item) {
  try {
    await ElMessageBox.confirm('确认删除该日排班？', '提示', { type: 'warning' })
    await request.delete(`/schedule/${item.id}`)
    ElMessage.success('已删除')
    fetchSchedule()
  } catch { /* ignore */ }
}

async function fetchSchedule() {
  try {
    const year = new Date().getFullYear()
    const r = await getMySchedule({ startDate: `${year}-01-01`, days: 365 })
    scheduleList.value = r.data || []
  } catch { /* ignore */ }
}

onMounted(fetchSchedule)
</script>

<style scoped>
.schedule-page {
  max-width: 960px;
  margin: 0 auto;
}

.publish-card {
  max-width: 960px;
  padding: 28px 32px;
}

/* 时段选择芯片网格 */
.slot-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.slot-chip {
  padding: 8px 18px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  background: #f0f5f4;
  color: var(--color-text-secondary);
  border: 1.5px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.slot-chip:hover {
  background: #e6f0ef;
  border-color: var(--color-primary-light);
}

.slot-chip.active {
  background: rgba(42, 157, 143, 0.12);
  color: var(--color-primary-dark);
  border-color: var(--color-primary);
  font-weight: 600;
}

/* 排班时间线 */
.schedule-timeline {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-width: 960px;
}

.timeline-item {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 28px;
  transition: all 0.2s;
}

.timeline-item:hover {
  border-color: rgba(42, 157, 143, 0.3);
  box-shadow: var(--shadow-md);
}

.timeline-date {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 72px;
  flex-shrink: 0;
}

.date-day {
  font-size: 28px;
  font-weight: 800;
  color: var(--color-primary-dark);
  line-height: 1;
}

.date-weekday {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary);
  margin-top: 4px;
}

.date-full {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-top: 4px;
}

.timeline-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.slot-tag {
  display: inline-block;
  padding: 5px 14px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  background: rgba(42, 157, 143, 0.08);
  color: var(--color-primary-dark);
  border: 1px solid rgba(42, 157, 143, 0.15);
  white-space: nowrap;
}

.del-btn {
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.timeline-item:hover .del-btn {
  opacity: 1;
}

.empty-state {
  max-width: 960px;
  padding: 40px;
}

@media (max-width: 640px) {
  .timeline-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .timeline-date {
    flex-direction: row;
    gap: 8px;
    align-items: baseline;
  }
  .del-btn { opacity: 1; }
}
</style>
