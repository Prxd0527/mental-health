<template>
  <div class="quiz-list-page page-wrapper">
    <div class="page-header">
      <h1 class="page-title">认识自我</h1>
      <p class="page-subtitle">选择一份专业的心理量表，探索内在潜能与当前状态</p>
    </div>

    <!-- 骨架屏 -->
    <div class="quiz-grid" v-if="loading">
      <div v-for="i in 6" :key="i" class="quiz-card skeleton-card">
        <el-skeleton animated>
          <template #template>
            <div style="display: flex; gap: 20px;">
              <el-skeleton-item variant="rect" style="width: 80px; height: 80px; border-radius: 20px;" />
              <div style="flex: 1;">
                <el-skeleton-item variant="text" style="width: 60%; margin-bottom: 12px; height: 20px;" />
                <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 8px;" />
                <el-skeleton-item variant="text" style="width: 80%;" />
              </div>
            </div>
            <div style="display: flex; justify-content: space-between; margin-top: 24px;">
              <el-skeleton-item variant="text" style="width: 30%; height: 24px;" />
              <el-skeleton-item variant="button" style="width: 80px; height: 32px; border-radius: 16px;" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <!-- 量表卡片方阵 -->
    <transition-group name="fade-slide" tag="div" class="quiz-grid" v-else-if="quizList.length">
      <div v-for="(quiz, index) in quizList" :key="quiz.id" class="quiz-card glass-panel-light">
        <div class="quiz-card-content">
          <div class="quiz-left">
            <div class="quiz-icon-wrapper" :style="{ background: getGradient(index) }">
              <span class="emoji-icon">{{ getEmoji(index) }}</span>
            </div>
          </div>
          <div class="quiz-right">
            <h3 class="quiz-name" :title="quiz.title">{{ quiz.title }}</h3>
            <p class="quiz-desc">{{ quiz.description || '发现心灵的奥秘，通过专业量表了解自己的心理状态。' }}</p>
          </div>
        </div>
        
        <div class="quiz-footer">
          <div class="quiz-meta">
            <span class="meta-tag"><i class="icon">📝</i> {{ quiz.questionCount || '—' }} 题</span>
            <span class="meta-tag"><i class="icon">⏱️</i> 约 {{ Math.ceil((quiz.questionCount || 10) * 0.5) }} 分钟</span>
          </div>
          <el-button type="primary" round class="start-btn" @click="startQuiz(quiz.id)">
            开始测评
          </el-button>
        </div>
      </div>
    </transition-group>

    <div class="empty-state" v-else-if="!loading">
      <el-empty description="暂时没有找到相关量表" :image-size="120" />
    </div>

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
const pageSize = ref(12)
const total = ref(0)
const loading = ref(true)

// 更柔和、具现代治愈感的渐变
const gradients = [
  'linear-gradient(135deg, #FAD7A1, #E96D71)', 
  'linear-gradient(135deg, #A1C4FD, #C2E9FB)',
  'linear-gradient(135deg, #D4FC79, #96E6A1)',
  'linear-gradient(135deg, #FF9A9E, #FECFEF)',
  'linear-gradient(135deg, #667EEA, #764BA2)',
  'linear-gradient(135deg, #84FAB0, #8FD3F4)'
]
// 卡片专属视觉形象
const emojis = ['🌟', '🧠', '🌿', '❤️', '🧩', '🧭']

function getGradient(index) {
  return gradients[index % gradients.length]
}

function getEmoji(index) {
  return emojis[index % emojis.length]
}

function startQuiz(id) {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('探索前，请先登录以保留您的成长轨迹')
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
  setTimeout(() => { loading.value = false }, 300)
}

onMounted(fetchQuizzes)
</script>

<style scoped>
.page-header {
  text-align: center; margin-bottom: 48px;
}
.page-title {
  font-size: 36px; font-weight: 800; color: var(--color-text-primary);
  margin-bottom: 12px; letter-spacing: -0.02em;
}
.page-subtitle {
  font-size: 16px; color: var(--color-text-secondary);
}

.quiz-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 24px;
}

.quiz-card {
  display: flex;
  flex-direction: column;
  padding: 24px;
  border-radius: 24px;
  transition: all var(--transition-spring);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.quiz-card:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  border-color: rgba(255, 255, 255, 0.8);
}

.skeleton-card {
  background: rgba(255, 255, 255, 0.5);
  box-shadow: none;
}

.quiz-card-content {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  flex: 1;
}

.quiz-icon-wrapper {
  width: 72px; height: 72px;
  border-radius: 20px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 16px rgba(0,0,0,0.1) inset;
  flex-shrink: 0;
}
.emoji-icon { font-size: 36px; }

.quiz-right { flex: 1; display: flex; flex-direction: column; }

.quiz-name {
  font-size: 18px; font-weight: 700; color: var(--color-text-primary);
  margin-bottom: 8px; line-height: 1.3;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

.quiz-desc {
  font-size: 14px; color: var(--color-text-secondary); line-height: 1.6;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

.quiz-footer {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--color-border-light);
}

.quiz-meta {
  display: flex; gap: 8px;
}

.meta-tag {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 10px; background: rgba(0,0,0,0.04);
  border-radius: 8px; font-size: 12px; font-weight: 600; color: var(--color-text-secondary);
}

.meta-tag .icon { font-style: normal; filter: grayscale(1); }

.start-btn {
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(42, 157, 143, 0.2);
  transition: all var(--transition-fast);
}
.start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(42, 157, 143, 0.3);
}

.pagination-wrapper { display: flex; justify-content: center; margin-top: 48px; }

@media (max-width: 768px) {
  .quiz-grid { grid-template-columns: 1fr; }
}
</style>
