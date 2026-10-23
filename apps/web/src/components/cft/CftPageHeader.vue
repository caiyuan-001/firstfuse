<template>
  <div class="cft-page-header">
    <div class="cft-page-header__left">
      <h1 class="cft-page-header__title">{{ title }}</h1>
      <p v-if="subtitle" class="cft-page-header__subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="cft-page-header__actions"><slot name="actions" /></div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  subtitle?: string;
}>();
</script>

<style lang="scss" scoped>
.cft-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  &__title { margin: 0; font-size: 24px; font-weight: 700; color: var(--cft-text-primary, #111827); }
  &__subtitle { margin: 4px 0 0; font-size: 14px; color: var(--cft-text-secondary, #6b7280); }
}
</style>
