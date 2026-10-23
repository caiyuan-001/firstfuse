<template>
  <div v-if="total > 0" class="cft-pagination">
    <el-pagination
      :current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      :page-sizes="pageSizes"
      layout="total, sizes, prev, pager, next"
      @current-change="handleCurrent"
      @size-change="handleSize"
    />
  </div>
</template>

<script setup lang="ts">
import { watch } from 'vue';

const props = withDefaults(defineProps<{
  total: number;
  currentPage: number;
  pageSize: number;
  pageSizes?: number[];
}>(), {
  pageSizes: () => [10, 20, 50],
});

const emit = defineEmits<{
  (e: 'update:currentPage', v: number): void;
  (e: 'update:pageSize', v: number): void;
  (e: 'change', page: number, size: number): void;
}>();

function handleCurrent(page: number) {
  emit('update:currentPage', page);
  emit('change', page, props.pageSize);
}
function handleSize(size: number) {
  emit('update:pageSize', size);
  emit('change', props.currentPage, size);
}
</script>

<style lang="scss" scoped>
.cft-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
