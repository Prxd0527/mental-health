<template>
  <div class="treehole-square page-wrapper">
    <div class="square-header">
      <h1 class="section-title">树洞广场</h1>
      <el-button type="primary" round @click="showPublish = true" v-if="authStore.isLoggedIn">
        ✏️ 发表心情
      </el-button>
    </div>

    <!-- 排序 + 标签过滤 -->
    <div class="filter-bar">
      <el-radio-group v-model="sortBy" @change="fetchPosts">
        <el-radio-button value="latest">最新</el-radio-button>
        <el-radio-button value="hot">最热</el-radio-button>
      </el-radio-group>
      <div class="tag-filters">
        <el-tag
          v-for="tag in commonTags"
          :key="tag"
          :type="selectedTag === tag ? '' : 'info'"
          :effect="selectedTag === tag ? 'dark' : 'plain'"
          class="tag-item"
          @click="toggleTag(tag)"
          round
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>

    <!-- 树洞卡片列表 -->
    <div class="posts-list" v-if="posts.length">
      <div
        v-for="post in posts"
        :key="post.id"
        class="post-card card"
        @click="$router.push(`/treehole/${post.id}`)"
      >
        <div class="post-header">
          <div class="author-info">
            <el-avatar :size="36" class="author-avatar">
              {{ (post.authorName || '匿名').charAt(0) }}
            </el-avatar>
            <div>
              <div class="author-name">{{ post.authorName || '匿名用户' }}</div>
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
              effect="plain"
            >{{ t }}</el-tag>
          </div>
        </div>
        <p class="post-content">{{ post.content }}</p>
        <div class="post-images" v-if="post.images">
          <img v-for="(img, i) in post.images.split(',')" :key="i" :src="img" class="post-img" />
        </div>
        <div class="post-actions">
          <span class="action-item" @click.stop="handleLike(post)">
            ❤️ {{ post.likes || 0 }}
          </span>
          <span class="action-item">💬 {{ post.commentCount || 0 }}</span>
          <span class="action-item">👁️ {{ post.views || 0 }}</span>
        </div>
      </div>
    </div>
    <el-empty v-else-if="!loading" description="还没有人发布树洞哦~" />

    <!-- 分页 -->
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
    <el-dialog v-model="showPublish" title="发表心情" width="520px" :close-on-click-modal="false">
      <el-form :model="publishForm" label-width="0">
        <el-form-item>
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="5"
            placeholder="在这里写下你的心情..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item>
          <el-input v-model="publishForm.tags" placeholder="标签（用英文逗号分隔，如：压力,焦虑）" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="publishForm.isAnonymousChecked">匿名发布</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublish = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { getPostSquare, publishPost, likePost } from '@/api/post'

const authStore = useAuthStore()
const posts = ref([])
const sortBy = ref('latest')
const selectedTag = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const showPublish = ref(false)
const publishing = ref(false)

const commonTags = ['压力', '焦虑', '情感', '学业', '人际关系', '自我成长', '失眠', '其他']

const publishForm = reactive({
  content: '',
  tags: '',
  isAnonymousChecked: true
})

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
  return dateStr.substring(0, 10)
}

function toggleTag(tag) {
  selectedTag.value = selectedTag.value === tag ? '' : tag
  pageNum.value = 1
  fetchPosts()
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
  loading.value = false
}

async function handleLike(post) {
  try {
    await likePost(post.id)
    post.likes = (post.likes || 0) + 1
  } catch (e) { /* ignore */ }
}

async function handlePublish() {
  if (!publishForm.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  publishing.value = true
  try {
    await publishPost({
      content: publishForm.content,
      tags: publishForm.tags,
      isAnonymous: publishForm.isAnonymousChecked ? 1 : 0
    })
    ElMessage.success('发布成功！')
    showPublish.value = false
    publishForm.content = ''
    publishForm.tags = ''
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
  margin-bottom: 20px;
}

.square-header .section-title {
  margin-bottom: 0;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.tag-filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-item {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  cursor: pointer;
  transition: all var(--transition-normal);
}

.post-card:hover {
  box-shadow: var(--shadow-md);
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
  gap: 10px;
}

.author-avatar {
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple));
  color: #fff;
  font-weight: 700;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.post-time {
  font-size: 12px;
  color: var(--color-text-muted);
}

.post-tags {
  display: flex;
  gap: 4px;
}

.post-content {
  font-size: 15px;
  line-height: 1.7;
  color: var(--color-text-primary);
  margin-bottom: 12px;
  white-space: pre-wrap;
}

.post-images {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.post-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.post-actions {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: var(--color-text-muted);
}

.action-item {
  cursor: pointer;
  transition: color var(--transition-fast);
}

.action-item:hover {
  color: var(--color-primary);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .filter-bar { flex-direction: column; align-items: flex-start; }
}
</style>
