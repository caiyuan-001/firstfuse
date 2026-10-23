<template>
  <div class="h5-shell">
    <div class="h5-frame">
      <div class="h5-body">
        <router-view v-slot="{ Component }">
          <transition name="h5-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>

      <!-- 底部 Tab 栏 -->
      <nav class="h5-tabbar">
        <div
          v-for="tab in tabs"
          :key="tab.path"
          class="tab-item"
          :class="{ active: isActive(tab.path) }"
          @click="router.push(tab.path)"
        >
          <el-icon :size="21"><component :is="tab.icon" /></el-icon>
          <span class="tab-label">{{ tab.label }}</span>
        </div>
      </nav>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { HomeFilled, Files, MagicStick, OfficeBuilding, User } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();

interface TabItem {
  path: string;
  label: string;
  icon: Component;
}

const tabs: TabItem[] = [
  { path: '/h5/home', label: '首页', icon: HomeFilled },
  { path: '/h5/project', label: '项目', icon: Files },
  { path: '/h5/diagnosis', label: 'AI诊断', icon: MagicStick },
  { path: '/h5/investors', label: '投资人', icon: OfficeBuilding },
  { path: '/h5/profile', label: '我的', icon: User },
];

function isActive(path: string) {
  return route.path === path || route.path.startsWith(`${path}/`);
}
</script>

<style lang="scss" scoped>
.h5-shell {
  min-height: 100vh;
  background: #eef2f7;
  display: flex;
  justify-content: center;
}
.h5-frame {
  width: 100%;
  max-width: 430px;
  height: 100vh;
  background: var(--cft-bg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 0 48px rgba(15, 23, 42, 0.12);
}
.h5-body {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  &::-webkit-scrollbar {
    width: 0;
  }
}
.h5-tabbar {
  flex-shrink: 0;
  display: flex;
  height: 58px;
  padding-bottom: env(safe-area-inset-bottom);
  background: rgba(255, 255, 255, 0.96);
  border-top: 1px solid var(--cft-border);
}
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.18s;
  .tab-label {
    font-size: 11px;
  }
  &:active {
    transform: scale(0.96);
  }
  &.active {
    color: var(--cft-primary);
  }
}
.h5-fade-enter-active,
.h5-fade-leave-active {
  transition: opacity 0.18s;
}
.h5-fade-enter-from,
.h5-fade-leave-to {
  opacity: 0;
}
</style>
