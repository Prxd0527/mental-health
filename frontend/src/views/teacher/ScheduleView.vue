<template>
  <div class="schedule-page">
    <h1 class="section-title">排班管理</h1>
    <div class="schedule-form card">
      <el-form :model="form" label-width="100px">
        <el-form-item label="工作日期">
          <el-date-picker v-model="form.workDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="可用时段">
          <el-checkbox-group v-model="selectedSlots">
            <el-checkbox v-for="s in allSlots" :key="s" :value="s">{{ s }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handlePublish" :loading="submitting">发布排班</el-button>
        </el-form-item>
      </el-form>
    </div>
    <h2 class="section-title" style="margin-top:32px">我的排班</h2>
    <el-table :data="scheduleList" stripe>
      <el-table-column prop="workDate" label="日期" width="140" />
      <el-table-column prop="availableSlots" label="可用时段" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { publishSchedule, getMySchedule } from '@/api/schedule'

const allSlots = ['09:00-10:00','10:00-11:00','11:00-12:00','14:00-15:00','15:00-16:00','16:00-17:00']
const selectedSlots = ref([])
const form = reactive({ workDate: '' })
const scheduleList = ref([])
const submitting = ref(false)

async function handlePublish() {
  if (!form.workDate || !selectedSlots.value.length) { ElMessage.warning('请选择日期和时段'); return }
  submitting.value = true
  try {
    await publishSchedule({ workDate: form.workDate, availableSlots: selectedSlots.value.join(',') })
    ElMessage.success('排班发布成功'); fetchSchedule()
  } catch { /* ignore */ }
  submitting.value = false
}

async function fetchSchedule() {
  try { const r = await getMySchedule({ startDate: '2026-01-01', endDate: '2026-12-31' }); scheduleList.value = r.data || [] } catch { /* ignore */ }
}

onMounted(fetchSchedule)
</script>

<style scoped>
.schedule-form { max-width: 600px; padding: 24px; }
</style>
