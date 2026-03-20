<template>
  <div class="teacher-list-page page-wrapper">
    <h1 class="section-title">咨询师列表</h1>
    <p class="page-desc">选择一位专业咨询师，预约一对一心理咨询</p>

    <div class="teacher-grid" v-if="teachers.length">
      <div v-for="t in teachers" :key="t.id" class="teacher-card card">
        <el-avatar :size="64" class="teacher-avatar">
          {{ (t.realName || t.username || '师').charAt(0) }}
        </el-avatar>
        <h3>{{ t.realName || t.username }}</h3>
        <p class="teacher-title">{{ t.title || '心理咨询师' }}</p>
        <p class="teacher-bio">{{ t.bio || '专业心理咨询，用心倾听每一个故事' }}</p>
        <el-button type="primary" round size="small" @click="openBooking(t)">预约咨询</el-button>
      </div>
    </div>
    <el-empty v-else description="暂无可预约的咨询师" />

    <!-- 预约对话框 -->
    <el-dialog v-model="showBooking" title="预约咨询" width="480px">
      <el-form :model="bookingForm" label-width="80px">
        <el-form-item label="咨询师">
          <el-input :value="selectedTeacher?.realName || selectedTeacher?.username" disabled />
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker v-model="bookingForm.appointDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="时间段">
          <el-select v-model="bookingForm.timeSlot" placeholder="选择时段" style="width:100%">
            <el-option v-for="slot in availableSlots" :key="slot" :label="slot" :value="slot" />
          </el-select>
        </el-form-item>
        <el-form-item label="咨询诉求">
          <el-input v-model="bookingForm.requirement" type="textarea" :rows="3" placeholder="简要描述你的咨询需求..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBooking = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTeachers } from '@/api/auth'
import { getTeacherSchedule } from '@/api/schedule'
import { submitAppointment } from '@/api/appointment'

const authStore = useAuthStore()
const router = useRouter()
const teachers = ref([])
const showBooking = ref(false)
const selectedTeacher = ref(null)
const submitting = ref(false)
const availableSlots = ref([])

const bookingForm = reactive({ appointDate: '', timeSlot: '', requirement: '' })

async function openBooking(t) {
  if (!authStore.isLoggedIn) { ElMessage.warning('请先登录'); return router.push('/login') }
  selectedTeacher.value = t
  bookingForm.appointDate = ''
  bookingForm.timeSlot = ''
  bookingForm.requirement = ''
  availableSlots.value = []
  showBooking.value = true
}

watch(() => bookingForm.appointDate, async (date) => {
  if (!date || !selectedTeacher.value) return
  try {
    const res = await getTeacherSchedule(selectedTeacher.value.id, { startDate: date, endDate: date })
    const schedules = res.data || []
    if (schedules.length && schedules[0].availableSlots) {
      availableSlots.value = schedules[0].availableSlots.split(',')
    } else {
      availableSlots.value = ['09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00']
    }
  } catch { availableSlots.value = ['09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00'] }
})

async function handleSubmit() {
  if (!bookingForm.appointDate || !bookingForm.timeSlot) { ElMessage.warning('请选择日期和时段'); return }
  submitting.value = true
  try {
    await submitAppointment({ teacherId: selectedTeacher.value.id, ...bookingForm })
    ElMessage.success('预约提交成功！')
    showBooking.value = false
    router.push('/appointments')
  } catch (e) { /* ignore */ }
  submitting.value = false
}

onMounted(async () => {
  try { const res = await getTeachers(); teachers.value = res.data || [] } catch { /* ignore */ }
})
</script>

<style scoped>
.page-desc { color: var(--color-text-secondary); margin-bottom: 28px; }
.teacher-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.teacher-card { text-align: center; padding: 32px 24px; }
.teacher-avatar { background: linear-gradient(135deg, var(--color-accent-orange), var(--color-accent-pink)); color: #fff; font-size: 24px; font-weight: 700; margin-bottom: 14px; }
.teacher-card h3 { font-size: 18px; font-weight: 600; margin-bottom: 4px; }
.teacher-title { font-size: 13px; color: var(--color-primary); margin-bottom: 8px; }
.teacher-bio { font-size: 13px; color: var(--color-text-muted); line-height: 1.5; margin-bottom: 16px; }
@media (max-width: 768px) { .teacher-grid { grid-template-columns: 1fr; } }
</style>
