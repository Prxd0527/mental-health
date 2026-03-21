<template>
  <el-dialog
    v-model="visible"
    title="举报违规内容"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="举报原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          placeholder="请详细描述举报原因，例如：广告推广、不善言论、涉黄涉暴等..."
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">
          提交
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { submitReport } from '@/api/report'

const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  targetType: '',
  targetId: null,
  reason: ''
})

const rules = {
  reason: [
    { required: true, message: '请输入举报原因', trigger: 'blur' },
    { min: 5, message: '原因描述不能少于5个字符', trigger: 'blur' }
  ]
}

const open = (type, id) => {
  form.targetType = type
  form.targetId = id
  form.reason = ''
  visible.value = true
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await submitReport(form)
        ElMessage.success('举报提交成功，我们将尽快处理。')
        visible.value = false
      } catch (e) {
        // request.js should handle errors
      } finally {
        submitting.value = false
      }
    }
  })
}

defineExpose({
  open
})
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>
