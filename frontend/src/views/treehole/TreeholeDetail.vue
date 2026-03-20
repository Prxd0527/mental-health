<template>
  <div class="treehole-detail page-wrapper">
    <el-button text @click="$router.back()" class="back-btn">← 返回广场</el-button>

    <!-- 树洞正文 -->
    <div class="post-detail card" v-if="post">
      <div class="post-header">
        <div class="author-info">
          <el-avatar :size="40" class="author-avatar">
            {{ (post.authorName || '匿名').charAt(0) }}
          </el-avatar>
          <div>
            <div class="author-name">{{ post.authorName || '匿名用户' }}</div>
            <div class="post-time">{{ post.createTime }}</div>
          </div>
        </div>
        <div class="post-tags" v-if="post.tags">
          <el-tag v-for="t in post.tags.split(',')" :key="t" size="small" round>{{ t }}</el-tag>
        </div>
      </div>
      <p class="post-content">{{ post.content }}</p>
      <div class="post-images" v-if="post.images">
        <img v-for="(img, i) in post.images.split(',')" :key="i" :src="img" />
      </div>
      <div class="post-stats">
        <span class="stat-item" @click="handleLike">❤️ {{ post.likes || 0 }}</span>
        <span class="stat-item">👁️ {{ post.views || 0 }}</span>
      </div>
    </div>
    <el-skeleton v-else :rows="4" animated />

    <!-- 评论区 -->
    <div class="comments-section" v-if="post">
      <h3 class="section-title">评论区</h3>

      <!-- 发表评论 -->
      <div class="comment-input card" v-if="authStore.isLoggedIn">
        <el-input
          v-model="newComment"
          type="textarea"
          :rows="3"
          placeholder="写下你的温暖回应..."
          maxlength="300"
          show-word-limit
        />
        <div class="comment-input-footer">
          <el-checkbox v-model="isAnonymous">匿名评论</el-checkbox>
          <el-button type="primary" round size="small" :loading="submitting" @click="submitComment">
            发表
          </el-button>
        </div>
      </div>
      <div v-else class="login-hint card">
        <router-link to="/login">登录后</router-link> 可以发表评论
      </div>

      <!-- 评论列表 -->
      <div class="comments-list" v-if="comments.length">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-header">
            <el-avatar :size="30" class="comment-avatar">
              {{ (comment.authorName || '匿名').charAt(0) }}
            </el-avatar>
            <span class="comment-author">{{ comment.authorName || '匿名用户' }}</span>
            <span class="comment-time">{{ comment.createTime }}</span>
          </div>
          <p class="comment-content">{{ comment.content }}</p>
          <div class="comment-actions">
            <span class="action-item" @click="handleCommentLike(comment)">
              ❤️ {{ comment.likes || 0 }}
            </span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无评论，来说说你的想法吧~" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { getPostDetail, likePost } from '@/api/post'
import { getComments, publishComment, likeComment } from '@/api/comment'

const route = useRoute()
const authStore = useAuthStore()

const post = ref(null)
const comments = ref([])
const newComment = ref('')
const isAnonymous = ref(true)
const submitting = ref(false)

async function fetchPost() {
  try {
    const res = await getPostDetail(route.params.id)
    post.value = res.data
  } catch (e) { /* ignore */ }
}

async function fetchComments() {
  try {
    const res = await getComments(route.params.id)
    comments.value = res.data || []
  } catch (e) { /* ignore */ }
}

async function handleLike() {
  try {
    await likePost(post.value.id)
    post.value.likes = (post.value.likes || 0) + 1
  } catch (e) { /* ignore */ }
}

async function handleCommentLike(comment) {
  try {
    await likeComment(comment.id)
    comment.likes = (comment.likes || 0) + 1
  } catch (e) { /* ignore */ }
}

async function submitComment() {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    await publishComment({
      postId: Number(route.params.id),
      content: newComment.value,
      isAnonymous: isAnonymous.value ? 1 : 0
    })
    ElMessage.success('评论发布成功！')
    newComment.value = ''
    fetchComments()
  } catch (e) { /* ignore */ }
  submitting.value = false
}

onMounted(() => {
  fetchPost()
  fetchComments()
})
</script>

<style scoped>
.back-btn {
  margin-bottom: 16px;
  color: var(--color-text-secondary);
}

.post-detail {
  max-width: 800px;
  margin: 0 auto 24px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple));
  color: #fff;
  font-weight: 700;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
}

.post-time {
  font-size: 12px;
  color: var(--color-text-muted);
}

.post-tags {
  display: flex;
  gap: 6px;
}

.post-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--color-text-primary);
  margin-bottom: 16px;
  white-space: pre-wrap;
}

.post-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.post-images img {
  max-width: 200px;
  border-radius: var(--radius-sm);
}

.post-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: var(--color-text-muted);
  padding-top: 12px;
  border-top: 1px solid var(--color-border-light);
}

.stat-item {
  cursor: pointer;
  transition: color var(--transition-fast);
}

.stat-item:hover {
  color: var(--color-primary);
}

/* 评论区 */
.comments-section {
  max-width: 800px;
  margin: 0 auto;
}

.comment-input {
  margin-bottom: 20px;
}

.comment-input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}

.login-hint {
  text-align: center;
  color: var(--color-text-muted);
  padding: 20px;
  margin-bottom: 20px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px 20px;
  border: 1px solid var(--color-border-light);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-avatar {
  background: var(--color-accent-purple);
  color: #fff;
  font-size: 12px;
}

.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-left: auto;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.comment-actions {
  font-size: 13px;
  color: var(--color-text-muted);
}

.action-item {
  cursor: pointer;
}

.action-item:hover {
  color: var(--color-primary);
}
</style>
