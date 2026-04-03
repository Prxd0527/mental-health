<template>
  <div class="article-manage-page">
    <h1 class="section-title">文章管理</h1>
    <el-button type="primary" @click="openEditor()" style="margin-bottom:16px" round>＋ 新增文章</el-button>

    <el-table :data="articles" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="views" label="阅读量" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'发布':'下架' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openEditor(row)">编辑</el-button>
          <el-button v-if="row.status===1" size="small" type="warning" @click="handleToggle(row.id,0)">下架</el-button>
          <el-button v-else size="small" type="success" @click="handleToggle(row.id,1)">发布</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 文章编辑抽屉 (使用抽屉替代对话框以获取更多编辑空间) -->
    <el-drawer
      v-model="showEditor"
      :title="editForm.id ? '编辑文章' : '新增文章'"
      direction="rtl"
      size="780px"
      :close-on-click-modal="false"
      :before-close="handleDrawerClose"
    >
      <el-form :model="editForm" label-width="80px" class="article-form">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category" placeholder="选择分类" allow-create filterable style="width:100%">
            <el-option label="文章" value="文章" />
            <el-option label="公告" value="公告" />
            <el-option label="活动" value="活动" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" class="editor-form-item">
          <!-- WangEditor 工具栏 -->
          <div class="editor-container">
            <Toolbar
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="default"
              class="editor-toolbar"
            />
            <!-- WangEditor 编辑区 -->
            <Editor
              v-model="editForm.content"
              :defaultConfig="editorConfig"
              mode="default"
              class="editor-content"
              @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditor = false" round>取消</el-button>
        <el-button type="primary" @click="handleSave" round>保存文章</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, shallowRef, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { getArticleManageList, saveArticle, toggleArticleStatus } from '@/api/admin'

// WangEditor 组件
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

const articles = ref([])
const showEditor = ref(false)
const editForm = reactive({ id: null, title: '', category: '', content: '' })

// 编辑器实例
const editorRef = shallowRef(null)

const toolbarConfig = {
  excludeKeys: [
    'uploadVideo', // 不需要视频上传
    'group-video',
  ]
}

const editorConfig = {
  placeholder: '请输入文章内容…',
  MENU_CONF: {
    uploadImage: {
      // 简单方案：禁用图片上传，使用插入网络图片
      customInsert(res, insertFn) {
        // 如果后端有上传接口，可在此处理
      }
    }
  }
}

function handleCreated(editor) {
  editorRef.value = editor
}

function openEditor(row) {
  if (row) {
    Object.assign(editForm, { id: row.id, title: row.title, category: row.category, content: row.content || '' })
  } else {
    editForm.id = null
    editForm.title = ''
    editForm.category = ''
    editForm.content = ''
  }
  showEditor.value = true
}

function handleDrawerClose(done) {
  done()
}

async function handleSave() {
  if (!editForm.title.trim()) { ElMessage.warning('请输入文章标题'); return }
  try {
    await saveArticle(editForm)
    ElMessage.success('保存成功')
    showEditor.value = false
    fetchData()
  } catch { /* ignore */ }
}

async function handleToggle(id, status) {
  try { await toggleArticleStatus(id, status); ElMessage.success('操作成功'); fetchData() } catch { /* ignore */ }
}

async function fetchData() {
  try { const r = await getArticleManageList(); articles.value = r.data?.records || r.data || [] } catch { /* ignore */ }
}

onMounted(fetchData)

// 编辑器实例销毁
onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.destroy()
  }
})
</script>

<style scoped>
.article-manage-page {
  max-width: 1100px;
  margin: 0 auto;
}

.article-form {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.editor-form-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.editor-form-item :deep(.el-form-item__content) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.editor-container {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 400px;
}

.editor-toolbar {
  border-bottom: 1px solid #e5e7eb;
  background: #fafbfc;
}

.editor-content {
  flex: 1;
  overflow-y: auto;
}

/* 覆盖 WangEditor 默认样式使其更融洽 */
.editor-container :deep(.w-e-text-container) {
  min-height: 350px;
  font-size: 15px;
  line-height: 1.75;
}

.editor-container :deep(.w-e-toolbar) {
  background: #fafbfc;
  flex-wrap: wrap;
}
</style>
