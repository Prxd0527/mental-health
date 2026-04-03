<template>
  <div class="article-detail-page">
    <div class="top-nav container">
      <el-button text @click="$router.back()" class="back-btn" round>
        <span style="margin-right: 4px;">←</span> 返回列表
      </el-button>
    </div>

    <article class="reader-container" v-if="article">
      <!-- 根据有无封面平滑切换首屏形态 -->
      <header class="hero-section" :class="{ 'no-cover': !article.coverImage }">
        <template v-if="article.coverImage">
          <div class="hero-bg" :style="{ backgroundImage: `url(${article.coverImage})` }">
            <div class="hero-overlay"></div>
          </div>
        </template>

        <div class="hero-content">
          <div class="badge-container" v-if="article.category">
            <span class="category-badge">{{ article.category }}</span>
          </div>
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <span class="meta-item"><i class="icon">📅</i> {{ formatDate(article.createTime) }}</span>
            <span class="meta-divider">•</span>
            <span class="meta-item"><i class="icon">📖</i> {{ article.views || 0 }} 阅读</span>
          </div>
        </div>
      </header>

      <!-- 文章正文区域（与头部协调） -->
      <div class="article-body-wrapper container" :class="{ 'no-cover-body': !article.coverImage }">
        <div class="article-body" v-html="article.content"></div>
        
        <div class="end-mark">
          <div class="dot"></div><div class="dot"></div><div class="dot"></div>
        </div>
      </div>
    </article>

    <div v-else class="loading-wrapper container">
      <!-- 骨架屏首屏 -->
      <el-skeleton :rows="2" animated style="margin: 40px 0 60px;"/>
      <el-skeleton :rows="10" animated />
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
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', padding: '0' })
}

onMounted(async () => {
  try {
    const res = await getArticleDetail(route.params.id)
    article.value = res.data
  } catch (e) { /* 拦截器处理 */ }
})
</script>

<style scoped>
.article-detail-page {
  /* 去除默认 wrapper，完全接管布局宽度，以支撑 Hero 铺满 */
  min-height: calc(100vh - 64px);
  background: var(--color-bg);
  padding-bottom: 80px;
}

.top-nav {
  padding: 16px 20px;
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(248, 249, 250, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0,0,0,0.03);
}

.back-btn {
  font-size: 15px;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}

.back-btn:hover {
  background-color: #fff;
  color: var(--color-primary);
  transform: translateX(-4px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

/* ================= 沉浸式首屏 Hero ================= */
.hero-section {
  position: relative;
  width: 100%;
  min-height: 440px;
  display: flex;
  align-items: flex-end;
  padding: 60px 20px 100px; /* 大幅增加底部内边距，以防止被向上卷的下层卡片遮挡文字 */
  color: #fff;
  margin-bottom: -60px; /* 让下方内容略微重叠 */
}

.hero-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  z-index: 0;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.8) 100%);
}

/* ================= 纯净文字排版专用（无封面） ================= */
.hero-section.no-cover {
  min-height: auto;
  padding: 60px 20px 24px; /* 取消长下衬，恢复正常间距 */
  background: transparent;
  color: var(--color-text-primary);
  margin-bottom: 0;
  align-items: flex-start;
}

.hero-section.no-cover .hero-content {
  text-align: center;
}

.hero-section.no-cover .article-title {
  color: var(--color-text-primary);
  text-shadow: none;
  font-size: 38px;
  line-height: 1.4;
}

.hero-section.no-cover .category-badge {
  background: rgba(42, 157, 143, 0.08);
  color: var(--color-primary-dark);
  border: 1px solid rgba(42, 157, 143, 0.15);
}

.hero-section.no-cover .article-meta {
  color: var(--color-text-secondary);
  justify-content: center;
}

.hero-content {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.category-badge {
  display: inline-block;
  padding: 4px 16px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  color: #fff;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.article-title {
  font-size: 40px;
  font-weight: 800;
  line-height: 1.3;
  margin-bottom: 24px;
  letter-spacing: -0.02em;
  text-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 15px;
  font-weight: 500;
  opacity: 0.9;
}

.meta-item .icon { font-style: normal; margin-right: 4px; filter: grayscale(1); opacity: 0.8; }
.meta-divider { opacity: 0.5; }

/* ================= 文章阅读区 ================= */
.article-body-wrapper {
  max-width: 880px;
  margin: 0 auto;
  background: #fff;
  border-radius: 24px 24px 0 0;
  padding: 60px;
  position: relative;
  box-shadow: 0 -10px 40px rgba(0,0,0,0.06);
  z-index: 20;
}

.article-body-wrapper.no-cover-body {
  margin-top: 0;
  box-shadow: none; /* 移除盖在上方的阴影 */
  background: transparent; /* 完美融合到大背景中 */
  padding-top: 24px; /* 极简的顶边距 */
  border-radius: 0;
}

.article-body {
  font-size: 18px; /* 加大字号 */
  line-height: 1.9;
  color: #333d4b;
  font-weight: 400;
}

/* 博客元素美化 */
.article-body :deep(p) {
  margin-bottom: 24px;
}

.article-body :deep(h2),
.article-body :deep(h3) {
  margin: 48px 0 20px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.4;
}

.article-body :deep(h2) { font-size: 26px; border-bottom: 2px solid var(--color-border-light); padding-bottom: 12px; }
.article-body :deep(h3) { font-size: 22px; }

.article-body :deep(img) {
  max-width: 100%;
  border-radius: 12px;
  margin: 32px 0;
  box-shadow: var(--shadow-md);
  display: block;
}

.article-body :deep(blockquote) {
  margin: 32px 0;
  padding: 20px 24px;
  border-left: 4px solid var(--color-primary);
  background: var(--color-primary-lighter);
  border-radius: 0 12px 12px 0;
  color: var(--color-primary-dark);
  font-style: italic;
  font-size: 19px;
}

.article-body :deep(a) {
  color: var(--color-primary);
  text-decoration: underline;
  text-underline-offset: 4px;
}

.article-body :deep(ul), .article-body :deep(ol) {
  margin-bottom: 24px;
  padding-left: 24px;
}
.article-body :deep(li) { margin-bottom: 8px; }

.end-mark {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 60px;
  opacity: 0.3;
}
.end-mark .dot { width: 6px; height: 6px; background: var(--color-text-primary); border-radius: 50%; }

.loading-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .hero-section { min-height: 380px; padding: 40px 16px 80px; }
  .hero-section.no-cover { padding: 40px 16px 20px; }
  .article-title { font-size: 28px; }
  .hero-section.no-cover .article-title { font-size: 26px; }
  .article-body-wrapper { padding: 40px 20px; border-radius: 16px 16px 0 0; }
  .article-body-wrapper.no-cover-body { padding: 20px; }
  .article-body { font-size: 16px; }
}
</style>
