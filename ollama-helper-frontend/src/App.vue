<template>
  <div class="app">
    <!-- 头部标题 -->
    <div class="app-header">
      <h1 class="app-title">设备服务 问题小助手</h1>
      <div class="app-subtitle">帮助您解答日常开发中设备相关问题</div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-container">
      <!-- 消息列表 -->
      <div class="messages-container" ref="messagesContainer">
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-content">
            <div class="welcome-icon">🤖</div>
            <h2>欢迎使用 设备服务 小助手</h2>
            <p >我可以帮助您：</p>
            <ul>
              <li>解答日常开发中设备相关问题</li>
            </ul>
            <p>请随时向我提问吧！</p>
          </div>
        </div>

        <!-- 历史消息 -->
        <ChatMessage
            v-for="message in messages"
            :key="message.id"
            :message="message.content"
            :is-user="message.isUser"
            :timestamp="message.timestamp"
        />

        <!-- AI 正在回复的消息 -->
        <div v-if="isAiTyping" class="chat-message ai-message">
          <div class="message-avatar">
            <div class="avatar ai-avatar">设备助手</div>
          </div>
          <div class="message-content">
            <div class="message-bubble">
              <div class="ai-typing-content">
                <div class="ai-response-text message-markdown" v-html="currentAiResponseRendered"></div>
                <LoadingDots v-if="isStreaming" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <ChatInput
          :disabled="isAiTyping"
          @send-message="sendMessage"
          placeholder="请输入您的问题..."
      />
    </div>

    <!-- 连接状态提示 -->
    <div v-if="connectionError" class="connection-error">
      <div class="error-content">
        <span class="error-icon">⚠️</span>
        <span>连接服务器失败，请检查后端服务是否启动</span>
      </div>
    </div>
  </div>
</template>

<script>
import ChatMessage from './components/ChatMessage.vue'
import ChatInput from './components/ChatInput.vue'
import LoadingDots from './components/LoadingDots.vue'
import { chatWithSSE } from './api/chatApi.js'
import { generateMemoryId } from './utils/index.js'
import { marked } from 'marked'

export default {
  name: 'App',
  components: {
    ChatMessage,
    ChatInput,
    LoadingDots
  },
  data() {
    return {
      messages: [],
      memoryId: null,
      isAiTyping: false,
      isStreaming: false,
      currentAiResponse: '',
      currentEventSource: null,
      connectionError: false
    }
  },
  computed: {
    currentAiResponseRendered() {
      if (!this.currentAiResponse) return ''
      // 配置marked选项
      marked.setOptions({
        breaks: true, // 支持换行
        gfm: true, // 支持GitHub风格的Markdown
        sanitize: false, // 不过滤HTML（根据需要可以开启）
        highlight: function(code, lang) {
          // 可以在这里添加代码高亮功能
          return code
        }
      })
      return marked(this.currentAiResponse)
    }
  },
  methods: {
    sendMessage(message) {
      // 添加用户消息
      this.addMessage(message, true)

      // 开始AI回复
      this.startAiResponse(message)
    },

    addMessage(content, isUser = false) {
      const message = {
        id: Date.now() + Math.random(),
        content,
        isUser,
        timestamp: new Date()
      }
      this.messages.push(message)
      this.scrollToBottom()
    },

    startAiResponse(userMessage) {
      this.isAiTyping = true
      this.isStreaming = true
      this.currentAiResponse = ''
      this.connectionError = false

      // 关闭之前的连接
      if (this.currentEventSource) {
        this.currentEventSource.close()
      }

      // 开始SSE连接
      this.currentEventSource = chatWithSSE(
          this.memoryId,
          userMessage,
          this.handleAiMessage,
          this.handleAiError,
          this.handleAiClose
      )
    },

    handleAiMessage(data) {
      this.currentAiResponse += data
      this.scrollToBottom()
    },

    handleAiError(error) {
      console.error('AI 回复出错:', error)
      this.connectionError = true
      this.finishAiResponse()

      // 5秒后自动隐藏错误提示
      setTimeout(() => {
        this.connectionError = false
      }, 5000)
    },

    handleAiClose() {
      this.finishAiResponse()
    },

    finishAiResponse() {
      this.isStreaming = false

      // 如果有内容，添加到消息列表
      if (this.currentAiResponse.trim()) {
        this.addMessage(this.currentAiResponse.trim(), false)
      }

      // 重置状态
      this.isAiTyping = false
      this.currentAiResponse = ''

      // 重置连接错误状态（确保正常结束时清除错误提示）
      this.connectionError = false

      // 关闭连接
      if (this.currentEventSource) {
        this.currentEventSource.close()
        this.currentEventSource = null
      }
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },

    initializeChat() {
      this.memoryId = generateMemoryId()
      console.log('聊天室ID:', this.memoryId)
    }
  },

  mounted() {
    this.initializeChat()
  },

  beforeUnmount() {
    // 组件销毁前关闭连接
    if (this.currentEventSource) {
      this.currentEventSource.close()
    }
  }
}
</script>

<style scoped>
.app {
  height: 95vh;
  width: 95%;
  max-width: 1200px;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px 32px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.app-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: -0.5px;
}

.app-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 8px;
  font-weight: 400;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f8f9fa;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  scroll-behavior: smooth;
  display: flex;
  flex-direction: column;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
}

.welcome-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.welcome-content {
  text-align: center;
  max-width: 500px;
  color: #666;
  animation: fadeIn 0.6s ease-out;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 24px;
  animation: bounce 2s infinite;
}

.welcome-content h2 {
  font-size: 24px;
  margin-bottom: 16px;
  color: #333;
  font-weight: 600;
}

.welcome-content p {
  margin-bottom: 12px;
  line-height: 1.6;
  color: #666;
}

.welcome-content ul {
  text-align: left;
  margin: 20px auto;
  max-width: 300px;
  list-style: none;
  padding: 0;
}

.welcome-content li {
  margin-bottom: 12px;
  padding-left: 24px;
  position: relative;
  color: #555;
}

.welcome-content li:before {
  content: "✨";
  position: absolute;
  left: 0;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

/* AI 正在回复时的消息样式 */
.chat-message {
  display: flex;
  margin-bottom: 24px;
  animation: slideIn 0.3s ease-out;
}

.ai-message {
  justify-content: flex-start;
  flex-direction: row;
}

.message-avatar {
  display: flex;
  align-items: flex-start;
  margin-right: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.ai-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.message-content {
  max-width: 75%;
  min-width: 100px;
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 20px;
  position: relative;
  word-wrap: break-word;
  word-break: break-word;
  background-color: #ffffff;
  color: #333;
  border-bottom-left-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
}

.message-bubble:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-typing-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-response-text {
  font-size: 14px;
  line-height: 1.5;
}

/* AI实时回复的Markdown样式 */
.ai-response-text.message-markdown h1,
.ai-response-text.message-markdown h2,
.ai-response-text.message-markdown h3,
.ai-response-text.message-markdown h4,
.ai-response-text.message-markdown h5,
.ai-response-text.message-markdown h6 {
  margin: 0.5em 0;
  font-weight: bold;
}

.ai-response-text.message-markdown h1 { font-size: 1.5em; }
.ai-response-text.message-markdown h2 { font-size: 1.3em; }
.ai-response-text.message-markdown h3 { font-size: 1.2em; }
.ai-response-text.message-markdown h4 { font-size: 1.1em; }
.ai-response-text.message-markdown h5 { font-size: 1em; }
.ai-response-text.message-markdown h6 { font-size: 0.9em; }

.ai-response-text.message-markdown p {
  margin: 0.5em 0;
}

.ai-response-text.message-markdown ul,
.ai-response-text.message-markdown ol {
  margin: 0.5em 0;
  padding-left: 1.5em;
}

.ai-response-text.message-markdown li {
  margin: 0.2em 0;
}

.ai-response-text.message-markdown code {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.ai-response-text.message-markdown pre {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 1em;
  border-radius: 5px;
  overflow-x: auto;
  margin: 0.5em 0;
}

.ai-response-text.message-markdown pre code {
  background-color: transparent;
  padding: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.ai-response-text.message-markdown blockquote {
  border-left: 4px solid #ccc;
  padding-left: 1em;
  margin: 0.5em 0;
  font-style: italic;
  color: #666;
}

.ai-response-text.message-markdown a {
  color: #007bff;
  text-decoration: underline;
}

.ai-response-text.message-markdown table {
  border-collapse: collapse;
  width: 100%;
  margin: 0.5em 0;
}

.ai-response-text.message-markdown th,
.ai-response-text.message-markdown td {
  border: 1px solid #ddd;
  padding: 0.5em;
  text-align: left;
}

.ai-response-text.message-markdown th {
  background-color: #f2f2f2;
  font-weight: bold;
}

.ai-response-text.message-markdown hr {
  border: none;
  border-top: 1px solid #ddd;
  margin: 1em 0;
}

.connection-error {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 14px 24px;
  border-radius: 12px;
  z-index: 1000;
  animation: slideDown 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  box-shadow: 0 8px 24px rgba(245, 87, 108, 0.4);
}

.error-content {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
}

.error-icon {
  font-size: 18px;
}

@keyframes slideDown {
  from {
    transform: translateX(-50%) translateY(-100%);
    opacity: 0;
    scale: 0.8;
  }
  to {
    transform: translateX(-50%) translateY(0);
    opacity: 1;
    scale: 1;
  }
}

/* 滚动条样式 */
.messages-container::-webkit-scrollbar {
  width: 8px;
}

.messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.messages-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: background 0.3s;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5568d3 0%, #653d91 100%);
}

@media (max-width: 768px) {
  .app {
    width: 100%;
    height: 100vh;
    border-radius: 0;
  }

  .app-header {
    padding: 20px 16px;
  }

  .app-title {
    font-size: 22px;
  }

  .app-subtitle {
    font-size: 13px;
  }

  .messages-container {
    padding: 16px;
  }

  .welcome-content {
    padding: 0 10px;
  }

  .welcome-icon {
    font-size: 48px;
  }

  .message-content {
    max-width: 85%;
  }

  .message-bubble {
    padding: 12px 16px;
  }
}
</style>