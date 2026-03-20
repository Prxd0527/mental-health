<template>
  <div class="approval-page">
    <h1 class="section-title">预约审批</h1>
    <el-radio-group v-model="statusFilter" @change="fetchAppointments" style="margin-bottom:20px">
      <el-radio-button value="">全部</el-radio-button>
      <el-radio-button value="PENDING">待审核</el-radio-button>
      <el-radio-button value="APPROVED">已同意</el-radio-button>
      <el-radio-button value="REJECTED">已拒绝</el-radio-button>
    </el-radio-group>

    <el-table :data="appointments" stripe>
      <el-table-column prop="studentName" label="学生" width="120" />
      <el-table-column prop="appointDate" label="日期" width="120" />
      <el-table-column prop="timeSlot" label="时段" width="120" />
      <el-table-column prop="requirement" label="诉求" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTypeMap[row.status]" size="small">{{ statusTextMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" v-if="statusFilter !== 'APPROVED' && statusFilter !== 'REJECTED'">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button size="small" type="success" @click="handleProcess(row.id, 'APPROVED')">同意</el-button>
            <el-button size="small" type="danger" @click="handleProcess(row.id, 'REJECTED')">拒绝</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTeacherAppointments, processAppointment } from '@/api/appointment'

const appointments = ref([])
const statusFilter = ref('')
const statusTextMap = { PENDING: '待审核', APPROVED: '已同意', REJECTED: '已拒绝', COMPLETED: '已完成', CANCELLED: '已取消' }
const statusTypeMap = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', COMPLETED: 'info', CANCELLED: 'info' }

async function fetchAppointments() {
  try { const r = await getTeacherAppointments({ status: statusFilter.value || undefined }); appointments.value = r.data || [] } catch { /* ignore */ }
}

async function handleProcess(id, status) {
  try { await processAppointment({ appointmentId: id, status }); ElMessage.success('操作成功'); fetchAppointments() } catch { /* ignore */ }
}

onMounted(fetchAppointments)
</script>
