<template>
  <div class="not-found">
    <div class="code">404</div>
    <div class="msg">页面不存在</div>
    <el-button type="primary" @click="router.push('/preview')">返回预览中心</el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
const router = useRouter();
</script>

<style lang="scss" scoped>
.not-found {
  min-height: 100vh;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 16px;
  .code { font-size: 96px; font-weight: bold; color: var(--cft-primary); }
  .msg { font-size: 20px; color: var(--cft-text-secondary); margin-bottom: 16px; }
}
</style>
