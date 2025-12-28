<template>
  <div class="chat-message" :class="{ 'user-message': isUser, 'ai-message': !isUser }" @mouseenter="showActions = true" @mouseleave="showActions = false">
    <div class="message-avatar">
      <div class="avatar" :class="{ 'user-avatar': isUser, 'ai-avatar': !isUser }">
        {{ isUser ? '我' : 'AI' }}
      </div>
    </div>
    <div class="message-content">
      <div class="message-bubble">
        <!-- 用户消息使用普通文本 -->
        <pre v-if="isUser" class="message-text">{{ message }}</pre>
        <!-- AI回复使用Markdown渲染 -->
        <div v-else class="message-markdown" v-html="renderedMessage"></div>
      </div>
      <div class="message-footer">
        <div class="message-time">{{ formatTime(timestamp) }}</div>
        <div v-if="showActions" class="message-actions">
          <button class="action-btn" @click="copyMessage" title="复制">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M16 1H4C2.9 1 2 1.9 2 3V17H4V3H16V1ZM19 5H8C6.9 5 6 5.9 6 7V21C6 22.1 6.9 23 8 23H19C20.1 23 21 22.1 21 21V7C21 5.9 20.1 5 19 5ZM19 21H8V7H19V21Z" fill="currentColor"/>
            </svg>
          </button>
        </div>
      </div>
      <div v-if="copied" class="copy-tooltip">已复制!</div>
    </div>
  </div>
</template>

<script>
import { formatTime } from '../utils/index.js'
import { marked } from 'marked'

export default {
  name: 'ChatMessage',
  props: {
    message: {
      type: String,
      required: true
    },
    isUser: {
      type: Boolean,
      default: false
    },
    timestamp: {
      type: Date,
      default: () => new Date()
    }
  },
  data() {
    return {
      showActions: false,
      copied: false
    }
  },
  computed: {
    renderedMessage() {
      if (this.isUser) {
        return this.message
      }
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
      return marked(this.message)
    }
  },
  methods: {
    formatTime,
    async copyMessage() {
      try {
        await navigator.clipboard.writeText(this.message)
        this.copied = true
        setTimeout(() => {
          this.copied = false
        }, 2000)
      } catch (err) {
        console.error('复制失败:', err)
      }
    }
  }
}
</script>

<style scoped>
.chat-message {
  display: flex;
  margin-bottom: 24px;
  padding: 0;
  position: relative;
  animation: slideIn 0.3s ease-out;
}

.user-message {
  justify-content: flex-end;
  flex-direction: row;
}

.user-message .message-avatar {
  order: 2;
  margin-left: 12px;
  margin-right: 0;
}

.user-message .message-content {
  order: 1;
}

.ai-message {
  justify-content: flex-start;
  flex-direction: row;
}

.ai-message .message-avatar {
  order: 1;
  margin-right: 12px;
  margin-left: 0;
}

.ai-message .message-content {
  order: 2;
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

.message-avatar {
  display: flex;
  align-items: flex-start;
  flex-shrink: 0;
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
  transition: transform 0.2s;
}

.avatar:hover {
  transform: scale(1.05);
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.ai-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.message-content {
  max-width: 75%;
  min-width: 100px;
  position: relative;
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 20px;
  position: relative;
  word-wrap: break-word;
  word-break: break-word;
  transition: transform 0.2s, box-shadow 0.2s;
}

.user-message .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 6px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.user-message .message-bubble:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.ai-message .message-bubble {
  background-color: #ffffff;
  color: #333;
  border-bottom-left-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.ai-message .message-bubble:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.message-text {
  font-family: inherit;
  font-size: 14px;
  line-height: 1.4;
  white-space: pre-wrap;
  margin: 0;
}

/* Markdown样式 */
.message-markdown {
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
}

.message-markdown h1,
.message-markdown h2,
.message-markdown h3,
.message-markdown h4,
.message-markdown h5,
.message-markdown h6 {
  margin: 0.5em 0;
  font-weight: bold;
}

.message-markdown h1 { font-size: 1.5em; }
.message-markdown h2 { font-size: 1.3em; }
.message-markdown h3 { font-size: 1.2em; }
.message-markdown h4 { font-size: 1.1em; }
.message-markdown h5 { font-size: 1em; }
.message-markdown h6 { font-size: 0.9em; }

.message-markdown p {
  margin: 0.5em 0;
}

.message-markdown ul,
.message-markdown ol {
  margin: 0.5em 0;
  padding-left: 1.5em;
}

.message-markdown li {
  margin: 0.2em 0;
}

.message-markdown code {
  background-color: rgba(0, 0, 0, 0.08);
  padding: 0.2em 0.5em;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', 'Menlo', monospace;
  font-size: 0.9em;
  color: #e83e8c;
  font-weight: 500;
}

.user-message .message-markdown code {
  background-color: rgba(255, 255, 255, 0.25);
  color: rgba(255, 255, 255, 0.95);
}

.message-markdown pre {
  background-color: #282c34;
  padding: 1em;
  border-radius: 8px;
  overflow-x: auto;
  margin: 0.8em 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.user-message .message-markdown pre {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.message-markdown pre code {
  background-color: transparent;
  padding: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', 'Menlo', monospace;
  font-size: 0.9em;
  color: #abb2bf;
}

.user-message .message-markdown pre code {
  color: rgba(255, 255, 255, 0.95);
}

.message-markdown blockquote {
  border-left: 4px solid #667eea;
  padding: 0.8em 1em;
  margin: 0.8em 0;
  font-style: italic;
  color: #555;
  background-color: rgba(102, 126, 234, 0.05);
  border-radius: 0 6px 6px 0;
}

.user-message .message-markdown blockquote {
  border-left-color: rgba(255, 255, 255, 0.6);
  background-color: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.95);
}

.message-markdown a {
  color: #007bff;
  text-decoration: underline;
}

.user-message .message-markdown a {
  color: #b3d9ff;
}

.message-markdown table {
  border-collapse: collapse;
  width: 100%;
  margin: 0.8em 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.message-markdown th,
.message-markdown td {
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 0.75em;
  text-align: left;
}

.message-markdown th {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  border-color: rgba(255, 255, 255, 0.2);
}

.user-message .message-markdown th {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.3);
}

.message-markdown td {
  background-color: rgba(255, 255, 255, 0.5);
}

.user-message .message-markdown td {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
}

.message-markdown hr {
  border: none;
  border-top: 1px solid #ddd;
  margin: 1em 0;
}

.user-message .message-markdown hr {
  border-top-color: rgba(255, 255, 255, 0.3);
}

.message-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6px;
  padding: 0 4px;
  position: relative;
}

.message-time {
  font-size: 12px;
  color: #999;
  font-weight: 400;
}

.user-message .message-time {
  text-align: right;
}

.ai-message .message-time {
  text-align: left;
}

.message-actions {
  display: flex;
  gap: 4px;
  opacity: 0.8;
}

.action-btn {
  background: rgba(0, 0, 0, 0.05);
  border: none;
  border-radius: 6px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
  padding: 0;
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.1);
  color: #333;
  transform: scale(1.1);
}

.user-message .action-btn {
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.9);
}

.user-message .action-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.copy-tooltip {
  position: absolute;
  top: -32px;
  right: 0;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  animation: fadeInOut 0.3s ease-out;
  z-index: 10;
}

.user-message .copy-tooltip {
  right: auto;
  left: 0;
}

@keyframes fadeInOut {
  0%, 100% {
    opacity: 0;
    transform: translateY(5px);
  }
  50% {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .message-content {
    max-width: 85%;
  }
  
  .message-bubble {
    padding: 12px 16px;
  }

  .message-actions {
    opacity: 1;
  }
}
</style> 