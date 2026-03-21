<template>
  <div class="quiz-do-page page-wrapper">
    <div class="quiz-container" v-if="questions.length">
      <!-- 进度条 -->
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      </div>
      <div class="progress-text">{{ currentIndex + 1 }} / {{ questions.length }}</div>

      <!-- 当前题目 -->
      <div class="question-card card">
        <h3 class="question-title">{{ currentQuestion.content }}</h3>
        <div class="options-list">
          <div
            v-for="(opt, i) in parsedOptions"
            :key="i"
            class="option-item"
            :class="{ selected: answers[currentQuestion.id] === opt.score }"
            @click="selectOption(opt.score)"
          >
            <span class="option-label">{{ String.fromCharCode(65 + i) }}</span>
            <span class="option-text">{{ opt.label }}</span>
          </div>
        </div>
      </div>

      <!-- 导航按钮 -->
      <div class="nav-buttons">
        <el-button @click="prevQuestion" :disabled="currentIndex === 0" round>上一题</el-button>
        <el-button
          v-if="currentIndex < questions.length - 1"
          type="primary"
          round
          :disabled="answers[currentQuestion.id] === undefined"
          @click="nextQuestion"
        >
          下一题
        </el-button>
        <el-button
          v-else
          type="primary"
          round
          :loading="submitting"
          :disabled="!allAnswered"
          @click="handleSubmit"
        >
          提交答卷
        </el-button>
      </div>
    </div>
    <el-skeleton v-else :rows="6" animated />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getQuizQuestions, submitQuiz } from '@/api/quiz'

const route = useRoute()
const router = useRouter()
const quizId = route.params.id

const questions = ref([])
const currentIndex = ref(0)
const answers = ref({})
const submitting = ref(false)

const currentQuestion = computed(() => questions.value[currentIndex.value] || {})
const progressPercent = computed(() =>
  questions.value.length ? ((currentIndex.value + 1) / questions.value.length) * 100 : 0
)
const allAnswered = computed(() =>
  questions.value.every(q => answers.value[q.id] !== undefined)
)

const parsedOptions = computed(() => {
  try {
    const opts = currentQuestion.value.options
    if (!opts) return []
    return typeof opts === 'string' ? JSON.parse(opts) : opts
  } catch { return [] }
})

function selectOption(score) {
  answers.value[currentQuestion.value.id] = score
}

function prevQuestion() {
  if (currentIndex.value > 0) currentIndex.value--
}

function nextQuestion() {
  if (currentIndex.value < questions.value.length - 1) currentIndex.value++
}

async function handleSubmit() {
  submitting.value = true
  try {
    const res = await submitQuiz(quizId, answers.value)
    ElMessage.success('提交成功！')
    router.push(`/quiz/result/${res.data?.id || res.data}`)
  } catch (e) { /* ignore */ }
  submitting.value = false
}

onMounted(async () => {
  try {
    const res = await getQuizQuestions(quizId)
    questions.value = res.data || []
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.quiz-container {
  max-width: 700px;
  margin: 0 auto;
}

.progress-bar {
  height: 6px;
  background: var(--color-border-light);
  border-radius: 3px;
  margin-bottom: 8px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary), var(--color-accent-purple));
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  text-align: right;
  font-size: 13px;
  color: var(--color-text-muted);
  margin-bottom: 20px;
}

.question-card {
  padding: 32px;
  margin-bottom: 24px;
}

.question-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 24px;
  line-height: 1.5;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border: 2px solid var(--color-border-light);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.option-item:hover {
  border-color: var(--color-primary-light);
  background: var(--color-primary-lighter);
}

.option-item.selected {
  border-color: var(--color-primary);
  background: var(--color-primary-lighter);
}

.option-label {
  width: 30px; height: 30px;
  border-radius: 50%;
  background: var(--color-bg);
  display: flex; align-items: center; justify-content: center;
  font-weight: 600; font-size: 14px;
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.option-item.selected .option-label {
  background: var(--color-primary);
  color: #fff;
}

.option-text {
  font-size: 15px;
  color: var(--color-text-primary);
}

.nav-buttons {
  display: flex;
  justify-content: space-between;
}
</style>
