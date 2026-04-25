<template>
  <div class="treehole-detail page-wrapper">
    <div class="top-nav">
      <el-button text @click="$router.back()" class="back-btn" round>
        <span style="margin-right: 4px;">←</span> 漫步回广场
      </el-button>
    </div>

    <!-- 树洞正文主体 -->
    <div class="post-detail card glass-card" v-if="post">
      <div class="post-header">
        <div class="author-info">
          <el-avatar :size="48" class="author-avatar shadow-sm">
            {{ (post.authorName || '匿').charAt(0) }}
          </el-avatar>
          <div class="author-meta">
            <div class="author-name">{{ post.authorName || '匿名倾诉者' }}</div>
            <div class="post-time">{{ formatTime(post.createTime) }}</div>
          </div>
        </div>
        <div class="post-tags" v-if="post.tags">
          <el-tag v-for="t in post.tags.split(',')" :key="t" size="default" round effect="light">{{ t }}</el-tag>
        </div>
      </div>
      
      <div class="content-body">
        <p class="post-content">{{ post.content }}</p>
        
        <!-- 图片画廊 -->
        <div class="post-images gallery" v-if="post.images">
          <img v-for="(img, i) in post.images.split(',')" :key="i" :src="img" class="gallery-img" />
        </div>
      </div>
      
      <div class="post-stats">
        <div style="display:flex; align-items:center; gap: 24px;">
          <span class="stat-item" @click="handleLike" :class="{ liked: post.hasLiked }">
            <i class="emoji">❤️</i> {{ post.likes || 0 }} 暖心
          </span>
          <span class="stat-item">
            <i class="emoji">📖</i> {{ post.views || 0 }} 擦肩而过
          </span>
        </div>
        <div style="display:flex; align-items:center; gap: 20px;">
          <el-button 
            v-if="authStore.isAdmin || authStore.userId === post.userId" 
            type="danger" 
            link 
            @click.stop="handleDeletePost"
            style="font-size: 13px; margin: 0;"
          >
            删除树洞
          </el-button>
          <span class="stat-item report-link" @click="openReport('POST', post.id)">
            🚩 觉得不适？
          </span>
        </div>
      </div>
    </div>
    
    <div v-else class="post-detail card skeleton-card">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 评论交流区 -->
    <div class="comments-section" v-if="post">
      <div class="section-divider">
        <span class="divider-text">共鸣与回声 ({{ comments.length }})</span>
      </div>

      <!-- 发表评论区 (悬浮层) -->
      <div class="comment-input card reply-card" v-if="authStore.isLoggedIn">
        <el-input
          v-model="newComment"
          type="textarea"
          :rows="3"
          placeholder="给 Ta 留下一丝温暖的回声..."
          maxlength="300"
          show-word-limit
          class="reply-textarea"
        />
        <div class="comment-input-footer">
          <el-checkbox v-model="isAnonymous">匿名留言</el-checkbox>
          <el-button type="primary" round :loading="submitting" @click="submitComment" class="send-btn">
            传递温暖
          </el-button>
        </div>
      </div>
      <div v-else class="login-hint card">
        💡 <router-link to="/login" class="link">登录通行证</router-link> 之后才能留下片语哦
      </div>

      <!-- 评论列表 -->
      <div class="comments-list" v-if="comments.length">
        <transition-group name="list" tag="div">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <el-avatar :size="36" class="comment-avatar">
                {{ (comment.authorName || '匿').charAt(0) }}
              </el-avatar>
              <div class="comment-meta">
                <span class="comment-author">{{ comment.authorName || '匿名倾听者' }}</span>
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              </div>
            </div>
            
            <p class="comment-content">{{ comment.content }}</p>
            
            <div class="comment-actions">
              <div style="display:flex; align-items:center;">
                <span class="action-item" @click="handleCommentLike(comment)" :class="{ liked: comment.hasLiked }">
                  <i class="emoji">❤️</i> {{ comment.likes || 0 }}
                </span>
              </div>
              <div style="display:flex; align-items:center;">
                <span 
                  class="action-item delete-tag" 
                  v-if="authStore.isAdmin || authStore.userId === comment.userId || authStore.userId === post.userId"
                  @click="handleDeleteComment(comment)"
                  style="color: var(--color-danger); margin-right: 12px;"
                >
                  删除
                </span>
                <span class="action-item report-link" @click="openReport('COMMENT', comment.id)">
                  🚩 举报
                </span>
              </div>
            </div>
          </div>
        </transition-group>
      </div>
      
      <div v-else class="empty-comments">
        <el-empty description="还没有人留下回声，这片空间等您点亮 ~" :image-size="100" />
      </div>
    </div>

    <ReportDialog ref="reportDialogRef" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostDetail, likePost, deletePost } from '@/api/post'
import { getComments, publishComment, likeComment, deleteComment } from '@/api/comment'
import ReportDialog from '@/components/ReportDialog.vue'

const route = useRoute()
const router = useRouter()
const reportDialogRef = ref(null)
const authStore = useAuthStore()

const post = ref(null)
const comments = ref([])
const newComment = ref('')
const isAnonymous = ref(true)
const submitting = ref(false)

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

async function fetchPost() {
  try {
    const res = await getPostDetail(route.params.id)
    post.value = res.data
  } catch (e) { /* ignore */ }
}

async function fetchComments() {
  try {
    const res = await getComments(route.params.id)
    // 后端返回的是 Page 对象，实际数据数组在 records 中
    comments.value = res.data?.records || res.data || []
  } catch (e) { /* ignore */ }
}

async function handleLike() {
  try {
    await likePost(post.value.id)
    post.value.likes = (post.value.likes || 0) + 1
    post.value.hasLiked = true
  } catch (e) { /* ignore */ }
}

async function handleCommentLike(comment) {
  try {
    await likeComment(comment.id)
    comment.likes = (comment.likes || 0) + 1
    comment.hasLiked = true
  } catch (e) { /* ignore */ }
}

function openReport(type, id) {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录后再举报')
    return
  }
  if (reportDialogRef.value) {
    reportDialogRef.value.open(type, id)
  }
}

async function submitComment() {
  if (!newComment.value.trim()) {
    ElMessage.warning('评论似乎是空的')
    return
  }
  submitting.value = true
  try {
    await publishComment({
      postId: Number(route.params.id),
      content: newComment.value,
      isAnonymous: isAnonymous.value ? 1 : 0
    })
    ElMessage.success('心声已轻轻放下。')
    newComment.value = ''
    fetchComments()
  } catch (e) { /* ignore */ }
  submitting.value = false
}

function handleDeletePost() {
  ElMessageBox.confirm(
    '确定要删除这个树洞吗？由于留言会一并消失，删除后不可恢复哦。',
    '提示',
    { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await deletePost(post.value.id)
      ElMessage.success('树洞已清空')
      router.back()
    } catch (e) { /* ignore */ }
  }).catch(() => {})
}

function handleDeleteComment(comment) {
  ElMessageBox.confirm(
    '确定擦除这条留言的回声吗？',
    '提示',
    { confirmButtonText: '擦除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await deleteComment(comment.id)
      ElMessage.success('回声已擦除')
      fetchComments()
    } catch (e) { /* ignore */ }
  }).catch(() => {})
}

onMounted(() => {
  fetchPost()
  fetchComments()
})
</script>

<style scoped>
.top-nav {
  margin-bottom: 24px;
}
.back-btn {
  font-size: 15px;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}
.back-btn:hover {
  background-color: var(--color-bg-hover);
  color: var(--color-primary);
  transform: translateX(-4px);
}

.post-detail {
  max-width: 840px;
  margin: 0 auto 32px;
  padding: 32px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--color-border-light);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.author-avatar {
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent-purple));
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

.author-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.post-time {
  font-size: 13px;
  color: var(--color-text-muted);
}

.post-content {
  font-size: 17px;
  line-height: 1.8;
  color: #2D3748;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin-bottom: 24px;
}

.gallery {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 24px;
}

.gallery-img {
  width: 100%;
  aspect-ratio: 16/10;
  object-fit: cover;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: transform 0.3s ease;
}
.gallery-img:hover { transform: scale(1.02); }

.post-stats {
  display: flex;
  justify-content: flex-end;
  gap: 24px;
  font-size: 14px;
  color: var(--color-text-muted);
  padding-top: 16px;
  border-top: 1px dashed var(--color-border-light);
}

.stat-item {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: color var(--transition-fast);
}

.stat-item .emoji { filter: grayscale(1); transition: 0.3s; font-style: normal; }
.stat-item:hover { color: var(--color-primary); }
.stat-item:hover .emoji { filter: grayscale(0); transform: scale(1.1); }
.stat-item.liked { color: var(--color-danger); }
.stat-item.liked .emoji { filter: grayscale(0); }

.report-link {
  color: var(--color-text-muted);
  font-size: 13px;
  opacity: 0.6;
}
.report-link:hover { opacity: 1; color: var(--color-danger); }

/* 评论区 */
.comments-section {
  max-width: 840px;
  margin: 0 auto;
}

.section-divider {
  display: flex;
  align-items: center;
  margin: 40px 0 24px;
}
.section-divider::before, .section-divider::after {
  content: ''; flex: 1; border-bottom: 1px solid var(--color-border);
}
.divider-text {
  padding: 0 16px; font-weight: 600; color: var(--color-text-secondary);
}

.reply-card {
  margin-bottom: 32px;
  background: var(--color-bg-card);
  padding: 20px;
}

.reply-textarea :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  background: #F8FAFC;
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  transition: all var(--transition-fast);
}
.reply-textarea :deep(.el-textarea__inner:focus) {
  background: #FFF; border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(42,157,143,0.1);
}

.comment-input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.send-btn { font-weight: 600; padding: 0 24px; }

.login-hint {
  text-align: center; color: var(--color-text-secondary); padding: 24px; margin-bottom: 32px; border: 1px dashed var(--color-border);
}
.login-hint .link { color: var(--color-primary); font-weight: 600; }

.comments-list { display: flex; flex-direction: column; gap: 16px; }

.comment-item {
  background: rgba(255,255,255,0.7);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}
.comment-item:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }

.comment-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }

.comment-avatar { background: var(--color-accent-purple); color: #fff; font-weight: bold; }
.comment-meta { display: flex; flex-direction: column; }
.comment-author { font-size: 15px; font-weight: 600; color: var(--color-text-primary); }
.comment-time { font-size: 12px; color: var(--color-text-muted); }

.comment-content {
  font-size: 15px; line-height: 1.6; color: #4A5568; margin-bottom: 16px; padding-left: 48px;
}

.comment-actions {
  display: flex; gap: 20px; font-size: 13px; color: var(--color-text-muted); padding-left: 48px;
}

/* 动效补给 */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
