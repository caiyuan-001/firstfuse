<template>
  <el-container class="institution-layout h-screen">
    <el-aside :width="sidebarCollapsed ? '64px' : '220px'" class="aside">
      <div class="logo-wrap">
        <div class="logo-icon">🏛️</div>
        <div v-if="!sidebarCollapsed" class="logo-brand">
          <span class="logo-text">机构工作台</span>
          <span class="logo-slogan">{{ PLATFORM_INFO.sloganShort }}</span>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        :collapse-transition="false"
        router
        class="side-menu"
        background-color="#ffffff"
        text-color="#374151"
        active-text-color="#2563eb"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar()">
            <Fold v-if="!sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="(b, i) in breadcrumbs" :key="i">{{ b }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-button size="small" @click="router.push('/preview')">
            <el-icon><SwitchButton /></el-icon>
            预览中心
          </el-button>
          <el-dropdown @command="handleCommand">
            <el-button size="small" plain class="ml-2">
              <el-avatar :size="24" class="mr-1">投</el-avatar>
              {{ userStore.nickname || '机构用户' }}
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import {
  DataAnalysis, Folder, MagicStick, Share, Calendar, TrendCharts, Wallet,
  Fold, Expand, SwitchButton,
} from '@element-plus/icons-vue';
import { useUserStore, useAppStore } from '@/store';
import { PLATFORM_INFO } from '@chuangrongtong/shared';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const appStore = useAppStore();

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed);

const menuItems = [
  { path: '/institution/dashboard', title: '工作台首页', icon: DataAnalysis },
  { path: '/institution/projects', title: '项目库', icon: Folder },
  { path: '/institution/ai-filter', title: 'AI项目筛选', icon: MagicStick },
  { path: '/institution/graph', title: '企业关系图谱', icon: Share },
  { path: '/institution/roadshows', title: '路演管理', icon: Calendar },
  { path: '/institution/portfolio', title: '被投项目跟踪', icon: TrendCharts },
  { path: '/institution/billing', title: '账单与租户设置', icon: Wallet },
];

const activeMenu = computed(() => route.path);
const breadcrumbs = computed(() => route.meta.title ? ['机构工作台', route.meta.title as string] : ['机构工作台']);

async function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' });
    userStore.logout();
    ElMessage.success('已退出登录');
    router.push('/login');
  }
}
</script>

<style lang="scss" scoped>
.institution-layout {
  .aside {
    background: white;
    border-right: 1px solid var(--cft-border);
    transition: width 0.2s;
    display: flex; flex-direction: column;
  }
  .logo-wrap {
    height: 60px;
    display: flex; align-items: center; padding: 0 16px; gap: 10px;
    border-bottom: 1px solid var(--cft-border);
    .logo-icon { font-size: 24px; }
    .logo-brand { display: flex; flex-direction: column; line-height: 1.2; min-width: 0; }
    .logo-text { font-size: 14px; font-weight: 600; color: var(--cft-text-primary); }
    .logo-slogan { font-size: 10px; color: var(--cft-text-muted); white-space: nowrap; }
  }
  .side-menu { flex: 1; border-right: none; padding-top: 8px; }
  .header {
    background: white; border-bottom: 1px solid var(--cft-border);
    display: flex; align-items: center; justify-content: space-between; padding: 0 20px;
    .header-left { display: flex; align-items: center; gap: 16px; }
    .collapse-btn { font-size: 20px; cursor: pointer; color: var(--cft-text-secondary); &:hover { color: var(--cft-primary); } }
  }
  .main-content { padding: 20px; overflow-y: auto; background: var(--cft-bg); }
  .fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
  .fade-enter-from, .fade-leave-to { opacity: 0; }
  .ml-2 { margin-left: 8px; }
  .mr-1 { margin-right: 4px; }
}
</style>
