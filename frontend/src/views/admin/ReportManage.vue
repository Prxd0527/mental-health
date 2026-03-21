<template>
  <div class="report-manage page-wrapper">
    <div class="header">
      <h2 class="title">举报管理</h2>
    </div>

    <!-- 过滤器 -->
    <div class="filter-section card">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="举报状态">
          <el-select v-model="filters.status" placeholder="全部状态" style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="待处理" value="UNPROCESSED" />
            <el-option label="已处理" value="PROCESSED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标类型">
          <el-select v-model="filters.targetType" placeholder="全部类型" style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="树洞" value="POST" />
            <el-option label="评论" value="COMMENT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 举报列表 -->
    <div class="table-container card">
      <el-table
        :data="reports"
        v-loading="loading"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="targetType" label="举报类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.targetType === 'POST' ? '' : 'success'" size="small">
              {{ row.targetType === 'POST' ? '树洞' : '评论' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="100" align="center" />
        <el-table-column prop="reason" label="举报原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reporterId" label="举报人ID" width="100" align="center" />
        <el-table-column prop="createTime" label="举报时间" width="160" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'UNPROCESSED'"
              type="danger"
              size="small"
              plain
              @click="handleProcess(row.id, 'PROCESS')"
            >
              屏蔽内容
            </el-button>
            <el-button
              v-if="row.status === 'UNPROCESSED'"
              size="small"
              @click="handleProcess(row.id, 'REJECT')"
            >
              驳回
            </el-button>
            <span v-else style="color: #909399; font-size: 13px;">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchReports"
          @current-change="fetchReports"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReports, processReport } from '@/api/report'

const loading = ref(false)
const reports = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const filters = reactive({
  status: '',
  targetType: ''
})

const fetchReports = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: filters.status || undefined,
      targetType: filters.targetType || undefined
    }
    const res = await getReports(params)
    if (res.code === 200) {
      reports.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('Failed to fetch reports:', error)
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  pageNum.value = 1
  fetchReports()
}

const resetFilter = () => {
  filters.status = ''
  filters.targetType = ''
  handleFilter()
}

const getStatusType = (status) => {
  const map = {
    UNPROCESSED: 'warning',
    PROCESSED: 'success',
    REJECTED: 'info'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    UNPROCESSED: '待处理',
    PROCESSED: '已处理',
    REJECTED: '已驳回'
  }
  return map[status] || '未知'
}

const handleProcess = (id, action) => {
  const isProcess = action === 'PROCESS'
  const actionText = isProcess ? '屏蔽该内容' : '驳回该举报'
  const type = isProcess ? 'warning' : 'info'

  ElMessageBox.confirm(
    `确定要${actionText}吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: type
    }
  )
    .then(async () => {
      try {
        await processReport(id, { action })
        ElMessage.success(`${actionText}成功`)
        fetchReports()
      } catch (error) {
        console.error('Process report failed:', error)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchReports()
})
</script>

<style scoped>
.header {
  margin-bottom: 24px;
}

.title {
  margin: 0;
  color: var(--color-text-primary);
}

.filter-section {
  padding: 20px;
  margin-bottom: 24px;
}

.filter-form {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.el-form-item {
  margin-bottom: 0;
}

.table-container {
  padding: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
