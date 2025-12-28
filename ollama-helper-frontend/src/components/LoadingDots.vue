<template>
  <div class="loading-dots">
    <div class="dot"></div>
    <div class="dot"></div>
    <div class="dot"></div>
  </div>
</template>

<script>
export default {
  name: 'LoadingDots'
}
</script>

<style scoped>
.loading-dots {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 6px;
  padding: 8px 0 4px 0;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  animation: bounce 1.4s infinite ease-in-out;
  box-shadow: 0 2px 4px rgba(102, 126, 234, 0.3);
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0.6) translateY(0);
    opacity: 0.6;
  }
  40% {
    transform: scale(1) translateY(-4px);
    opacity: 1;
  }
}
</style> 