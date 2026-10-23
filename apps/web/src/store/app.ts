import { defineStore } from 'pinia';
import { ref } from 'vue';

/**
 * 应用级状态
 * - 侧边栏折叠
 * - 当前激活的工作模式（预览用）
 */
export const useAppStore = defineStore(
  'app',
  () => {
    const sidebarCollapsed = ref(false);
    const workMode = ref<'pc' | 'mobile'>('pc');

    function toggleSidebar() {
      sidebarCollapsed.value = !sidebarCollapsed.value;
    }

    function setWorkMode(mode: 'pc' | 'mobile') {
      workMode.value = mode;
    }

    return {
      sidebarCollapsed,
      workMode,
      toggleSidebar,
      setWorkMode,
    };
  },
  {
    persist: {
      key: 'cft_app',
      storage: localStorage,
      pick: ['sidebarCollapsed', 'workMode'],
    },
  },
);
