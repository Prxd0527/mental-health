<template>
  <div class="quiz-do-page">
    <!-- 全屏柔和渐变呼吸背景 -->
    <div class="flowing-background"></div>
    
    <div class="quiz-container" v-if="questions.length">
      <!-- 顶部沉浸式进度控制台 -->
      <div class="progress-section">
        <div class="progress-header">
          <span class="progress-label">探索进度</span>
          <span class="progress-text">{{ currentIndex + 1 }} / {{ questions.length }}</span>
        </div>
        <div class="progress-bar-bg">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        </div>
      </div>

      <!-- 单题核心展示区: 加入动效过渡 -->
      <transition name="fade-slide-y" mode="out-in">
        <div class="question-presenter glass-card" :key="currentIndex">
          <h3 class="question-title">
            <span class="q-num">Q{{ currentIndex + 1 }}.</span>
            {{ currentQuestion.content }}
          </h3>
          
          <div class="options-list">
            <div
              v-for="(opt, i) in parsedOptions"
              :key="i"
              class="option-item"
              :class="{ selected: selectedIndices[currentQuestion.id] === i }"
              @click="selectOption(i, opt.score)"
            >
              <div class="option-check">
                <i class="el-icon-check" v-if="selectedIndices[currentQuestion.id] === i">✓</i>
                <span v-else>{{ String.fromCharCode(65 + i) }}</span>
              </div>
              <span class="option-text">{{ opt.label }}</span>
            </div>
          </div>
        </div>
      </transition>

      <!-- 底部操作与导航栏 -->
      <div class="nav-actions">
        <!-- 为了布局居平，这里放个占位符 -->
        <div class="nav-left">
          <el-button 
            @click="prevQuestion" 
            :disabled="currentIndex === 0" 
            class="nav-btn"
            text
            round
          >
            <span style="margin-right:4px;">←</span> 上一步
          </el-button>
        </div>
        
        <div class="nav-right">
          <el-button
            v-if="currentIndex < questions.length - 1"
            type="primary"
            class="action-btn next-btn"
            round
            size="large"
            :disabled="answers[currentQuestion.id] === undefined"
            @click="nextQuestion"
          >
            下一题 <span style="margin-left:4px;">→</span>
          </el-button>
          <el-button
            v-else
            type="primary"
            class="action-btn submit-btn"
            round
            size="large"
            :loading="submitting"
            :disabled="!allAnswered"
            @click="handleSubmit"
          >
            完成测评 ✨
          </el-button>
        </div>
      </div>
    </div>

    <!-- 首屏加载骨架 -->
    <div class="quiz-container loading-state" v-else>
      <el-skeleton :rows="2" animated style="margin-bottom: 24px;"/>
      <el-skeleton :rows="6" animated />
    </div>
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
const selectedIndices = ref({}) // 新增：专门用于记录UI选中项的索引 (解藕了拥有冗余分数分值的判断)
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

function selectOption(index, score) {
  // UI 高亮锁定索引
  selectedIndices.value[currentQuestion.value.id] = index
  // 数据底座绑定具体分值
  answers.value[currentQuestion.value.id] = score
  
  // UX Optimization: 自动跳转下一题 (如果不是最后一题)
  if (currentIndex.value < questions.value.length - 1) {
    setTimeout(() => {
      // 防止用户反悔快速切题时的冲突，做个安全检查
      if (selectedIndices.value[currentQuestion.value.id] === index) {
        nextQuestion()
      }
    }, 400)
  }
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
    ElMessage.success({ message: '测评已完成！正在生成报告...', center: true })
    setTimeout(() => {
      router.push(`/quiz/result/${res.data?.id || res.data}`)
    }, 500)
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
.quiz-do-page {
  min-height: calc(100vh - 64px);
  position: relative;
  overflow-x: hidden;
  padding: 60px 20px;
}

/* 沉浸式动态背景 */
.flowing-background {
  position: absolute;
  inset: 0;
  z-index: -1;
  background: linear-gradient(120deg, var(--color-bg), var(--color-primary-lighter), #F0E8F5);
  background-size: 200% 200%;
  animation: flow 15s ease infinite alternate;
  opacity: 0.6;
}
@keyframes flow {
  0% { background-position: 0% 50%; }
  100% { background-position: 100% 50%; }
}

.quiz-container {
  max-width: 680px;
  margin: 0 auto;
  position: relative;
  z-index: 10;
}

/* 进度条设计 */
.progress-section { margin-bottom: 40px; }
.progress-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px; font-weight: 600; font-size: 14px; color: var(--color-text-secondary);
}
.progress-bar-bg {
  height: 8px; background: rgba(0,0,0,0.05); border-radius: 4px; overflow: hidden;
  box-shadow: inset 0 1px 2px rgba(0,0,0,0.05);
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary-light), var(--color-primary));
  border-radius: 4px; transition: width 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 答题卡片 */
.glass-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 48px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
}

.question-title {
  font-size: 22px; font-weight: 700; color: var(--color-text-primary);
  line-height: 1.5; margin-bottom: 40px;
  display: flex; gap: 12px; align-items: flex-start;
}
.q-num { color: var(--color-primary); font-size: 24px; line-height: 1.4; opacity: 0.8; }

.options-list { display: flex; flex-direction: column; gap: 16px; }

.option-item {
  display: flex; align-items: center; gap: 16px;
  padding: 16px 24px;
  background: #fff;
  border: 2px solid transparent;
  border-radius: 16px; cursor: pointer;
  transition: all var(--transition-spring);
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
}

.option-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.05);
  background: var(--color-bg-hover);
}

.option-item.selected {
  border-color: var(--color-primary);
  background: rgba(42, 157, 143, 0.05);
  box-shadow: 0 8px 24px rgba(42, 157, 143, 0.15);
  transform: scale(1.02);
}

.option-check {
  width: 32px; height: 32px; border-radius: 50%;
  background: var(--color-bg);
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 15px; color: var(--color-text-secondary);
  transition: all 0.2s;
}

.option-item.selected .option-check {
  background: var(--color-primary); color: #fff; transform: scale(1.1);
}

.option-text { font-size: 16px; font-weight: 500; color: var(--color-text-primary); }

/* 炫酷转场动画 */
.fade-slide-y-enter-active, .fade-slide-y-leave-active { transition: all 0.4s ease; }
.fade-slide-y-enter-from { opacity: 0; transform: translateY(20px); }
.fade-slide-y-leave-to { opacity: 0; transform: translateY(-20px); }

/* 底部操作 */
.nav-actions {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 40px;
}

.action-btn { font-size: 16px; font-weight: 600; padding: 0 32px; box-shadow: 0 8px 16px rgba(42,157,143,0.2); }
.action-btn:hover { transform: translateY(-2px); box-shadow: 0 10px 20px rgba(42,157,143,0.3); }
.submit-btn { background: linear-gradient(135deg, var(--color-primary), #207e73); border: none; }

@media (max-width: 768px) {
  .quiz-do-page { padding: 32px 16px; }
  .glass-card { padding: 32px 20px; }
  .question-title { font-size: 18px; }
  .option-item { padding: 14px 16px; }
}
</style>
