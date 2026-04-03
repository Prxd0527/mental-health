<template>
  <div class="quiz-result-page page-wrapper">
    <div class="result-container" v-if="result">
      
      <!-- 主结果看板 -->
      <div class="result-card glass-panel">
        <h2 class="result-top-title">{{ result.quizTitle || '测评分析报告' }}</h2>
        <p class="result-time">评估时间：{{ formatTime(result.createTime) }}</p>

        <div class="score-display">
          <div class="glow-ring"></div>
          <div class="score-circle">
            <span class="score-value">{{ result.totalScore }}</span>
            <span class="score-label">综合得分</span>
          </div>
        </div>

        <div class="result-summary">
          完成测评，感谢您与自己的心灵进行了一次深度对话。
        </div>
      </div>

      <!-- 专业分析与建议板块 -->
      <div class="analysis-card card">
        <div class="analysis-header">
          <i class="emoji-icon">💡</i>
          <h3>评估解读与建议</h3>
        </div>
        <div class="analysis-body">
          <p class="suggestion-text">{{ result.suggestion || result.resultDesc || '系统正在综合分析您的数据，暂无更多专业建议...' }}</p>
        </div>
      </div>

      <div class="result-actions">
        <!-- 为了避免生硬，用不同权重的按钮 -->
        <el-button round size="large" class="action-btn outline-btn" @click="$router.push('/quiz')">浏览其他量表</el-button>
        <el-button type="primary" round size="large" class="action-btn primary-btn" @click="$router.push('/profile')">
          我的心理档案
        </el-button>
      </div>
      
    </div>
    <div v-else class="loading-wrapper">
      <el-skeleton :rows="10" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getMyResults } from '@/api/quiz'

const route = useRoute()
const result = ref(null)

function formatTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => {
  try {
    const res = await getMyResults()
    const results = res.data || []
    result.value = results.find(r => String(r.id) === String(route.params.id)) || results[0]
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.quiz-result-page {
  padding-top: 40px;
  padding-bottom: 80px;
}
.result-container { max-width: 680px; margin: 0 auto; }

.glass-panel {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  padding: 48px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0,0,0,0.05);
  margin-bottom: 32px;
  position: relative;
  overflow: hidden;
}

.result-top-title {
  font-size: 26px; font-weight: 800; color: var(--color-text-primary);
  margin-bottom: 8px; letter-spacing: -0.01em;
}
.result-time {
  font-size: 14px; color: var(--color-text-muted); margin-bottom: 40px;
}

/* 分数大圆环设计 */
.score-display {
  position: relative;
  width: 160px; height: 160px;
  margin: 0 auto 40px;
  display: flex; justify-content: center; align-items: center;
}

.glow-ring {
  position: absolute; inset: -10px;
  background: conic-gradient(from 0deg, var(--color-primary-light), var(--color-accent-purple), var(--color-primary));
  border-radius: 50%; opacity: 0.3; filter: blur(20px);
  animation: rotate 6s linear infinite;
}
@keyframes rotate { 100% { transform: rotate(360deg); } }

.score-circle {
  position: relative;
  width: 160px; height: 160px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple));
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: #fff; z-index: 10;
  box-shadow: 0 10px 30px rgba(42, 157, 143, 0.4) inset, 0 8px 20px rgba(0,0,0,0.1);
  border: 4px solid rgba(255,255,255,0.4);
}

.score-value { font-size: 56px; font-weight: 800; line-height: 1; text-shadow: 0 2px 10px rgba(0,0,0,0.2); }
.score-label { font-size: 14px; font-weight: 500; opacity: 0.9; margin-top: 8px; }

.result-summary {
  font-size: 16px; color: var(--color-text-secondary); line-height: 1.6;
}

/* 解读卡片 */
.analysis-card {
  padding: 40px; border-radius: 20px; margin-bottom: 48px;
  background: #fff;
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
}

.analysis-header {
  display: flex; align-items: center; gap: 12px; margin-bottom: 24px;
}
.emoji-icon { font-size: 24px; font-style: normal; }
.analysis-header h3 { font-size: 20px; font-weight: 700; color: var(--color-text-primary); }

.analysis-body {
  background: #F8FAFC; border-radius: 12px; padding: 24px;
  border-left: 4px solid var(--color-primary);
}
.suggestion-text {
  font-size: 16px; line-height: 1.8; color: var(--color-text-primary); font-weight: 400;
  white-space: pre-wrap;
}

.result-actions { display: flex; justify-content: center; gap: 20px; }
.action-btn { font-size: 16px; font-weight: 600; padding: 0 32px; transition: all var(--transition-spring); }
.outline-btn { border: 2px solid var(--color-primary); color: var(--color-primary); background: transparent; }
.outline-btn:hover { background: var(--color-primary-lighter); }
.primary-btn { box-shadow: 0 8px 20px rgba(42, 157, 143, 0.3); }
.primary-btn:hover { transform: translateY(-2px); box-shadow: 0 12px 24px rgba(42, 157, 143, 0.4); }

.loading-wrapper { max-width: 680px; margin: 0 auto; }

@media (max-width: 768px) {
  .quiz-result-page { padding-top: 20px; }
  .glass-panel { padding: 32px 20px; }
  .score-display, .score-circle { width: 140px; height: 140px; }
  .score-value { font-size: 48px; }
  .analysis-card { padding: 32px 20px; }
  .result-actions { flex-direction: column; width: 100%; }
  .action-btn { width: 100%; margin: 0 !important; }
}
</style>
