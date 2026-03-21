<template>
  <el-dialog
    v-model="visible"
    title="评价咨询体验"
    width="500px"
    :close-on-click-modal="false"
  >
    <div class="feedback-form">
      <div class="rating-section">
        <p class="rating-label">请为本次咨询打分：</p>
        <el-rate
          v-model="form.rating"
          :colors="['#FF9900', '#FF6600', '#E8A87C']"
          :texts="['很差', '不太好', '一般', '不错', '非常好']"
          show-text
          :size="'large'"
        />
      </div>

      <el-form :model="form" class="content-form">
        <el-form-item>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="分享您的咨询感受（可选）..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.isAnonymousChecked">匿名评价</el-checkbox>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="!form.rating" @click="submit">
          提交评价
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { submitFeedback } from '@/api/feedback'

const visible = ref(false)
const submitting = ref(false)
const form = reactive({
  appointmentId: null,
  rating: 0,
  content: '',
  isAnonymousChecked: true
})

const emit = defineEmits(['submitted'])

const open = (appointmentId) => {
  form.appointmentId = appointmentId
  form.rating = 0
  form.content = ''
  form.isAnonymousChecked = true
  visible.value = true
}

const submit = async () => {
  if (!form.rating || form.rating < 1) {
    ElMessage.warning('请选择评分')
    return
  }
  submitting.value = true
  try {
    await submitFeedback({
      appointmentId: form.appointmentId,
      rating: form.rating,
      content: form.content,
      isAnonymous: form.isAnonymousChecked ? 1 : 0
    })
    ElMessage.success('评价提交成功，感谢您的反馈！')
    visible.value = false
    emit('submitted')
  } catch (e) {
    // request.js handles errors
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.feedback-form {
  padding: 0 8px;
}

.rating-section {
  text-align: center;
  margin-bottom: 20px;
}

.rating-label {
  font-size: 15px;
  color: var(--color-text-primary);
  margin-bottom: 12px;
}

.content-form {
  margin-top: 16px;
}

.dialog-footer {
  text-align: right;
}
</style>
