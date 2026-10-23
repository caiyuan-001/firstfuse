<template>
  <el-button
    :type="btnType"
    :icon="icon"
    :loading="loading"
    :size="size"
    link
    @click="handle"
  >
    {{ btnText }}
  </el-button>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const props = withDefaults(defineProps<{
  confirmTitle: string;
  confirmContent: string;
  confirmType?: 'warning' | 'error' | 'info' | 'success';
  apiFn: () => Promise<any>;
  successText?: string;
  btnText?: string;
  btnType?: 'primary' | 'danger' | 'success' | 'warning' | 'info';
  icon?: string;
  size?: 'large' | 'default' | 'small';
}>(), {
  confirmType: 'warning',
  btnText: '确定',
  btnType: 'primary',
  size: 'default',
});

const emit = defineEmits<{
  (e: 'success', data: any): void;
}>();

const loading = ref(false);

async function handle() {
  try {
    await ElMessageBox.confirm(props.confirmContent, props.confirmTitle, { type: props.confirmType });
  } catch { return; }

  loading.value = true;
  try {
    const data = await props.apiFn();
    if (props.successText) ElMessage.success(props.successText);
    emit('success', data);
  } catch {
    // apiFn 内部自己会 ElMessage.error，这里不重复弹
  } finally {
    loading.value = false;
  }
}
</script>
