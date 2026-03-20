<template>
  <div class="quiz-list-page page-wrapper">
    <h1 class="section-title">心理测评</h1>
    <p class="page-desc">通过专业心理量表，了解自己的心理健康状态</p>

    <div class="quiz-grid" v-if="quizList.length">
      <div v-for="quiz in quizList" :key="quiz.id" class="quiz-card card">
        <div class="quiz-icon" :style="{ background: getGradient(quiz.id) }">📊</div>
        <h3 class="quiz-name">{{ quiz.title }}</h3>
        <p class="quiz-desc">{{ quiz.description }}</p>
        <div class="quiz-meta">
          <span>{{ quiz.questionCount || '—' }} 题</span>
          <span>约 {{ quiz.estimatedMinutes || 5 }} 分钟</span>
        </div>
        <el-button type="primary" round @click="startQuiz(quiz.id)">开始测评</el-button>
      </div>
    </div>
    <el-empty v-else-if="!loading" description="暂无可用测评量表" />

    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="fetchQuizzes"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { getQuizList } from '@/api/quiz'

const router = useRouter()
const authStore = useAuthStore()
const quizList = ref([])
const pageNum = ref(1)
const pageSize = ref(9)
const total = ref(0)
const loading = ref(false)

const gradients = [
  'linear-gradient(135deg, #5CB8A5, #7ECFBF)',
  'linear-gradient(135deg, #B8A9C9, #D4C5E2)',
  'linear-gradient(135deg, #E8A87C, #F0C8A8)',
  'linear-gradient(135deg, #7EB8D4, #A8D8EA)'
]

function getGradient(id) {
  return gradients[(id - 1) % gradients.length]
}

function startQuiz(id) {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return router.push('/login')
  }
  router.push(`/quiz/${id}/do`)
}

async function fetchQuizzes() {
  loading.value = true
  try {
    const res = await getQuizList({ pageNum: pageNum.value, pageSize: pageSize.value })
    quizList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { /* ignore */ }
  loading.value = false
}

onMounted(fetchQuizzes)
</script>

<style scoped>
.page-desc {
  color: var(--color-text-secondary);
  margin-bottom: 28px;
  font-size: 15px;
}

.quiz-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.quiz-card {
  text-align: center;
  padding: 32px 24px;
}

.quiz-icon {
  width: 64px; height: 64px;
  border-radius: 20px;
  display: flex; align-items: center; justify-content: center;
  font-size: 32px;
  margin: 0 auto 16px;
}

.quiz-name {
  font-size: 18px; font-weight: 600; margin-bottom: 8px;
  color: var(--color-text-primary);
}

.quiz-desc {
  font-size: 13px; color: var(--color-text-secondary);
  line-height: 1.6; margin-bottom: 16px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

.quiz-meta {
  display: flex; justify-content: center; gap: 16px;
  font-size: 12px; color: var(--color-text-muted);
  margin-bottom: 20px;
}

.pagination-wrapper { display: flex; justify-content: center; margin-top: 32px; }

@media (max-width: 768px) {
  .quiz-grid { grid-template-columns: 1fr; }
}
</style>
