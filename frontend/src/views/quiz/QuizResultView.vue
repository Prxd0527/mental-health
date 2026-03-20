<template>
  <div class="quiz-result-page page-wrapper">
    <div class="result-container" v-if="result">
      <div class="result-card card">
        <div class="result-header">
          <div class="score-circle">
            <span class="score-value">{{ result.totalScore }}</span>
            <span class="score-label">总分</span>
          </div>
          <h2 class="result-title">{{ result.quizTitle || '测评结果' }}</h2>
          <p class="result-time">{{ result.createTime }}</p>
        </div>
        <div class="result-body">
          <div class="suggestion-box">
            <h3>💡 评估建议</h3>
            <p>{{ result.suggestion || result.resultDesc || '暂无建议' }}</p>
          </div>
        </div>
        <div class="result-actions">
          <el-button round @click="$router.push('/quiz')">返回量表列表</el-button>
          <el-button type="primary" round @click="$router.push('/profile')">查看历史记录</el-button>
        </div>
      </div>
    </div>
    <el-skeleton v-else :rows="6" animated />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getMyResults } from '@/api/quiz'

const route = useRoute()
const result = ref(null)

onMounted(async () => {
  try {
    const res = await getMyResults()
    const results = res.data || []
    result.value = results.find(r => String(r.id) === String(route.params.id)) || results[0]
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.result-container { max-width: 600px; margin: 0 auto; }

.result-card { padding: 40px; text-align: center; }

.result-header { margin-bottom: 32px; }

.score-circle {
  width: 100px; height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple));
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  margin: 0 auto 20px;
  color: #fff;
}

.score-value { font-size: 32px; font-weight: 800; line-height: 1; }
.score-label { font-size: 12px; opacity: 0.8; margin-top: 4px; }

.result-title { font-size: 22px; font-weight: 700; margin-bottom: 8px; }
.result-time { font-size: 14px; color: var(--color-text-muted); }

.suggestion-box {
  background: var(--color-bg);
  border-radius: var(--radius-md);
  padding: 24px;
  text-align: left;
  margin-bottom: 28px;
}

.suggestion-box h3 { font-size: 16px; margin-bottom: 12px; }
.suggestion-box p { font-size: 15px; line-height: 1.8; color: var(--color-text-secondary); }

.result-actions { display: flex; justify-content: center; gap: 12px; }
</style>
