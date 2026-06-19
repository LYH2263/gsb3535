<template>
  <div class="app-shell">
    <el-alert
      v-if="globalError"
      class="error-banner"
      type="error"
      :title="globalError"
      closable
      @close="clearGlobalError"
    />
    <router-view />
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useStore } from "vuex";

const store = useStore();
const globalError = computed(() => store.getters["ui/globalError"]);

const clearGlobalError = () => store.dispatch("ui/clearGlobalError");
</script>

<style>
:root {
  --apple-bg: #f5f5f7;
  --apple-card-bg: rgba(255, 255, 255, 0.8);
  --apple-text: #1d1d1f;
  --apple-text-secondary: #86868b;
  --apple-blue: #0071e3;
  --apple-radius: 12px;
  --apple-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  --apple-glass: saturate(180%) blur(20px);
}

body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Myriad Set Pro", "SF Pro Icons", "Helvetica Neue", Helvetica, Arial, sans-serif;
  background-color: var(--apple-bg);
  color: var(--apple-text);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 全局滚动条优化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #d2d2d7;
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: #86868b;
}

/* 元素进入动效 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>

<style scoped>
.app-shell {
  min-height: 100vh;
  background-color: var(--apple-bg);
}

.error-banner {
  margin: 16px;
  border-radius: var(--apple-radius);
}
</style>
