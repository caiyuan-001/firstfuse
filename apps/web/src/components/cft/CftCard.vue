<template>
  <div class="cft-card" :class="{ 'cft-card--bordered': bordered, 'cft-card--shadow-hover': shadow === 'hover', 'cft-card--shadow-none': shadow === 'never' }">
    <div v-if="title || $slots.header" class="cft-card__header">
      <h3 v-if="title">{{ title }}</h3>
      <slot name="header" />
    </div>
    <div class="cft-card__body"><slot /></div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title?: string;
  bordered?: boolean;
  shadow?: 'always' | 'hover' | 'never';
}>(), {
  bordered: true,
  shadow: 'always',
});
</script>

<style lang="scss" scoped>
.cft-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.2s;

  &--bordered { border: 1px solid var(--cft-border, #e5e7eb); }
  &--shadow-hover:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
  &--shadow-none { box-shadow: none; }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 { margin: 0; font-size: 16px; font-weight: 600; }
  }
}
</style>
