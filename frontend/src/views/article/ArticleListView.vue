<template>
  <div class="article-list-page page-wrapper">
    <h1 class="section-title">心理科普</h1>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索文章标题..."
        clearable
        prefix-icon="Search"
        size="large"
        class="search-input"
        @keyup.enter="doSearch"
        @clear="doSearch"
      />
    </div>

    <!-- 文章列表 -->
    <div class="articles-grid" v-if="articles.length">
      <div
        v-for="article in articles"
        :key="article.id"
        class="article-card"
        @click="$router.push(`/article/${article.id}`)"
      >
        <div class="article-cover" v-if="article.coverImage">
          <img :src="article.coverImage" :alt="article.title" />
        </div>
        <div class="article-cover placeholder-cover" v-else>
          <span>📖</span>
        </div>
        <div class="article-body">
          <span class="article-category" v-if="article.category">{{ article.category }}</span>
          <h3 class="article-title">{{ article.title }}</h3>
          <p class="article-summary">{{ article.summary || stripHtml(article.content) }}</p>
          <div class="article-footer">
            <span class="article-date">{{ formatDate(article.createTime) }}</span>
            <span class="article-views">👁️ {{ article.views || 0 }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="暂无科普文章" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="fetchArticles"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticleList } from '@/api/article'

const articles = ref([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(9)
const total = ref(0)
const loading = ref(false)

function stripHtml(html) {
  if (!html) return ''
  return html.replace(/<[^>]+>/g, '').substring(0, 120) + '...'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}

async function fetchArticles() {
  loading.value = true
  try {
    const res = await getArticleList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    })
    articles.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { /* 拦截器处理 */ }
  loading.value = false
}

function doSearch() {
  pageNum.value = 1
  fetchArticles()
}

onMounted(fetchArticles)
</script>

<style scoped>
.search-bar {
  margin-bottom: 24px;
  max-width: 480px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.article-card {
  background: #fff;
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid var(--color-border-light);
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.article-cover img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.placeholder-cover {
  height: 180px;
  background: linear-gradient(135deg, var(--color-primary-lighter), #F0E8F5);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
}

.article-body {
  padding: 16px 20px 20px;
}

.article-category {
  display: inline-block;
  padding: 2px 10px;
  background: var(--color-primary-lighter);
  color: var(--color-primary-dark);
  border-radius: 10px;
  font-size: 12px;
  margin-bottom: 8px;
}

.article-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-summary {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--color-text-muted);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 992px) {
  .articles-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 576px) {
  .articles-grid { grid-template-columns: 1fr; }
}
</style>
