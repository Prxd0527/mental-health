<template>
  <div class="treehole-square page-wrapper">
    <div class="square-header">
      <div class="header-text">
        <h1 class="section-title">树洞广场</h1>
        <p class="page-subtitle">在这里，你的每一句心声都会被温柔接纳</p>
      </div>
      <el-button type="primary" round size="large" @click="showPublish = true" v-if="authStore.isLoggedIn" class="publish-btn">
        <span class="icon">✨</span> 倾诉心声
      </el-button>
    </div>

    <!-- 排序 + 标签过滤 -->
    <div class="filter-bar glass-panel-light">
      <el-radio-group v-model="sortBy" size="large" @change="fetchPosts">
        <el-radio-button value="latest">最新流露</el-radio-button>
        <el-radio-button value="hot">热门共鸣</el-radio-button>
      </el-radio-group>
      <div class="tag-filters">
        <el-tag
          v-for="tag in commonTags"
          :key="tag"
          :type="selectedTag === tag ? 'primary' : 'info'"
          :effect="selectedTag === tag ? 'dark' : 'plain'"
          class="tag-item"
          @click="toggleTag(tag)"
          round
          size="large"
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>

    <!-- 骨架屏占位 -->
    <div class="posts-list stream-layout" v-if="loading">
      <div v-for="i in 6" :key="i" class="post-card card skeleton-card">
        <el-skeleton animated>
          <template #template>
            <div class="sk-header">
              <el-skeleton-item variant="circle" style="width: 40px; height: 40px; margin-right: 12px;" />
              <div>
                <el-skeleton-item variant="text" style="width: 60px; margin-bottom: 4px;" />
                <el-skeleton-item variant="text" style="width: 100px;" />
              </div>
            </div>
            <el-skeleton-item variant="p" style="width: 100%; height: 80px;" />
            <div class="sk-actions" style="margin-top: 16px; display: flex; gap: 16px;">
              <el-skeleton-item variant="text" style="width: 40px;" />
              <el-skeleton-item variant="text" style="width: 40px;" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <!-- 树洞卡片列表 (单列流式布局) -->
    <transition-group name="fade-slide" tag="div" class="posts-list stream-layout" v-else-if="posts.length">
      <div
        v-for="post in posts"
        :key="post.id"
        class="post-card card"
        @click="$router.push(`/treehole/${post.id}`)"
      >
        <div class="post-header">
          <div class="author-info">
            <el-avatar :size="40" class="author-avatar">
              {{ (post.authorName || '匿').charAt(0) }}
            </el-avatar>
            <div class="author-meta">
              <div class="author-name">{{ post.authorName || '匿名倾诉者' }}</div>
              <div class="post-time">{{ formatTime(post.createTime) }}</div>
            </div>
          </div>
          <div class="post-tags" v-if="post.tags">
            <el-tag
              v-for="t in post.tags.split(',')"
              :key="t"
              size="small"
              round
              type="info"
              effect="light"
            >{{ t }}</el-tag>
          </div>
        </div>
        
        <h3 v-if="post.title" class="post-title">{{ post.title }}</h3>
        <p class="post-content">{{ post.content }}</p>
        
        <!-- 动态美学图片画廊 -->
        <div class="post-gallery" v-if="post.images" :class="'gallery-' + Math.min(post.images.split(',').length, 3)">
          <div v-for="(img, i) in post.images.split(',')" :key="i" class="gallery-item" @click.stop="previewImage(img)">
            <img :src="img" class="gallery-img" loading="lazy" decoding="async" />
          </div>
        </div>
        
        <div class="post-actions">
          <div class="actions-left">
            <span class="action-item like-btn" @click.stop="handleLike(post)" :class="{ liked: post.hasLiked }">
              <i class="emoji">❤️</i> {{ post.likes || 0 }}
            </span>
            <span class="action-item">
              <i class="emoji">💬</i> {{ post.commentCount || 0 }}
            </span>
            <span class="action-item view-count">
              <i class="emoji">📖</i> {{ post.views || 0 }}
            </span>
          </div>
          <div class="actions-right" style="display:flex; align-items:center;">
            <el-button 
              v-if="authStore.isAdmin || authStore.userId === post.userId" 
              type="danger" 
              link 
              @click.stop="handleDeletePost(post)"
              style="margin-right: 12px; font-size: 13px;"
            >
              删除
            </el-button>
            <el-tooltip content="举报违规" placement="top">
              <span class="action-item report-btn" @click.stop="openReport(post.id)">🚩</span>
            </el-tooltip>
          </div>
        </div>
      </div>
    </transition-group>

    <div v-else-if="!loading" class="empty-state">
      <el-empty description="这里还很安静，来做第一个倾诉的人吧~" :image-size="120" />
    </div>

    <!-- 优雅的分页组件 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="fetchPosts"
      />
    </div>

    <!-- 发布对话框 -->
    <el-dialog v-model="showPublish" title="倾听你的心声" width="600px" :close-on-click-modal="false" class="glass-dialog">
      <el-form :model="publishForm" label-width="0">
        <!-- 标题 -->
        <el-form-item>
          <el-input
            v-model="publishForm.title"
            placeholder="给心声起个标题（选填）"
            maxlength="50"
            show-word-limit
            class="publish-title-input"
          />
        </el-form-item>
        <!-- 内容 -->
        <el-form-item>
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="5"
            placeholder="此刻，你想对树洞说些什么..."
            maxlength="500"
            show-word-limit
            class="publish-textarea"
          />
        </el-form-item>
        <!-- 精美图片上传区 -->
        <el-form-item>
          <div class="upload-section">
            <span class="upload-label">📷 插入图片（最多 3 张）</span>
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              list-type="picture-card"
              :limit="3"
              :file-list="uploadFileList"
              :on-success="handleUploadSuccess"
              :on-remove="handleUploadRemove"
              :on-exceed="() => ElMessage.warning('最多只允许上传 3 张照片喔')"
              accept="image/*"
              class="sleek-upload"
            >
              <el-icon><i class="el-icon-plus" style="font-size:24px; color:#A0AEC0;">+</i></el-icon>
            </el-upload>
          </div>
        </el-form-item>
        <!-- 标签选择 -->
        <el-form-item>
          <div class="tag-selector">
            <span class="tag-selector-label">🏷️ 归类一下心声</span>
            <div class="tag-chips">
              <span 
                v-for="t in presetTags" 
                :key="t" 
                class="tag-chip"
                :class="{ active: selectedPublishTags.includes(t) }"
                @click="togglePublishTag(t)"
              >
                {{ t }}
              </span>
              <el-input
                v-model="customTag"
                class="custom-tag-input"
                placeholder="+ 自定义"
                @keyup.enter="addCustomTag"
                @blur="addCustomTag"
              />
            </div>
          </div>
        </el-form-item>
        <el-form-item class="publish-options">
          <el-checkbox v-model="publishForm.isAnonymousChecked" size="large" border>
            隐去真实姓名保护隐私
          </el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublish = false" round>再想想</el-button>
        <el-button type="primary" :loading="publishing" @click="handlePublish" round>轻轻投放</el-button>
      </template>
    </el-dialog>

    <ReportDialog ref="reportDialogRef" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostSquare, publishPost, likePost, deletePost } from '@/api/post'
import ReportDialog from '@/components/ReportDialog.vue'

const authStore = useAuthStore()
const reportDialogRef = ref(null)
const posts = ref([])
const sortBy = ref('latest')
const selectedTag = ref('')
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const loading = ref(true)
const showPublish = ref(false)
const publishing = ref(false)

const commonTags = ['压力', '焦虑', '情感', '学业', '人际关系', '自我成长', '失眠', '其他']
const presetTags = ['压力', '焦虑', '情感', '学业', '人际关系', '自我成长', '失眠']
const selectedPublishTags = ref([])
const customTag = ref('')
const uploadFileList = ref([])
const uploadedImages = ref([])

// 上传配置
const uploadUrl = '/api/common/upload'
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const publishForm = reactive({
  title: '',
  content: '',
  isAnonymousChecked: true
})

function previewImage(url) {
  window.open(url, '_blank')
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diffMs = now - d
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour}小时前`
  if (diffHour < 72) return `${Math.floor(diffHour / 24)}天前`
  return dateStr.substring(5, 16)
}

function toggleTag(tag) {
  selectedTag.value = selectedTag.value === tag ? '' : tag
  pageNum.value = 1
  fetchPosts()
}

function togglePublishTag(tag) {
  const idx = selectedPublishTags.value.indexOf(tag)
  if (idx >= 0) selectedPublishTags.value.splice(idx, 1)
  else selectedPublishTags.value.push(tag)
}

function addCustomTag() {
  const t = customTag.value.trim()
  if (t && !selectedPublishTags.value.includes(t)) {
    selectedPublishTags.value.push(t)
  }
  customTag.value = ''
}

function handleUploadSuccess(res) {
  if (res.code === 200 && res.data) {
    uploadedImages.value.push(res.data)
  }
}

function handleUploadRemove(file) {
  const url = file.response?.data || file.url
  uploadedImages.value = uploadedImages.value.filter(u => u !== url)
}

async function fetchPosts() {
  loading.value = true
  try {
    const res = await getPostSquare({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value,
      tags: selectedTag.value || undefined
    })
    posts.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { /* ignore */ }
  // 加入极短的延迟以使过渡动画更性感
  setTimeout(() => { loading.value = false }, 300)
}

function handleDeletePost(post) {
  ElMessageBox.confirm(
    '确定要删除这条心声吗？删除后不可恢复。',
    '提示',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await deletePost(post.id)
      ElMessage.success('删除成功')
      fetchPosts()
    } catch (e) {
      // ignore
    }
  }).catch(() => {})
}

async function handleLike(post) {
  try {
    await likePost(post.id)
    post.likes = (post.likes || 0) + 1
    post.hasLiked = true // trigger heart animation
  } catch (e) { /* ignore */ }
}

function openReport(postId) {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录后再举报')
    return
  }
  if (reportDialogRef.value) {
    reportDialogRef.value.open('POST', postId)
  }
}

async function handlePublish() {
  if (!publishForm.content.trim()) {
    ElMessage.warning('不要让树洞空空的哦')
    return
  }
  publishing.value = true
  try {
    await publishPost({
      title: publishForm.title || undefined,
      content: publishForm.content,
      tags: selectedPublishTags.value.join(','),
      images: uploadedImages.value.join(','),
      isAnonymous: publishForm.isAnonymousChecked ? 1 : 0
    })
    ElMessage.success('心声已轻轻投放。')
    showPublish.value = false
    publishForm.title = ''
    publishForm.content = ''
    selectedPublishTags.value = []
    uploadedImages.value = []
    uploadFileList.value = []
    fetchPosts()
  } catch (e) { /* ignore */ }
  publishing.value = false
}

onMounted(fetchPosts)
</script>

<style scoped>
.square-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-subtitle {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin: 4px 0 0;
}

.publish-btn {
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(42, 157, 143, 0.3);
  transition: all var(--transition-spring);
}
.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(42, 157, 143, 0.4);
}
.publish-btn .icon { margin-right: 4px; }

/* 筛选栏 - 毛玻璃轻量版，宽度与内容对齐 */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  margin: 0 auto 32px;
  flex-wrap: wrap;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255, 255, 255, 0.4);
  max-width: 900px;
}

.tag-filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tag-item {
  cursor: pointer;
  transition: all var(--transition-fast);
  font-weight: 500;
}

/* 现代论坛单列流式布局 */
.stream-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  max-width: 900px; /* 拓宽至 900px，赋予大屏下更舒展从容的卡片身长 */
  margin: 0 auto;
}

.post-card {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  padding: 24px;
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275), box-shadow 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  will-change: transform;
}

.skeleton-card {
  box-shadow: none;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
  border: 1px dashed rgba(255, 255, 255, 0.5);
}

.post-card:hover {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(255, 255, 255, 0.8);
  box-shadow: 0 12px 32px rgba(42, 157, 143, 0.12); 
  transform: translateY(-3px);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-accent-purple));
  color: #fff;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.author-meta {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.2;
  margin-bottom: 4px;
}

.post-time {
  font-size: 12px;
  color: var(--color-text-muted);
}

.post-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.post-content {
  font-size: 15px;
  line-height: 1.6;
  color: var(--color-text-primary);
  margin-bottom: 16px;
  white-space: pre-wrap;
  word-break: break-word;
  /* ==== 极其严酷的长文截断（最多3行），逼迫高专注用户进入详情页展图留言 ==== */
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 论坛流中的图片显示：强制规范收敛的图片区 */
.post-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 16px;
  max-width: 480px; /* 在宽列里不完全铺开避免笨重 */
}

@media (max-width: 500px) {
  .post-images { grid-template-columns: repeat(2, 1fr); max-width: 100%; }
}

.post-img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: var(--radius-md);
  transition: transform var(--transition-normal);
}
.post-img:hover {
  transform: scale(1.02);
}

.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid var(--color-border-light);
}

.actions-left {
  display: flex;
  gap: 24px;
}

.action-item {
  cursor: pointer;
  font-size: 14px;
  color: var(--color-text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all var(--transition-fast);
}

.action-item .emoji { font-style: normal; font-size: 16px; filter: grayscale(1); transition: all 0.3s; }
.action-item:hover { color: var(--color-primary); }
.action-item:hover .emoji { filter: grayscale(0); transform: scale(1.1); }

.like-btn.liked { color: var(--color-danger); }
.like-btn.liked .emoji { filter: grayscale(0); animation: pop 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); }

@keyframes pop {
  0% { transform: scale(1); }
  50% { transform: scale(1.4); }
  100% { transform: scale(1); }
}

.report-btn { opacity: 0; transition: opacity 0.2s; }
.post-card:hover .report-btn { opacity: 0.5; }
.report-btn:hover { opacity: 1; }

.empty-state {
  padding: 60px 0;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.publish-textarea :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  padding: 16px;
  background: #f8fafc;
  border: 1px solid var(--color-border);
  font-size: 15px;
  transition: all var(--transition-fast);
}
.publish-textarea :deep(.el-textarea__inner:focus) {
  background: #fff;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(42, 157, 143, 0.1);
}
.publish-options {
  margin-top: 8px;
}
/* 标签选择器 */
.tag-selector {
  width: 100%;
}
.tag-selector-label {
  display: block;
  font-weight: 600;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 12px;
}
.tag-chips {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}
.tag-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  padding: 0 16px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid rgba(42, 157, 143, 0.2);
  background: rgba(255, 255, 255, 0.6);
  color: var(--color-text-regular);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  user-select: none;
}
.tag-chip:hover {
  background: rgba(42, 157, 143, 0.05);
  transform: translateY(-2px);
}
.tag-chip.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
  box-shadow: 0 6px 12px rgba(42, 157, 143, 0.2);
}

/* 统一的自定义标签输入框 */
.custom-tag-input {
  width: 120px;
}
.custom-tag-input :deep(.el-input__wrapper) {
  border-radius: 16px;
  height: 32px;
  box-shadow: none;
  border: 1px dashed rgba(42, 157, 143, 0.4);
  background: rgba(255, 255, 255, 0.4);
  padding: 0 16px;
  transition: all 0.3s ease;
}
.custom-tag-input :deep(.el-input__wrapper.is-focus) {
  border-style: solid;
  border-color: var(--color-primary);
  background: #fff;
  box-shadow: 0 4px 12px rgba(42, 157, 143, 0.1);
}
.custom-tag-input :deep(.el-input__inner) {
  font-size: 13px;
  text-align: center;
}

/* 动态美学图片画廊 */
.post-gallery {
  display: grid;
  gap: 8px;
  margin-bottom: 20px;
  border-radius: var(--radius-md);
  overflow: hidden;
  max-width: 500px;
}
.gallery-1 {
  grid-template-columns: 1fr;
}
.gallery-1 .gallery-item {
  aspect-ratio: 16 / 9;
}
.gallery-2 {
  grid-template-columns: 1fr 1fr;
}
.gallery-2 .gallery-item {
  aspect-ratio: 1;
}
.gallery-3 {
  grid-template-columns: repeat(3, 1fr);
}
.gallery-3 .gallery-item {
  aspect-ratio: 1;
}
.gallery-item {
  width: 100%;
  overflow: hidden;
  background: #f0f2f5;
  border-radius: 6px;
}
.gallery-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}
.gallery-img:hover {
  transform: scale(1.05);
}

/* 标题输入 */
.publish-title-input :deep(.el-input__inner) {
  font-size: 16px;
  font-weight: 600;
}

/* 图片上传区美化 */
.upload-section {
  width: 100%;
  margin-bottom: 8px;
}
.upload-label {
  display: block;
  font-weight: 600;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 10px;
}
.sleek-upload :deep(.el-upload--picture-card) {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px dashed rgba(42, 157, 143, 0.4);
  transition: all 0.3s ease;
}
.sleek-upload :deep(.el-upload--picture-card:hover) {
  background: rgba(42, 157, 143, 0.05);
  border-color: var(--color-primary);
}
.sleek-upload :deep(.el-upload-list__item) {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
</style>
