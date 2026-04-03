<template>
  <div class="chat-page">
    <!-- 侧边栏：会话列表 -->
    <div class="chat-sidebar glass-panel">
      <div class="sidebar-header">
        <h3 class="sidebar-title">近期会话</h3>
        <el-button circle size="small" icon="Edit" @click="$router.push('/counselors')" title="寻找咨询师"></el-button>
      </div>

      <div class="conv-list scroll-invisible">
        <transition-group name="list">
          <div 
            v-for="conv in conversations" 
            :key="conv.userId" 
            class="conv-item" 
            :class="{ active: activeTarget === conv.userId }" 
            @click="selectConversation(conv)"
          >
            <div class="conv-avatar-wrapper">
              <el-avatar :size="44" class="conv-avatar">{{ (conv.username || '?').charAt(0) }}</el-avatar>
              <!-- 假扮的小绿点体现实时状态 -->
              <span class="status-dot"></span>
            </div>
            <div class="conv-info">
              <div class="conv-info-top">
                <span class="conv-name">{{ conv.username || '神秘用户 ' + conv.userId }}</span>
                <!-- Optional Time -->
                <span class="conv-meta">最近</span>
              </div>
              <span class="conv-last">{{ conv.lastMessage || '...' }}</span>
            </div>
          </div>
        </transition-group>
        <el-empty v-if="!conversations.length" description="还没有聊天记录" :image-size="80" />
      </div>
    </div>

    <!-- 聊天主界面 -->
    <div class="chat-main" :class="{ 'has-target': activeTarget }">
      <template v-if="activeTarget">
        <!-- 聊天室头部 -->
        <div class="chat-header">
          <div class="chat-header-info">
            <span class="active-name">{{ activeConvName }}</span>
            <span class="active-status">沟通中 🌿</span>
          </div>
          <el-button text class="history-btn">历史记录</el-button>
        </div>

        <!-- 聊天流 -->
        <div class="chat-messages scroll-invisible" ref="messageListRef">
          <transition-group name="slide-up">
            <div 
              v-for="msg in messages" 
              :key="msg.id || msg.createTime" 
              class="msg-row" 
              :class="{ 'msg-self': msg.senderId === authStore.userId, 'msg-others': msg.senderId !== authStore.userId }"
            >
              <el-avatar :size="36" class="msg-avatar shadow-sm">
                {{ msg.senderId === authStore.userId ? '我' : (activeConvName.charAt(0) || '对') }}
              </el-avatar>
              <div class="msg-content">
                <div class="msg-bubble shadow-sm">{{ msg.content }}</div>
                <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
              </div>
            </div>
          </transition-group>
          <div v-if="!messages.length" class="chat-empty-hint">
            <i class="emoji">👋</i>
            <p>勇敢踏出第一步，向 Ta 问声好吧</p>
          </div>
        </div>

        <!-- 发送区 -->
        <div class="chat-input-area">
          <div class="chat-tools">
            <i class="tool-icon">😀</i>
            <i class="tool-icon">📎</i>
            <i class="tool-icon">🖼️</i>
          </div>
          <div class="input-box">
            <el-input 
              v-model="inputMessage" 
              type="textarea"
              :rows="2"
              placeholder="轻敲几句，传递温暖..." 
              @keydown.enter.prevent="handleEnter"
              class="chat-textarea"
            />
            <el-button type="primary" class="send-btn" @click="sendMessage" round :disabled="!inputMessage.trim()">
              发送 <span style="margin-left: 4px;">🚀</span>
            </el-button>
          </div>
        </div>
      </template>

      <!-- 未选中时的默认占位图 -->
      <div v-else class="chat-placeholder">
        <div class="placeholder-blob"></div>
        <i class="placeholder-icon">💬</i>
        <h3>选择一个会话</h3>
        <p>让沟通连接彼此的心灵</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getChatHistory, getConversationList } from '@/api/chat'

const authStore = useAuthStore()
const conversations = ref([])
const messages = ref([])
const activeTarget = ref(null)
const inputMessage = ref('')
const messageListRef = ref(null)
let ws = null

const activeConvName = computed(() => {
  if (!activeTarget.value) return ''
  const c = conversations.value.find(c => c.userId === activeTarget.value)
  return c?.username || '用户 ' + activeTarget.value
})

function formatTime(isoStr) {
  if (!isoStr) return ''
  const d = new Date(isoStr)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function connectWebSocket() {
  if (!authStore.userId) return
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/chat/${authStore.userId}`
  ws = new WebSocket(wsUrl)
  
  ws.onmessage = async (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (msg.senderId === activeTarget.value || msg.receiverId === activeTarget.value) {
        messages.value.push(msg)
        scrollToBottom()
      }
      const res = await getConversationList()
      conversations.value = res.data || []
    } catch { /* ignore */ }
  }
  ws.onclose = () => { setTimeout(connectWebSocket, 3000) }
}

async function selectConversation(conv) {
  if (activeTarget.value === conv.userId) return
  activeTarget.value = conv.userId
  try {
    const res = await getChatHistory(conv.userId)
    messages.value = res.data || []
    scrollToBottom()
  } catch { /* ignore */ }
}

function handleEnter(e) {
  if (!e.shiftKey) {
    sendMessage()
  }
}

async function sendMessage() {
  if (!inputMessage.value.trim() || !ws || ws.readyState !== WebSocket.OPEN) return
  const text = inputMessage.value.trim()
  const msg = { receiverId: activeTarget.value, content: text, type: 'TEXT' }
  ws.send(JSON.stringify(msg))
  
  messages.value.push({ senderId: authStore.userId, content: text, createTime: new Date().toISOString() })
  
  const conv = conversations.value.find(c => c.userId === activeTarget.value)
  if (conv) conv.lastMessage = text

  inputMessage.value = ''
  scrollToBottom()

  try {
    const res = await getConversationList()
    conversations.value = res.data || []
  } catch { /* ignore */ }
}

function scrollToBottom() {
  nextTick(() => { 
    if (messageListRef.value) {
      // 丝滑滚到最底
      messageListRef.value.scrollTo({
        top: messageListRef.value.scrollHeight,
        behavior: 'smooth'
      })
    } 
  })
}

onMounted(async () => {
  try { 
    const res = await getConversationList()
    conversations.value = res.data || [] 
  } catch { /* ignore */ }
  connectWebSocket()
})

onUnmounted(() => { if (ws) ws.close() })
</script>

<style scoped>
.chat-page { 
  display: flex; 
  height: calc(100vh - 80px); /* Adjust based on padding */
  margin: -10px -10px; /* Offset some container padding if necessary */
  background: var(--color-bg); 
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.02);
  overflow: hidden;
  border: 1px solid rgba(0,0,0,0.03);
}

.scroll-invisible::-webkit-scrollbar { width: 4px; }
.scroll-invisible::-webkit-scrollbar-thumb { background: transparent; border-radius: 4px; }
.scroll-invisible:hover::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.15); }

/* --- Sidebar --- */
.chat-sidebar { 
  width: 300px; 
  display: flex; flex-direction: column;
  background: rgba(255, 255, 255, 0.9);
  border-right: 1px solid rgba(0,0,0,0.05); 
  z-index: 10;
}

.sidebar-header { 
  padding: 24px 20px 16px; 
  display: flex; justify-content: space-between; align-items: center;
}
.sidebar-title { font-size: 18px; font-weight: 700; color: var(--color-text-primary); }

.conv-list { flex: 1; padding: 0 12px 20px; overflow-y: auto; }

.conv-item { 
  display: flex; align-items: center; gap: 12px; 
  padding: 12px; border-radius: 16px; 
  cursor: pointer; margin-bottom: 8px; 
  transition: all var(--transition-fast); 
  border: 1px solid transparent;
}
.conv-item:hover { background: #f8fafc; }
.conv-item.active { 
  background: #fff; 
  border-color: rgba(42, 157, 143, 0.2); 
  box-shadow: 0 4px 12px rgba(42, 157, 143, 0.08); 
  transform: scale(1.02);
}

.conv-avatar-wrapper { position: relative; }
.conv-avatar { background: linear-gradient(135deg, var(--color-primary-light), var(--color-accent-purple)); color: #fff; font-weight: 600; font-size: 16px; }
.status-dot { position: absolute; bottom: 0; right: 0; width: 12px; height: 12px; border-radius: 50%; background: #48bb78; border: 2px solid #fff; }

.conv-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.conv-info-top { display: flex; justify-content: space-between; align-items: center; }
.conv-name { font-size: 15px; font-weight: 600; color: var(--color-text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.conv-meta { font-size: 12px; color: var(--color-text-muted); }
.conv-last { font-size: 13px; color: var(--color-text-muted); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; opacity: 0.8; }

/* --- Chat Main --- */
.chat-main { 
  flex: 1; display: flex; flex-direction: column; 
  background: var(--color-bg-card); 
  position: relative;
}

/* Header */
.chat-header { 
  padding: 20px 24px; display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid rgba(0,0,0,0.03); background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px); z-index: 10;
}
.chat-header-info { display: flex; flex-direction: column; }
.active-name { font-size: 18px; font-weight: 700; color: var(--color-text-primary); }
.active-status { font-size: 13px; color: var(--color-primary); font-weight: 500; margin-top: 2px; }
.history-btn { font-size: 13px; }

/* Messages */
.chat-messages { flex: 1; padding: 24px; overflow-y: auto; display: flex; flex-direction: column; }

.msg-row { display: flex; gap: 12px; margin-bottom: 24px; align-items: flex-end; }
.msg-self { flex-direction: row-reverse; }

.msg-avatar { background: #e2e8f0; color: var(--color-text-secondary); font-size: 14px; font-weight: 700; }
.msg-self .msg-avatar { background: var(--color-primary); color: #fff; }

.msg-content { display: flex; flex-direction: column; max-width: 65%; gap: 6px; }
.msg-self .msg-content { align-items: flex-end; }

.msg-bubble { 
  padding: 12px 18px; 
  font-size: 15px; line-height: 1.6; 
  word-wrap: break-word; 
  transition: all 0.3s ease;
}

/* 对方消息 (左侧) */
.msg-others .msg-bubble {
  background: #fff; 
  color: #334155;
  border-radius: 20px 20px 20px 4px;
  border: 1px solid rgba(0,0,0,0.05);
}

/* 己方消息 (右侧) */
.msg-self .msg-bubble {
  background: linear-gradient(135deg, var(--color-primary), #207e73); 
  color: #fff; 
  border-radius: 20px 20px 4px 20px;
}

.msg-time { font-size: 11px; color: var(--color-text-muted); opacity: 0; transition: opacity 0.2s; }
.msg-row:hover .msg-time { opacity: 1; }

.chat-empty-hint { margin: auto; text-align: center; color: var(--color-text-secondary); opacity: 0.6; }
.chat-empty-hint .emoji { font-size: 48px; font-style: normal; margin-bottom: 12px; display: block; animation: wave 2s infinite; transform-origin: bottom center; }
@keyframes wave { 0%, 100% { transform: rotate(0); } 50% { transform: rotate(15deg); } }

/* Slide Up Transition */
.slide-up-enter-active, .slide-up-leave-active { transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.slide-up-enter-from { opacity: 0; transform: translateY(20px) scale(0.95); }
.slide-up-leave-to { opacity: 0; transform: translateY(-20px); }

/* Input Area */
.chat-input-area { 
  padding: 16px 24px; background: #fff; border-top: 1px solid rgba(0,0,0,0.04);
}
.chat-tools { display: flex; gap: 16px; margin-bottom: 12px; padding-left: 8px; }
.tool-icon { font-style: normal; font-size: 18px; cursor: pointer; filter: grayscale(1); opacity: 0.6; transition: 0.2s; }
.tool-icon:hover { filter: grayscale(0); opacity: 1; transform: scale(1.1); }

.input-box { display: flex; gap: 16px; align-items: flex-end; }
.chat-textarea :deep(.el-textarea__inner) {
  border: none; background: #f8fafc; border-radius: 12px; padding: 12px 16px; font-size: 15px; resize: none; transition: all 0.3s;
}
.chat-textarea :deep(.el-textarea__inner:focus) { background: #fff; box-shadow: 0 0 0 2px rgba(42,157,143,0.1) inset; }

.send-btn { 
  height: 48px; padding: 0 24px; font-weight: 600; font-size: 15px;
  box-shadow: 0 4px 12px rgba(42, 157, 143, 0.2);
}

/* Placeholder State */
.chat-placeholder { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative; overflow: hidden; background: #f8fafc; }
.placeholder-blob { position: absolute; width: 300px; height: 300px; background: radial-gradient(circle, var(--color-primary-light) 0%, transparent 60%); opacity: 0.2; filter: blur(40px); animation: pulse 6s infinite alternate; }
.placeholder-icon { font-style: normal; font-size: 64px; opacity: 0.8; margin-bottom: 24px; z-index: 1; }
.chat-placeholder h3 { font-size: 20px; font-weight: 700; color: var(--color-text-primary); margin-bottom: 8px; z-index: 1; }
.chat-placeholder p { color: var(--color-text-secondary); z-index: 1; }
</style>
