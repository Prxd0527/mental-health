<template>
  <div class="teacher-list-page page-wrapper">
    <div class="page-header">
      <h1 class="page-title">预约咨询</h1>
      <p class="page-subtitle">选择一位专业的心理咨询师，开启专属的一对一倾听时光</p>
    </div>

    <!-- 骨架屏占位 -->
    <div class="teacher-grid" v-if="loading">
      <div v-for="i in 6" :key="i" class="teacher-card skeleton-card">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="circle" style="width: 80px; height: 80px; margin: 0 auto 16px;" />
            <el-skeleton-item variant="text" style="width: 40%; margin: 0 auto 8px;" />
            <el-skeleton-item variant="text" style="width: 30%; margin: 0 auto 16px;" />
            <el-skeleton-item variant="p" style="width: 90%; height: 40px; margin: 0 auto 24px;" />
            <el-skeleton-item variant="button" style="width: 120px; height: 36px; border-radius: 18px; margin: 0 auto;" />
          </template>
        </el-skeleton>
      </div>
    </div>

    <transition-group name="fade-slide" tag="div" class="teacher-grid" v-else-if="teachers.length">
      <div v-for="t in teachers" :key="t.id" class="teacher-card glass-panel-light">
        <div class="teacher-avatar-wrapper">
          <el-avatar :size="84" class="teacher-avatar">
            {{ (t.realName || t.username || '师').charAt(0) }}
          </el-avatar>
        </div>
        
        <h3 class="teacher-name">{{ t.realName || t.username }}</h3>
        <span class="teacher-tag">{{ t.title || '专业心理咨询师' }}</span>
        
        <p class="teacher-bio">{{ t.bio || '用心倾听每一个故事，陪你走过人生的起伏，寻找内心的平静与力量。' }}</p>
        
        <div class="teacher-footer">
          <el-button type="primary" round class="booking-btn" @click="openBooking(t)">预约咨询</el-button>
        </div>
      </div>
    </transition-group>

    <div class="empty-state" v-else-if="!loading">
      <el-empty description="当前暂无可预约的咨询师，请稍后再来看看" :image-size="120" />
    </div>

    <!-- 沉浸式预约对话框 -->
    <el-dialog v-model="showBooking" :title="`与 ${selectedTeacher?.realName || selectedTeacher?.username} 预约`" width="500px" class="glass-dialog custom-dialog" :close-on-click-modal="false">
      <div class="booking-header">
        <el-avatar :size="48" class="dialog-avatar">{{ (selectedTeacher?.realName || selectedTeacher?.username || '?').charAt(0) }}</el-avatar>
        <div class="dialog-meta">
          <strong>{{ selectedTeacher?.realName || selectedTeacher?.username }}</strong>
          <span>正在等待你的诉说</span>
        </div>
      </div>

      <el-form :model="bookingForm" label-position="top" class="booking-form">
        <el-form-item label="期待碰面的日期">
          <el-date-picker 
            v-model="bookingForm.appointDate" 
            type="date" 
            format="YYYY年MM月DD日" 
            value-format="YYYY-MM-DD" 
            placeholder="选择一个安静的日期" 
            class="full-width custom-picker"
            :disabled-date="(time) => time.getTime() < Date.now() - 8.64e7"
          />
        </el-form-item>
        <el-form-item label="期望的专属时段" v-if="bookingForm.appointDate">
          <el-select v-model="bookingForm.timeSlot" placeholder="选择具体时间段" class="full-width">
            <el-option v-for="slot in availableSlots" :key="slot" :label="slot" :value="slot" />
          </el-select>
        </el-form-item>
        <el-form-item label="这一刻，你想聊些什么？ (选填)">
          <el-input 
            v-model="bookingForm.requirement" 
            type="textarea" 
            :rows="4" 
            placeholder="简要写下你的心事，让咨询师提前了解你..." 
            class="custom-textarea"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBooking = false" round plain>再想想</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit" round class="submit-booking-btn">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
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
const loading = ref(true)

const bookingForm = reactive({ appointDate: '', timeSlot: '', requirement: '' })

async function openBooking(t) {
  if (!authStore.isLoggedIn) { 
    ElMessage.warning('需先登录档案，才能预定专属倾听时间')
    return router.push('/login') 
  }
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
      availableSlots.value = ['09:00-10:00', '10:00-11:00', '14:00-15:00', '16:00-17:00']
    }
  } catch { availableSlots.value = ['09:00-10:00', '10:00-11:00', '14:00-15:00'] }
})

async function handleSubmit() {
  if (!bookingForm.appointDate || !bookingForm.timeSlot) { 
    ElMessage.warning('请告诉我们您期待的日期和时段')
    return 
  }
  submitting.value = true
  try {
    await submitAppointment({ teacherId: selectedTeacher.value.id, ...bookingForm })
    ElMessage.success({ message: '预约提交成功！期待与您的相遇。', center: true })
    showBooking.value = false
    setTimeout(() => { router.push('/appointments') }, 800)
  } catch (e) { /* ignore */ }
  submitting.value = false
}

onMounted(async () => {
  loading.value = true
  try { 
    const res = await getTeachers()
    teachers.value = res.data || [] 
  } catch { /* ignore */ }
  setTimeout(() => { loading.value = false }, 300)
})
</script>

<style scoped>
.page-header { text-align: center; margin-bottom: 48px; }
.page-title { font-size: 34px; font-weight: 800; color: var(--color-text-primary); margin-bottom: 12px; letter-spacing: -0.02em; }
.page-subtitle { font-size: 16px; color: var(--color-text-secondary); }

.teacher-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 32px; }

.teacher-card {
  position: relative;
  border-radius: var(--radius-xl);
  text-align: center;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  padding-top: 40px;
}

.teacher-card:hover { 
  transform: translateY(-8px); 
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.04); 
  border-color: #fff; 
}

.skeleton-card { background: rgba(255, 255, 255, 0.5); padding: 40px 24px; box-shadow: none; }

.teacher-avatar-wrapper { 
  margin-bottom: 20px; 
  display: flex; justify-content: center; 
}

.teacher-avatar { 
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary)); 
  color: #fff; 
  font-size: 32px; 
  font-weight: 700; 
  box-shadow: 0 10px 24px rgba(42, 157, 143, 0.3); 
}

.teacher-name { 
  font-size: 20px; font-weight: 700; color: var(--color-text-primary); 
  margin-bottom: 8px; letter-spacing: 0.5px;
}

.teacher-tag { 
  display: inline-block; padding: 4px 14px; 
  background: var(--color-bg-light); color: var(--color-text-secondary); 
  font-size: 13px; font-weight: 500; border-radius: 20px; 
  margin-bottom: 24px; align-self: center;
}

.teacher-bio { 
  font-size: 14px; color: var(--color-text-muted); line-height: 1.7; 
  padding: 0 24px; margin-bottom: 24px; 
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; 
  overflow: hidden; flex: 1; 
}

.teacher-footer { padding: 0 24px 32px; }

.booking-btn { 
  font-weight: 600; width: 100%; 
  background: rgba(42, 157, 143, 0.08) !important; 
  color: var(--color-primary) !important;
  border: none !important;
  height: 44px;
  font-size: 15px;
  transition: all 0.3s ease; 
  border-radius: 22px;
}

.teacher-card:hover .booking-btn { 
  background: var(--color-primary) !important; 
  color: #fff !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(42, 157, 143, 0.25); 
}

/* 对话框特殊样式 */
.booking-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; padding: 16px; background: rgba(0,0,0,0.02); border-radius: 12px; }
.dialog-avatar { background: var(--color-primary); color: #fff; font-size: 20px; font-weight: 700; }
.dialog-meta { display: flex; flex-direction: column; }
.dialog-meta strong { font-size: 16px; color: var(--color-text-primary); }
.dialog-meta span { font-size: 13px; color: var(--color-text-muted); }

.full-width { width: 100%; }
.custom-textarea :deep(.el-textarea__inner) { background: #f8fafc; border-radius: 8px; padding: 12px; transition: all 0.3s; }
.custom-textarea :deep(.el-textarea__inner:focus) { background: #fff; box-shadow: 0 0 0 2px rgba(42, 157, 143, 0.1); }
.submit-booking-btn { font-weight: 600; padding: 0 24px; }

@media (max-width: 768px) {
  .teacher-grid { grid-template-columns: 1fr; gap: 20px; }
  .page-header { margin-bottom: 32px; }
}
</style>
