<template>
  <div class="home-page">
    <!-- Hero Banner -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="hero-shape hero-shape-1"></div>
        <div class="hero-shape hero-shape-2"></div>
        <div class="hero-shape hero-shape-3"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">🌿 大学生心理健康平台</div>
        <h1 class="hero-title">心灵树洞</h1>
        <p class="hero-subtitle">
          一个安全、温暖的倾诉空间<br/>
          匿名分享 · 专业测评 · 在线咨询
        </p>
        <div class="hero-actions">
          <button class="explore-btn" @click="scrollToFeatures">
            <span>开启探索之旅</span>
            <i class="scroll-arrow">↓</i>
          </button>
        </div>
      </div>
    </section>

    <!-- 功能入口卡片 -->
    <section class="features-section container" id="features">
      <h2 class="section-title">核心功能</h2>
      <div class="features-grid">
        <div class="feature-card" @click="$router.push('/treehole')">
          <div class="feature-icon" style="background: linear-gradient(135deg, #5CB8A5, #7ECFBF);">🌳</div>
          <h3>匿名树洞</h3>
          <p>分享你的心情故事，获得温暖的回应</p>
        </div>
        <div class="feature-card" @click="$router.push('/articles')">
          <div class="feature-icon" style="background: linear-gradient(135deg, #7EB8D4, #A8D8EA);">📖</div>
          <h3>心理科普</h3>
          <p>专业心理健康知识文章，助你成长</p>
        </div>
        <div class="feature-card" @click="$router.push('/quiz')">
          <div class="feature-icon" style="background: linear-gradient(135deg, #B8A9C9, #D4C5E2);">📊</div>
          <h3>心理测评</h3>
          <p>专业量表测评，了解自己的心理状态</p>
        </div>
        <div class="feature-card" @click="$router.push('/counselors')">
          <div class="feature-icon" style="background: linear-gradient(135deg, #E8A87C, #F0C8A8);">💬</div>
          <h3>预约咨询</h3>
          <p>一对一专业心理咨询，倾听你的声音</p>
        </div>
      </div>
    </section>

    <!-- 热门树洞预览 -->
    <section class="hot-posts-section container">
      <div class="section-header">
        <h2 class="section-title">热门树洞</h2>
        <router-link to="/treehole" class="view-all">查看更多 →</router-link>
      </div>
      <div class="posts-grid" v-if="hotPosts.length">
        <div
          v-for="post in hotPosts"
          :key="post.id"
          class="post-preview-card"
          @click="$router.push(`/treehole/${post.id}`)"
        >
          <div class="post-tag" v-if="post.tags">{{ post.tags.split(',')[0] }}</div>
          <p class="post-content">{{ post.content }}</p>
          <div class="post-meta">
            <span class="meta-item">
              <span class="meta-icon">❤️</span> {{ post.likes || 0 }}
            </span>
            <span class="meta-item">
              <span class="meta-icon">📖</span> {{ post.views || 0 }}
            </span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无树洞内容" />
    </section>

    <!-- 最新科普 -->
    <section class="articles-section container">
      <div class="section-header">
        <h2 class="section-title">最新科普</h2>
        <router-link to="/articles" class="view-all">查看更多 →</router-link>
      </div>
      <div class="articles-list" v-if="latestArticles.length">
        <div
          v-for="article in latestArticles"
          :key="article.id"
          class="article-preview-card"
          @click="$router.push(`/article/${article.id}`)"
        >
          <div class="article-cover" v-if="article.coverImage">
            <img :src="article.coverImage" :alt="article.title" />
          </div>
          <div class="article-info">
            <h4 class="article-title">{{ article.title }}</h4>
            <div class="article-meta">
              <span>{{ article.category || '文章' }}</span>
              <span>📖 {{ article.views || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无科普文章" />
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPostSquare } from '@/api/post'
import { getArticleList } from '@/api/article'

const hotPosts = ref([])
const latestArticles = ref([])

function scrollToFeatures() {
  document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })
}

onMounted(async () => {
  try {
    const postRes = await getPostSquare({ pageNum: 1, pageSize: 4, sortBy: 'hot' })
    hotPosts.value = postRes.data?.records || []
  } catch (e) { /* 首页容错 */ }

  try {
    const articleRes = await getArticleList({ pageNum: 1, pageSize: 3 })
    latestArticles.value = articleRes.data?.records || []
  } catch (e) { /* 首页容错 */ }
})
</script>

<style scoped>
/* Hero */
.hero-section {
  position: relative;
  padding: 80px 20px 60px;
  text-align: center;
  overflow: hidden;
  margin: -24px -20px 40px;
  border-radius: 0 0 var(--radius-xl) var(--radius-xl);
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #E9F7F4 0%, #FAF8F5 40%, #F0E8F5 100%);
  z-index: 0;
}

.hero-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
}

.hero-shape-1 {
  width: 300px; height: 300px;
  background: var(--color-primary);
  top: -80px; right: -60px;
}

.hero-shape-2 {
  width: 200px; height: 200px;
  background: var(--color-accent-purple);
  bottom: -40px; left: -20px;
}

.hero-shape-3 {
  width: 120px; height: 120px;
  background: var(--color-accent-orange);
  top: 40%; left: 15%;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(92, 184, 165, 0.12);
  color: var(--color-primary-dark);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 16px;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 16px;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 16px;
  color: var(--color-text-secondary);
  line-height: 1.8;
  margin-bottom: 28px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
}

/* 新探索按钮样式 */
.explore-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 30px;
  color: var(--color-primary-dark);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 8px 32px rgba(42, 157, 143, 0.1);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.explore-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 12px 40px rgba(42, 157, 143, 0.2);
}

.scroll-arrow {
  font-style: normal;
  font-size: 18px;
  font-weight: 700;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(4px); }
  60% { transform: translateY(2px); }
}

/* Features */
.features-section {
  margin-bottom: 48px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.feature-card {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 28px 20px;
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid var(--color-border-light);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin: 0 auto 14px;
}

.feature-card h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--color-text-primary);
}

.feature-card p {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

/* Section header */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.view-all {
  font-size: 14px;
  color: var(--color-primary);
  font-weight: 500;
}

/* Hot posts */
.hot-posts-section {
  margin-bottom: 48px;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.post-preview-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 20px;
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid var(--color-border-light);
}

.post-preview-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.post-tag {
  display: inline-block;
  padding: 2px 10px;
  background: var(--color-primary-lighter);
  color: var(--color-primary-dark);
  border-radius: 12px;
  font-size: 12px;
  margin-bottom: 10px;
}

.post-content {
  font-size: 14px;
  color: var(--color-text-primary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12px;
}

.post-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--color-text-muted);
}

.meta-icon {
  margin-right: 2px;
}

/* Articles */
.articles-section {
  margin-bottom: 60px;
}

.articles-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.article-preview-card {
  background: #fff;
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid var(--color-border-light);
}

.article-preview-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.article-cover img {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.article-info {
  padding: 16px;
}

.article-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--color-text-muted);
}

/* Responsive */
@media (max-width: 768px) {
  .hero-title { font-size: 32px; }
  .features-grid { grid-template-columns: repeat(2, 1fr); }
  .posts-grid { grid-template-columns: 1fr; }
  .articles-list { grid-template-columns: 1fr; }
}
</style>
