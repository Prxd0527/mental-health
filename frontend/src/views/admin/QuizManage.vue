<template>
  <div class="quiz-manage-page">
    <h1 class="section-title">题库管理</h1>
    <el-button type="primary" @click="openEditor()" style="margin-bottom:16px" round>＋ 新增量表</el-button>

    <el-table :data="quizzes" stripe class="quiz-table">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="量表名称" />
      <el-table-column prop="questionCount" label="题目数" width="90" align="center">
        <template #default="{ row }">
          <el-tag size="small" round>{{ row.questionCount ?? '-' }} 题</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'上线':'下线' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template #default="{ row }">
          <el-button size="small" @click="openEditor(row)">编辑</el-button>
          <el-button size="small" type="primary" @click="openQuestionPanel(row)">管理题目</el-button>
          <el-button v-if="row.status===1" size="small" type="warning" @click="handleToggle(row.id,0)">下线</el-button>
          <el-button v-else size="small" type="success" @click="handleToggle(row.id,1)">上线</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 量表编辑对话框 -->
    <el-dialog v-model="showEditor" :title="editForm.id?'编辑量表':'新增量表'" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="editForm.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditor=false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- ========== 题目管理抽屉（核心新增区域） ========== -->
    <el-drawer
      v-model="showQuestionDrawer"
      :title="`题目管理 — ${currentQuiz?.title || ''}`"
      direction="rtl"
      size="720px"
      :close-on-click-modal="false"
    >
      <div class="drawer-toolbar">
        <span class="q-count">共 {{ questions.length }} 题</span>
        <el-button type="primary" size="small" round @click="openQuestionEditor()">＋ 添加题目</el-button>
      </div>

      <!-- 题目列表 -->
      <div v-if="questionsLoading" style="padding:40px;text-align:center;">
        <el-skeleton :rows="4" animated />
      </div>
      <div v-else-if="questions.length" class="question-list">
        <div v-for="(q, idx) in questions" :key="q.id" class="question-card">
          <div class="q-header">
            <span class="q-index">Q{{ idx + 1 }}</span>
            <span class="q-type-tag">{{ q.type === 'MULTI' ? '多选' : '单选' }}</span>
            <span class="q-content-preview">{{ q.content }}</span>
            <div class="q-actions">
              <el-button text size="small" type="primary" @click="openQuestionEditor(q)">编辑</el-button>
              <el-button text size="small" type="danger" @click="handleDeleteQuestion(q)">删除</el-button>
            </div>
          </div>
          <div class="q-options">
            <div v-for="(opt, oi) in safeParseOptions(q.options)" :key="oi" class="q-opt-item">
              <span class="opt-label">{{ oi + 1 }}</span>
              <span class="opt-text">{{ opt.label }}</span>
              <span class="opt-score">{{ opt.score }}分</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else style="padding:60px 0;">
        <el-empty description="暂无题目，点击上方按钮添加" :image-size="80" />
      </div>
    </el-drawer>

    <!-- 题目新增/编辑对话框 -->
    <el-dialog v-model="showQuestionEditor" :title="qForm.id ? '编辑题目' : '添加题目'" width="620px" :close-on-click-modal="false">
      <el-form :model="qForm" label-width="90px">
        <el-form-item label="题目内容">
          <el-input v-model="qForm.content" type="textarea" :rows="2" placeholder="输入题目问题" />
        </el-form-item>

        <el-form-item label="题型">
          <el-radio-group v-model="qForm.type">
            <el-radio value="SINGLE">单选</el-radio>
            <el-radio value="MULTI">多选</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="排序号">
          <el-input-number v-model="qForm.sortOrder" :min="1" :max="999" />
        </el-form-item>

        <el-form-item label="选项列表">
          <div class="options-editor">
            <div v-for="(opt, idx) in qForm.optionsList" :key="idx" class="opt-row">
              <span class="opt-seq">{{ idx + 1 }}</span>
              <el-input v-model="opt.label" style="flex:1" placeholder="输入选项文字（如：没有、很轻、中等、严重）" />
              <el-input-number v-model="opt.score" :min="0" :max="100" style="width:110px" />
              <span class="opt-score-hint">分</span>
              <el-button text type="danger" @click="qForm.optionsList.splice(idx, 1)" :disabled="qForm.optionsList.length <= 2">✕</el-button>
            </div>
            <el-button text type="primary" @click="addOption" size="small" style="margin-top:8px">＋ 添加选项</el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showQuestionEditor = false">取消</el-button>
        <el-button type="primary" :loading="qSaving" @click="handleSaveQuestion">保存题目</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQuizManageList, saveQuiz, toggleQuizStatus, getQuizQuestions, saveQuestion, deleteQuestion } from '@/api/admin'

const quizzes = ref([])
const showEditor = ref(false)
const editForm = reactive({ id: null, title: '', description: '' })

// ---- 量表 CRUD ----
function openEditor(row) {
  if (row) { Object.assign(editForm, row) } else { editForm.id = null; editForm.title = ''; editForm.description = '' }
  showEditor.value = true
}

async function handleSave() {
  try { await saveQuiz(editForm); ElMessage.success('保存成功'); showEditor.value = false; fetchData() } catch { /* ignore */ }
}

async function handleToggle(id, status) {
  try { await toggleQuizStatus(id, status); ElMessage.success('操作成功'); fetchData() } catch { /* ignore */ }
}

async function fetchData() {
  try { const r = await getQuizManageList(); quizzes.value = r.data?.records || r.data || [] } catch { /* ignore */ }
}

// ---- 题目管理 ----
const showQuestionDrawer = ref(false)
const currentQuiz = ref(null)
const questions = ref([])
const questionsLoading = ref(false)

async function openQuestionPanel(quiz) {
  currentQuiz.value = quiz
  showQuestionDrawer.value = true
  await fetchQuestions(quiz.id)
}

async function fetchQuestions(quizId) {
  questionsLoading.value = true
  try {
    const r = await getQuizQuestions(quizId)
    questions.value = r.data || []
  } catch { /* ignore */ }
  questionsLoading.value = false
}

function safeParseOptions(optStr) {
  if (!optStr) return []
  try { return JSON.parse(optStr) } catch { return [] }
}

// ---- 题目编辑器 ----
const showQuestionEditor = ref(false)
const qSaving = ref(false)
const qForm = reactive({
  id: null,
  quizId: null,
  type: 'SINGLE',
  content: '',
  sortOrder: 1,
  optionsList: []
})

function openQuestionEditor(q) {
  if (q) {
    qForm.id = q.id
    qForm.quizId = q.quizId
    qForm.type = q.type || 'SINGLE'
    qForm.content = q.content
    qForm.sortOrder = q.sortOrder || 1
    qForm.optionsList = safeParseOptions(q.options)
  } else {
    qForm.id = null
    qForm.quizId = currentQuiz.value?.id
    qForm.type = 'SINGLE'
    qForm.content = ''
    qForm.sortOrder = questions.value.length + 1
    qForm.optionsList = [
      { label: '', score: 0 },
      { label: '', score: 1 },
      { label: '', score: 2 },
      { label: '', score: 3 }
    ]
  }
  showQuestionEditor.value = true
}

function addOption() {
  qForm.optionsList.push({ label: '', score: 0 })
}

async function handleSaveQuestion() {
  if (!qForm.content.trim()) { ElMessage.warning('请输入题目内容'); return }
  if (qForm.optionsList.length < 2) { ElMessage.warning('至少需要两个选项'); return }

  qSaving.value = true
  try {
    await saveQuestion({
      id: qForm.id || undefined,
      quizId: qForm.quizId,
      type: qForm.type,
      content: qForm.content,
      sortOrder: qForm.sortOrder,
      options: JSON.stringify(qForm.optionsList)
    })
    ElMessage.success('题目保存成功')
    showQuestionEditor.value = false
    await fetchQuestions(currentQuiz.value.id)
    fetchData() // 刷新题目计数
  } catch { /* ignore */ }
  qSaving.value = false
}

async function handleDeleteQuestion(q) {
  try {
    await ElMessageBox.confirm(`确认删除题目「${q.content.substring(0, 20)}…」？此操作不可撤销。`, '删除确认', { type: 'warning' })
    await deleteQuestion(q.id)
    ElMessage.success('已删除')
    await fetchQuestions(currentQuiz.value.id)
    fetchData()
  } catch { /* ignore */ }
}

onMounted(fetchData)
</script>

<style scoped>
.quiz-manage-page {
  max-width: 1100px;
  margin: 0 auto;
}

/* 抽屉内部工具栏 */
.drawer-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border-light);
}
.q-count {
  font-size: 14px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

/* 题目卡片 */
.question-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-card {
  padding: 16px 20px;
  border-radius: 12px;
  background: #f8fafa;
  border: 1px solid var(--color-border-light);
  transition: all 0.2s;
}

.question-card:hover {
  border-color: rgba(42, 157, 143, 0.3);
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.q-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.q-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px; height: 24px;
  border-radius: 6px;
  background: var(--color-primary);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.q-type-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(42, 157, 143, 0.08);
  color: var(--color-primary-dark);
  font-weight: 600;
  flex-shrink: 0;
}

.q-content-preview {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.q-actions {
  flex-shrink: 0;
  display: flex;
  gap: 4px;
}

.q-options {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 6px;
  padding-left: 46px;
}

.q-opt-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.opt-label {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px; height: 22px;
  border-radius: 50%;
  background: #e8f0ef;
  color: var(--color-primary-dark);
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}

.opt-score {
  margin-left: auto;
  font-size: 12px;
  color: var(--color-primary);
  font-weight: 600;
  white-space: nowrap;
}

/* 选项编辑器 */
.options-editor {
  width: 100%;
}

.opt-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.opt-seq {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px; height: 26px;
  border-radius: 50%;
  background: #e8f0ef;
  color: var(--color-primary-dark);
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.opt-score-hint {
  font-size: 13px;
  color: var(--color-text-muted);
  flex-shrink: 0;
}
</style>
