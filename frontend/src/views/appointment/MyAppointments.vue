<template>
  <div class="my-appointments page-wrapper">
    <div class="page-header">
      <h1 class="page-title">预约时空</h1>
      <p class="page-subtitle">在这里，记录每一次与内心对话的足迹</p>
    </div>

    <!-- 骨架屏占位 -->
    <div class="appointments-list" v-if="loading">
      <div v-for="i in 3" :key="i" class="apt-card skeleton-card">
        <el-skeleton animated>
          <template #template>
            <div style="display: flex; justify-content: space-between; margin-bottom: 24px;">
              <el-skeleton-item variant="rect" style="width: 60px; height: 24px; border-radius: 12px;" />
              <el-skeleton-item variant="text" style="width: 120px;" />
            </div>
            <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 24px;">
              <el-skeleton-item variant="circle" style="width: 48px; height: 48px;" />
              <div style="flex: 1;">
                <el-skeleton-item variant="text" style="width: 80px; margin-bottom: 8px;" />
                <el-skeleton-item variant="text" style="width: 100%;" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <transition-group name="fade-slide" tag="div" class="appointments-list" v-else-if="appointments.length">
      <div v-for="apt in appointments" :key="apt.id" class="apt-card glass-panel-light">
        <div class="apt-header">
          <div class="status-badge" :class="statusClass(apt.status)">
            <span class="dot"></span> {{ statusText(apt.status) }}
          </div>
          <span class="apt-date"><i class="icon">📅</i> {{ apt.appointDate }} &nbsp; 🕒 {{ apt.timeSlot }}</span>
        </div>
        
        <div class="apt-body">
          <div class="teacher-info">
            <el-avatar :size="48" class="t-avatar shadow-sm">{{ (apt.teacherName || '师').charAt(0) }}</el-avatar>
            <div class="t-meta">
              <span class="t-name">{{ apt.teacherName || '—' }}</span>
              <span class="t-label">专属心理咨询师</span>
            </div>
          </div>

          <div class="apt-details">
            <div class="detail-row" v-if="apt.requirement">
              <span class="label">咨询诉求：</span>
              <span class="value">{{ apt.requirement }}</span>
            </div>
            <div class="detail-row feedback-row" v-if="apt.feedback">
              <span class="label">评估反馈：</span>
              <span class="value">{{ apt.feedback }}</span>
            </div>
          </div>
        </div>
        
        <div class="apt-footer">
          <el-button v-if="apt.status === 'PENDING'" size="small" type="danger" plain round @click="handleCancel(apt.id)">
            取消预约
          </el-button>
          
          <el-button v-if="apt.status === 'APPROVED'" size="large" type="primary" round class="action-btn" @click="$router.push('/chat')">
            进入咨询室 <span style="margin-left:4px;">✨</span>
          </el-button>
          
          <div v-if="apt.status === 'COMPLETED'">
            <el-button v-if="!apt.evaluated" size="small" type="warning" plain round @click="openFeedback(apt.id)">
              ⭐ 留下您的评价
            </el-button>
            <span v-else class="evaluated-badge">✓ 已评价</span>
          </div>
        </div>
      </div>
    </transition-group>

    <div class="empty-state" v-else-if="!loading">
      <el-empty description="时空里静悄悄的，暂无您的预约足迹" :image-size="120" />
    </div>

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
const loading = ref(true)

const statusMap = { PENDING: '待审核', APPROVED: '已同意', REJECTED: '遗憾拒绝', COMPLETED: '圆满完成', CANCELLED: '已取消' }
function statusText(s) { return statusMap[s] || s }

// 配合不同的色彩类名
function statusClass(s) {
  if (s === 'PENDING') return 'status-pending'
  if (s === 'APPROVED') return 'status-approved'
  if (s === 'REJECTED') return 'status-rejected'
  if (s === 'COMPLETED') return 'status-completed'
  return 'status-cancelled'
}

async function handleCancel(id) {
  await ElMessageBox.confirm('确定取消该次心理咨询预约吗？', '提示')
  try { 
    await cancelAppointment(id)
    ElMessage.success('预约已取消。期待下次再见。')
    fetchData() 
  } catch { /* ignore */ }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyAppointments()
    const list = res.data || []
    for (const apt of list) {
      if (apt.status === 'COMPLETED') {
        try {
          const evalRes = await checkEvaluated(apt.id)
          apt.evaluated = evalRes.data === true
        } catch { apt.evaluated = false }
      } else { apt.evaluated = false }
    }
    appointments.value = list
  } catch { /* ignore */ }
  setTimeout(() => { loading.value = false }, 300)
}

function openFeedback(appointmentId) {
  if (feedbackDialogRef.value) {
    feedbackDialogRef.value.open(appointmentId)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-header { text-align: center; margin-bottom: 40px; }
.page-title { font-size: 32px; font-weight: 800; color: var(--color-text-primary); margin-bottom: 12px; }
.page-subtitle { font-size: 15px; color: var(--color-text-secondary); }

.appointments-list { 
  display: flex; flex-direction: column; gap: 24px; max-width: 800px; margin: 0 auto;
}

.apt-card {
  padding: 32px; border-radius: 20px;
  background: rgba(255, 255, 255, 0.85);
  transition: all var(--transition-spring);
  border: 1px solid rgba(255, 255, 255, 0.5);
  display: flex; flex-direction: column; gap: 24px;
}
.apt-card:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: 0 16px 32px rgba(0,0,0,0.06);
  background: #fff;
}

.skeleton-card { box-shadow: none; background: rgba(255, 255, 255, 0.4); padding: 32px; }

/* 头部状态条 */
.apt-header { 
  display: flex; align-items: center; justify-content: space-between;
  padding-bottom: 16px; border-bottom: 1px solid var(--color-border-light);
}

.status-badge { 
  display: inline-flex; align-items: center; gap: 6px; 
  padding: 4px 12px; border-radius: 20px; font-size: 13px; font-weight: 600;
}
.status-badge .dot { width: 6px; height: 6px; border-radius: 50%; }

.status-pending { background: #fffbeb; color: #b45309; border: 1px solid #fde68a; }
.status-pending .dot { background: #f59e0b; }

.status-approved { background: #ecfdf5; color: #047857; border: 1px solid #a7f3d0; }
.status-approved .dot { background: #10b981; animation: pulse 2s infinite; }

.status-rejected { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; }
.status-rejected .dot { background: #ef4444; }

.status-completed { background: #f8fafc; color: #334155; border: 1px solid #e2e8f0; }
.status-completed .dot { background: #64748b; }

.status-cancelled { background: #f1f5f9; color: #64748b; border: 1px solid #cbd5e1; opacity: 0.8; }
.status-cancelled .dot { background: #94a3b8; }

.apt-date { font-size: 14px; font-weight: 500; color: var(--color-text-secondary); display: flex; align-items: center; gap: 4px; }
.apt-date .icon { font-style: normal; opacity: 0.6; }

/* 主体信息区 */
.apt-body { display: flex; flex-direction: column; gap: 20px; }

.teacher-info { display: flex; align-items: center; gap: 16px; }
.t-avatar { background: linear-gradient(135deg, var(--color-primary-light), var(--color-accent-purple)); color: #fff; font-size: 18px; font-weight: 700; }
.t-meta { display: flex; flex-direction: column; gap: 4px; }
.t-name { font-size: 18px; font-weight: 700; color: var(--color-text-primary); }
.t-label { font-size: 13px; color: var(--color-primary); font-weight: 500; }

.apt-details { display: flex; flex-direction: column; gap: 12px; background: rgba(0,0,0,0.02); padding: 16px 20px; border-radius: 12px; }
.detail-row { display: flex; font-size: 15px; line-height: 1.6; }
.detail-row .label { color: var(--color-text-muted); width: 80px; flex-shrink: 0; }
.detail-row .value { color: var(--color-text-primary); flex: 1; word-break: break-word; }
.feedback-row .value { color: var(--color-primary-dark); font-weight: 500; }

/* 底部操作区 */
.apt-footer { display: flex; justify-content: flex-end; align-items: center; padding-top: 8px; }

.action-btn { font-weight: 600; box-shadow: 0 4px 12px rgba(42, 157, 143, 0.2); transition: all var(--transition-fast); }
.action-btn:hover { box-shadow: 0 6px 16px rgba(42, 157, 143, 0.3); transform: translateY(-2px); }

.evaluated-badge { font-size: 14px; font-weight: 500; color: #10b981; display: inline-flex; align-items: center; gap: 4px; padding: 6px 12px; background: #ecfdf5; border-radius: 20px; }
</style>
