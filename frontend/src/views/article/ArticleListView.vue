<template>
  <div class="article-list-page page-wrapper">
    <div class="page-header">
      <h1 class="page-title">探索心灵</h1>
      <p class="page-subtitle">治愈从了解开始，阅读专业的心理健康科普</p>
    </div>

    <!-- 沉浸式搜索栏 -->
    <div class="search-section">
      <el-input
        v-model="keyword"
        placeholder="搜索感兴趣的话题 (例如：焦虑、抑郁)..."
        clearable
        prefix-icon="Search"
        size="large"
        class="search-input glass-input"
        @keyup.enter="doSearch"
        @clear="doSearch"
      />
    </div>

    <!-- 骨架屏占位 -->
    <div class="articles-grid" v-if="loading">
      <div v-for="i in 6" :key="i" class="article-card skeleton-card">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="image" style="width: 100%; height: 200px;" />
            <div style="padding: 24px;">
              <el-skeleton-item variant="text" style="width: 30%; margin-bottom: 12px;" />
              <el-skeleton-item variant="text" style="width: 90%; margin-bottom: 8px; height: 20px" />
              <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 24px;" />
              <div style="display: flex; justify-content: space-between;">
                <el-skeleton-item variant="text" style="width: 30%;" />
                <el-skeleton-item variant="text" style="width: 20%;" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <!-- 现代化展示列表 -->
    <transition-group name="fade-slide" tag="div" class="articles-grid" v-else-if="articles.length">
      <div
        v-for="article in articles"
        :key="article.id"
        class="article-card"
        @click="$router.push(`/article/${article.id}`)"
      >
        <!-- 封面区 (仅当有封面时渲染) -->
        <div class="article-cover-wrapper" v-if="article.coverImage">
          <div class="article-cover">
            <img :src="article.coverImage" :alt="article.title" loading="lazy" />
          </div>
          <!-- 悬浮类目标签 -->
          <span class="article-badge" v-if="article.category">{{ article.category }}</span>
        </div>
        
        <!-- 纯文本与混合排版主体 -->
        <div class="article-body" :class="{'no-image': !article.coverImage}">
          <!-- 无图状态下，标签前置显示 -->
          <div class="category-tag-inline" v-if="!article.coverImage && article.category">
            <span class="inline-badge">{{ article.category }}</span>
          </div>
          
          <h3 class="article-title" :title="article.title">{{ article.title }}</h3>
          <p class="article-summary">{{ article.summary || stripHtml(article.content) }}</p>
          
          <!-- 作者/日期 底栏 -->
          <div class="article-footer">
            <div class="meta-left">
              <span class="article-date">{{ formatDate(article.createTime) }}</span>
            </div>
            <div class="meta-right">
              <span class="article-views"><i class="emoji">📖</i> {{ article.views || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </transition-group>

    <div class="empty-state" v-else-if="!loading">
      <el-empty description="暂时没有相关的文章哦" :image-size="120" />
    </div>

    <!-- 优雅的分页组件 -->
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
const loading = ref(true)

function stripHtml(html) {
  if (!html) return ''
  return html.replace(/<[^>]+>/g, '').substring(0, 100) + '...'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' })
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
  setTimeout(() => { loading.value = false }, 300)
}

function doSearch() {
  pageNum.value = 1
  fetchArticles()
}

onMounted(fetchArticles)
</script>

<style scoped>
.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 36px;
  font-weight: 800;
  color: var(--color-text-primary);
  letter-spacing: -0.02em;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 16px;
  color: var(--color-text-secondary);
}

.search-section {
  max-width: 560px;
  margin: 0 auto 48px;
}

.glass-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  padding: 8px 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: all var(--transition-fast);
}

.glass-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 6px 20px rgba(42, 157, 143, 0.15), 0 0 0 1px var(--color-primary) inset;
  background: #fff;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 32px;
}

.article-card {
  background: #fff;
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-spring);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
}

.skeleton-card {
  box-shadow: none;
  background: rgba(255, 255, 255, 0.4);
}

.article-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
}

.article-cover-wrapper {
  position: relative;
  width: 100%;
  height: 220px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.article-card:hover .article-cover img {
  transform: scale(1.05);
}

/* 无图文章排版强化 */
.article-body.no-image {
  padding: 32px 32px 24px;
}

.category-tag-inline {
  margin-bottom: 16px;
}

.inline-badge {
  background: rgba(42, 157, 143, 0.08);
  color: var(--color-primary-dark);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  display: inline-block;
  border: 1px solid rgba(42, 157, 143, 0.15);
}

.article-body.no-image .article-title {
  font-size: 22px;
  margin-bottom: 12px;
}

.article-body.no-image .article-summary {
  -webkit-line-clamp: 5; /* 纯文本模式允许展示更多摘要 */
  line-clamp: 5;
  font-size: 15px;
  line-height: 1.7;
}

.article-badge {
  position: absolute;
  top: 16px; left: 16px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  color: var(--color-primary-dark);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}

.article-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.article-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--transition-fast);
}

.article-card:hover .article-title {
  color: var(--color-primary-dark);
}

.article-summary {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 24px;
  flex: 1;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--color-text-muted);
  border-top: 1px solid var(--color-border-light);
  padding-top: 16px;
}

.meta-right .emoji { font-style: normal; filter: grayscale(1); }

.empty-state {
  padding: 60px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 48px;
}

@media (max-width: 768px) {
  .articles-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
  }
}
</style>
