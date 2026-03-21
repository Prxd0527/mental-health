<template>
  <div class="chat-page">
    <div class="chat-sidebar">
      <h3 class="sidebar-title">会话列表</h3>
      <div v-for="conv in conversations" :key="conv.targetId" class="conv-item" :class="{ active: activeTarget === conv.targetId }" @click="selectConversation(conv)">
        <el-avatar :size="36">{{ (conv.targetName || '?').charAt(0) }}</el-avatar>
        <div class="conv-info">
          <span class="conv-name">{{ conv.targetName || '用户' + conv.targetId }}</span>
          <span class="conv-last">{{ conv.lastMessage || '' }}</span>
        </div>
      </div>
      <el-empty v-if="!conversations.length" description="暂无会话" :image-size="60" />
    </div>
    <div class="chat-main">
      <div class="chat-messages" ref="messageListRef">
        <div v-for="msg in messages" :key="msg.id || msg.createTime" class="msg-row" :class="{ 'msg-self': msg.senderId === authStore.userId }">
          <el-avatar :size="32">{{ msg.senderId === authStore.userId ? '我' : '对' }}</el-avatar>
          <div class="msg-bubble">{{ msg.content }}</div>
        </div>
        <el-empty v-if="!messages.length && activeTarget" description="暂无消息，开始聊天吧" :image-size="60" />
      </div>
      <div class="chat-input" v-if="activeTarget">
        <el-input v-model="inputMessage" placeholder="输入消息..." @keyup.enter="sendMessage" />
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
      <div v-else class="chat-placeholder">选择一个会话开始聊天</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getChatHistory, getConversationList } from '@/api/chat'

const authStore = useAuthStore()
const conversations = ref([])
const messages = ref([])
const activeTarget = ref(null)
const inputMessage = ref('')
const messageListRef = ref(null)
let ws = null

function connectWebSocket() {
  if (!authStore.userId) return
  const wsUrl = `ws://${window.location.host}/ws/chat/${authStore.userId}`
  ws = new WebSocket(wsUrl)
  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (msg.senderId === activeTarget.value || msg.receiverId === activeTarget.value) {
        messages.value.push(msg)
        scrollToBottom()
      }
    } catch { /* ignore */ }
  }
  ws.onclose = () => { setTimeout(connectWebSocket, 3000) }
}

async function selectConversation(conv) {
  activeTarget.value = conv.targetId
  try {
    const res = await getChatHistory(conv.targetId)
    messages.value = res.data || []
    scrollToBottom()
  } catch { /* ignore */ }
}

function sendMessage() {
  if (!inputMessage.value.trim() || !ws || ws.readyState !== WebSocket.OPEN) return
  const msg = { receiverId: activeTarget.value, content: inputMessage.value, type: 'TEXT' }
  ws.send(JSON.stringify(msg))
  messages.value.push({ senderId: authStore.userId, content: inputMessage.value, createTime: new Date().toISOString() })
  inputMessage.value = ''
  scrollToBottom()
}

function scrollToBottom() {
  nextTick(() => { if (messageListRef.value) messageListRef.value.scrollTop = messageListRef.value.scrollHeight })
}

onMounted(async () => {
  try { const res = await getConversationList(); conversations.value = res.data || [] } catch { /* ignore */ }
  connectWebSocket()
})

onUnmounted(() => { if (ws) ws.close() })
</script>

<style scoped>
.chat-page { display: flex; height: calc(100vh - 64px); background: #fff; margin: -24px -20px; }
.chat-sidebar { width: 280px; border-right: 1px solid var(--color-border-light); padding: 16px; overflow-y: auto; }
.sidebar-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.conv-item { display: flex; align-items: center; gap: 10px; padding: 10px; border-radius: var(--radius-md); cursor: pointer; margin-bottom: 4px; transition: background var(--transition-fast); }
.conv-item:hover, .conv-item.active { background: var(--color-primary-lighter); }
.conv-info { flex: 1; min-width: 0; }
.conv-name { display: block; font-size: 14px; font-weight: 500; }
.conv-last { display: block; font-size: 12px; color: var(--color-text-muted); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.chat-main { flex: 1; display: flex; flex-direction: column; }
.chat-messages { flex: 1; padding: 20px; overflow-y: auto; }
.msg-row { display: flex; gap: 8px; margin-bottom: 12px; }
.msg-row.msg-self { flex-direction: row-reverse; }
.msg-bubble { max-width: 70%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.5; background: var(--color-bg); }
.msg-self .msg-bubble { background: var(--color-primary); color: #fff; }
.chat-input { display: flex; gap: 8px; padding: 16px; border-top: 1px solid var(--color-border-light); }
.chat-input .el-input { flex: 1; }
.chat-placeholder { flex: 1; display: flex; align-items: center; justify-content: center; color: var(--color-text-muted); }
</style>
