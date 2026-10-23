<template>
  <el-drawer v-model="visible" :title="title" :size="size" :direction="direction">
    <slot />
    <template v-if="$slots.footer" #footer><slot name="footer" /></template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  modelValue: boolean;
  title?: string;
  size?: string;
  direction?: 'rtl' | 'ltr' | 'ttb' | 'btt';
}>(), {
  title: '详情',
  size: '600px',
  direction: 'rtl',
});

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void;
}>();

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});
</script>
