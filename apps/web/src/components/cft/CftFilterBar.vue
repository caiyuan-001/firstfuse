<template>
  <el-card class="cft-filter-bar" shadow="never">
    <el-form :inline="inline" @submit.prevent>
      <slot />
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ inline?: boolean }>(), { inline: true });
</script>

<style lang="scss" scoped>
.cft-filter-bar {
  margin-bottom: 16px;

  :deep(.el-card__body) { padding: 16px 20px; }
  :deep(.el-form-item) { margin-bottom: 0; }
}
</style>
