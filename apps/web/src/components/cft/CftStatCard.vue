<template>
  <div class="cft-stat-card" :style="{ '--cft-sc-color': color }">
    <div v-if="icon" class="cft-stat-card__icon">
      <el-icon :size="20"><component :is="icon" /></el-icon>
    </div>
    <div class="cft-stat-card__info">
      <div class="cft-stat-card__value">{{ value }}</div>
      <div class="cft-stat-card__label">{{ label }}</div>
    </div>
    <div v-if="trend !== undefined" class="cft-stat-card__trend" :class="trend >= 0 ? 'up' : 'down'">
      {{ trend >= 0 ? '↑' : '↓' }} {{ Math.abs(trend) }}%
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  label: string;
  value: string | number;
  icon?: string;
  color?: string;
  trend?: number;
}>(), {
  color: '#2563eb',
});
</script>

<style lang="scss" scoped>
.cft-stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  border: 1px solid var(--cft-border, #e5e7eb);
  border-top: 3px solid var(--cft-sc-color);
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;

  &__icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: rgba(37, 99, 235, 0.1);
    color: var(--cft-sc-color);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__info { flex: 1; min-width: 0; }
  &__value { font-size: 22px; font-weight: 700; color: var(--cft-text-primary, #111827); }
  &__label { font-size: 13px; color: var(--cft-text-muted, #9ca3af); margin-top: 2px; }

  &__trend {
    font-size: 11px;
    position: absolute;
    top: 12px;
    right: 16px;
    &.up { color: #22c55e; }
    &.down { color: #ef4444; }
  }
}
</style>
