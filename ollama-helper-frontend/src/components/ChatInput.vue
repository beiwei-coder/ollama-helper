<template>
  <div class="chat-input">
    <div class="input-container">
      <textarea
        ref="inputRef"
        v-model="inputMessage"
        :placeholder="placeholder"
        :disabled="disabled"
        class="input-textarea"
        rows="1"
        @keydown="handleKeyDown"
        @input="adjustHeight"
      />
      <button
        :disabled="disabled || !inputMessage.trim()"
        @click="sendMessage"
        class="send-button"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M2 21l21-9L2 3v7l15 2-15 2v7z" fill="currentColor"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatInput',
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String,
      default: '请输入您的问题...'
    }
  },
  data() {
    return {
      inputMessage: ''
    }
  },
  methods: {
    sendMessage() {
      if (this.inputMessage.trim() && !this.disabled) {
        this.$emit('send-message', this.inputMessage.trim())
        this.inputMessage = ''
        this.adjustHeight()
      }
    },
    handleKeyDown(event) {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault()
        this.sendMessage()
      }
    },
    adjustHeight() {
      this.$nextTick(() => {
        const textarea = this.$refs.inputRef
        textarea.style.height = 'auto'
        textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
      })
    },
    focus() {
      this.$refs.inputRef.focus()
    }
  },
  mounted() {
    this.adjustHeight()
  }
}
</script>

<style scoped>
.chat-input {
  padding: 20px 24px;
  background: #ffffff;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.03);
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
}

.input-textarea {
  flex: 1;
  padding: 14px 20px;
  border: 2px solid #e1e5e9;
  border-radius: 24px;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  outline: none;
  transition: all 0.3s ease;
  min-height: 48px;
  max-height: 120px;
  overflow-y: auto;
  background-color: #f8f9fa;
  font-family: inherit;
  color: #333;
}

.input-textarea::placeholder {
  color: #999;
  opacity: 0.7;
}

.input-textarea:focus {
  border-color: #667eea;
  background-color: #fff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-textarea:focus::placeholder {
  opacity: 0.5;
}

.input-textarea:disabled {
  background-color: #f5f5f5;
  color: #999;
  cursor: not-allowed;
  border-color: #e1e5e9;
}

.input-textarea::-webkit-scrollbar {
  width: 6px;
}

.input-textarea::-webkit-scrollbar-track {
  background: transparent;
}

.input-textarea::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.input-textarea::-webkit-scrollbar-thumb:hover {
  background: #999;
}

.send-button {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.send-button:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.send-button:active:not(:disabled) {
  transform: scale(0.95);
}

.send-button:disabled {
  background: #ccc;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.send-button svg {
  transition: transform 0.2s;
}

.send-button:hover:not(:disabled) svg {
  transform: translateX(2px);
}

@media (max-width: 768px) {
  .chat-input {
    padding: 16px;
  }
  
  .input-container {
    gap: 10px;
  }
  
  .input-textarea {
    font-size: 16px; /* 防止在移动设备上自动缩放 */
    padding: 12px 16px;
  }

  .send-button {
    width: 44px;
    height: 44px;
  }
}
</style> 