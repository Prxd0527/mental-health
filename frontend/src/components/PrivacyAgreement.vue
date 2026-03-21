<template>
  <el-dialog
    v-model="visible"
    title="📋 用户协议与隐私保护声明"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    center
  >
    <div class="agreement-content">
      <div class="agreement-scroll" ref="scrollRef" @scroll="handleScroll">
        <h3>心灵树洞 —— 隐私保护协议</h3>
        <p>欢迎使用"心灵树洞"大学生心理健康咨询平台（以下简称"本平台"）。我们深知您的隐私安全对您至关重要。在此，我们郑重承诺将严格保护您的个人信息。请您在使用本平台前仔细阅读本协议：</p>

        <h4>一、信息收集范围</h4>
        <p>1. 注册信息：学号/邮箱、密码（密文存储）、姓名。</p>
        <p>2. 使用记录：树洞发布内容、评论、心理测评作答记录、预约咨询记录、心情打卡记录。</p>
        <p>3. 通讯数据：在线咨询过程中的文字聊天记录。</p>

        <h4>二、信息使用目的</h4>
        <p>1. 为您提供心理健康评估与咨询服务。</p>
        <p>2. 匿名化聚合统计，以改进平台服务质量。</p>
        <p>3. 在检测到高危风险时，启动危机干预保护机制。</p>

        <h4>三、信息保护措施</h4>
        <p>1. 所有密码均采用 BCrypt 加密存储，不可逆还原。</p>
        <p>2. 树洞匿名功能确保您的身份信息不会在前台公开展示。</p>
        <p>3. 咨询聊天记录仅供您与咨询师双方查阅，管理员不具备查看权限。</p>
        <p>4. 心理测评结果仅本人可查阅，不向第三方披露。</p>

        <h4>四、信息共享限制</h4>
        <p>未经您的明确同意，我们不会将您的个人信息提供给任何第三方，但以下情况除外：</p>
        <p>1. 依据法律法规的要求。</p>
        <p>2. 当检测到高危自伤/自杀风险时，为保障您的人身安全，可能通知学校心理危机干预部门。</p>

        <h4>五、免责声明</h4>
        <p>1. 本平台提供的心理测评与科普内容仅供参考，不构成专业医疗诊断。</p>
        <p>2. 如您正在经历严重的心理困扰，请及时联系专业心理咨询机构或拨打24小时心理援助热线：<strong>400-161-9995</strong>。</p>
        <p>3. 树洞中的用户生成内容不代表本平台观点。</p>

        <h4>六、协议更新</h4>
        <p>本协议可能随平台功能迭代而更新，更新后将在首次登录时再次弹出提示。</p>

        <p class="agreement-date">最后更新日期：2026年3月20日</p>
      </div>
    </div>

    <template #footer>
      <div class="agreement-footer">
        <el-checkbox v-model="agreed" :disabled="!scrolledToBottom">
          我已仔细阅读并同意以上全部条款
        </el-checkbox>
        <p v-if="!scrolledToBottom" class="scroll-hint">
          ⬇️ 请先阅读完全部内容
        </p>
        <el-button
          type="primary"
          round
          size="large"
          :disabled="!agreed"
          @click="handleAgree"
          class="agree-btn"
        >
          同意并继续
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'

const visible = ref(false)
const agreed = ref(false)
const scrolledToBottom = ref(false)
const scrollRef = ref(null)

const emit = defineEmits(['agreed'])

function handleScroll() {
  if (!scrollRef.value) return
  const el = scrollRef.value
  const nearBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 30
  if (nearBottom) {
    scrolledToBottom.value = true
  }
}

function handleAgree() {
  if (!agreed.value) return
  visible.value = false
  emit('agreed')
}

function open() {
  agreed.value = false
  scrolledToBottom.value = false
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped>
.agreement-content {
  max-height: 400px;
}

.agreement-scroll {
  max-height: 380px;
  overflow-y: auto;
  padding: 0 8px;
  font-size: 14px;
  line-height: 1.8;
  color: var(--color-text-primary);
}

.agreement-scroll h3 {
  text-align: center;
  margin-bottom: 16px;
  color: var(--color-primary);
}

.agreement-scroll h4 {
  margin: 16px 0 8px;
  color: var(--color-text-primary);
  font-size: 15px;
}

.agreement-scroll p {
  margin-bottom: 6px;
  text-indent: 2em;
}

.agreement-date {
  text-align: right;
  color: var(--color-text-muted);
  font-size: 13px;
  margin-top: 16px;
}

.agreement-footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.scroll-hint {
  font-size: 13px;
  color: var(--color-accent-orange);
  margin: 0;
}

.agree-btn {
  width: 200px;
}
</style>
