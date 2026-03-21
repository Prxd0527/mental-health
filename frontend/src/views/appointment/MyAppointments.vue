<template>
  <div class="my-appointments page-wrapper">
    <h1 class="section-title">我的预约</h1>
    <div class="appointments-list" v-if="appointments.length">
      <div v-for="apt in appointments" :key="apt.id" class="apt-card card">
        <div class="apt-header">
          <el-tag :type="statusType(apt.status)" round>{{ statusText(apt.status) }}</el-tag>
          <span class="apt-date">{{ apt.appointDate }} {{ apt.timeSlot }}</span>
        </div>
        <div class="apt-body">
          <p><strong>咨询师：</strong>{{ apt.teacherName || '—' }}</p>
          <p v-if="apt.requirement"><strong>诉求：</strong>{{ apt.requirement }}</p>
          <p v-if="apt.feedback"><strong>反馈：</strong>{{ apt.feedback }}</p>
        </div>
        <div class="apt-footer" v-if="apt.status === 'PENDING'">
          <el-button size="small" type="danger" round @click="handleCancel(apt.id)">取消预约</el-button>
        </div>
        <div class="apt-footer" v-if="apt.status === 'APPROVED'">
          <el-button size="small" type="primary" round @click="$router.push('/chat')">去咨询</el-button>
        </div>
        <div class="apt-footer" v-if="apt.status === 'COMPLETED'">
          <el-button v-if="!apt.evaluated" size="small" type="warning" round @click="openFeedback(apt.id)">
            ⭐ 评价咨询
          </el-button>
          <el-tag v-else type="success" size="small" round>已评价</el-tag>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无预约记录" />

    <FeedbackDialog ref="feedbackDialogRef" @submitted="fetchData" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyAppointments, cancelAppointment } from '@/api/appointment'
import { checkEvaluated } from '@/api/feedback'
import FeedbackDialog from '@/components/FeedbackDialog.vue'

const appointments = ref([])
const feedbackDialogRef = ref(null)

const statusMap = { PENDING: '待审核', APPROVED: '已同意', REJECTED: '已拒绝', COMPLETED: '已完成', CANCELLED: '已取消' }
const statusTypeMap = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', COMPLETED: 'info', CANCELLED: 'info' }
function statusText(s) { return statusMap[s] || s }
function statusType(s) { return statusTypeMap[s] || '' }

async function handleCancel(id) {
  await ElMessageBox.confirm('确定取消该预约吗？', '提示')
  try { await cancelAppointment(id); ElMessage.success('已取消'); fetchData() } catch { /* ignore */ }
}

async function fetchData() {
  try {
    const res = await getMyAppointments()
    const list = res.data || []
    // 为已完成的预约检查是否已评价
    for (const apt of list) {
      if (apt.status === 'COMPLETED') {
        try {
          const evalRes = await checkEvaluated(apt.id)
          apt.evaluated = evalRes.data === true
        } catch {
          apt.evaluated = false
        }
      } else {
        apt.evaluated = false
      }
    }
    appointments.value = list
  } catch { /* ignore */ }
}

function openFeedback(appointmentId) {
  if (feedbackDialogRef.value) {
    feedbackDialogRef.value.open(appointmentId)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.appointments-list { display: flex; flex-direction: column; gap: 16px; }
.apt-card { padding: 20px 24px; }
.apt-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.apt-date { font-size: 14px; color: var(--color-text-secondary); font-weight: 500; }
.apt-body p { font-size: 14px; margin-bottom: 6px; color: var(--color-text-primary); }
.apt-footer { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
