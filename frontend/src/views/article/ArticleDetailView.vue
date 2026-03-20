<template>
  <div class="article-detail-page page-wrapper">
    <div class="article-container" v-if="article">
      <el-button text @click="$router.back()" class="back-btn">
        ← 返回列表
      </el-button>
      <article class="article-content-card card">
        <div class="article-header">
          <span class="article-category" v-if="article.category">{{ article.category }}</span>
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <span>{{ formatDate(article.createTime) }}</span>
            <span>👁️ {{ article.views }} 阅读</span>
          </div>
        </div>
        <div class="article-body" v-html="article.content"></div>
      </article>
    </div>
    <div v-else class="loading-wrapper">
      <el-skeleton :rows="8" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticleDetail } from '@/api/article'

const route = useRoute()
const article = ref(null)

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}

onMounted(async () => {
  try {
    const res = await getArticleDetail(route.params.id)
    article.value = res.data
  } catch (e) { /* 拦截器处理 */ }
})
</script>

<style scoped>
.article-container {
  max-width: 800px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 16px;
  color: var(--color-text-secondary);
}

.article-content-card {
  padding: 40px;
}

.article-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--color-border-light);
}

.article-category {
  display: inline-block;
  padding: 3px 12px;
  background: var(--color-primary-lighter);
  color: var(--color-primary-dark);
  border-radius: 10px;
  font-size: 13px;
  margin-bottom: 12px;
}

.article-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.4;
  margin-bottom: 16px;
}

.article-meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: var(--color-text-muted);
}

.article-body {
  font-size: 15px;
  line-height: 1.8;
  color: var(--color-text-primary);
}

.article-body :deep(p) {
  margin-bottom: 16px;
}

.article-body :deep(h2),
.article-body :deep(h3) {
  margin: 24px 0 12px;
  font-weight: 600;
}

.article-body :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: 16px 0;
}

.loading-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .article-content-card { padding: 24px 16px; }
  .article-title { font-size: 22px; }
}
</style>
