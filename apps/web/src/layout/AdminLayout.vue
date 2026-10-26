<template>
  <el-container class="admin-layout h-screen">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarCollapsed ? '64px' : '220px'" class="aside">
      <div class="logo-wrap">
        <div class="logo-icon">CR</div>
        <div v-if="!sidebarCollapsed" class="logo-brand">
          <span class="logo-text">创融通运营后台</span>
          <span class="logo-slogan">{{ PLATFORM_INFO.sloganShort }}</span>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        :collapse-transition="false"
        router
        class="side-menu"
        background-color="#001529"
        text-color="rgba(255,255,255,0.75)"
        active-text-color="#2563eb"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主区域 -->
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
          <el-tooltip content="切换角色预览" placement="bottom">
            <el-dropdown @command="handleRoleSwitch">
              <el-button size="small" plain>
                <el-icon><User /></el-icon>
                {{ userStore.nickname || '管理员' }}
                <el-tag size="small" type="danger" class="ml-2">平台管理员</el-tag>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="preview">预览中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-tooltip>
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
  DataBoard, OfficeBuilding, CircleCheck, Promotion, Money, Cpu, Reading, Warning, Document,
  Fold, Expand, User,
} from '@element-plus/icons-vue';
import { useUserStore, useAppStore } from '@/store';
import { PLATFORM_INFO } from '@chuangrongtong/shared';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const appStore = useAppStore();

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed);

const menuItems = [
  { path: '/admin/overview', title: '运营总览', icon: DataBoard },
  { path: '/admin/tenants', title: '租户管理', icon: OfficeBuilding },
  { path: '/admin/projects', title: '项目审核', icon: CircleCheck },
  { path: '/admin/roadshows', title: '路演运营', icon: Promotion },
  { path: '/admin/orders', title: '订单结算', icon: Money },
  { path: '/admin/ai-config', title: 'AI配置中心', icon: Cpu },
  { path: '/admin/knowledge', title: '知识库管理', icon: Reading },
  { path: '/admin/blacklist', title: '风控黑名单', icon: Warning },
  { path: '/admin/logs', title: '操作审计日志', icon: Document },
];

const activeMenu = computed(() => route.path);
const breadcrumbs = computed(() => route.meta.title ? ['平台管理后台', route.meta.title as string] : ['平台管理后台']);

async function handleRoleSwitch(cmd: string) {
  if (cmd === 'preview') {
    router.push('/preview');
  } else if (cmd === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' });
    userStore.logout();
    ElMessage.success('已退出登录');
    router.push('/login');
  }
}
</script>

<style lang="scss" scoped>
.admin-layout {
  .aside {
    background: #001529;
    transition: width 0.2s;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
  .logo-wrap {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    gap: 10px;
    border-bottom: 1px solid rgba(255,255,255,0.08);
    .logo-icon {
      width: 32px; height: 32px;
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      color: white; font-weight: bold;
      border-radius: 8px;
      display: flex; align-items: center; justify-content: center;
      flex-shrink: 0;
    }
    .logo-brand { display: flex; flex-direction: column; line-height: 1.2; min-width: 0; }
    .logo-text { color: white; font-size: 14px; font-weight: 600; }
    .logo-slogan { font-size: 10px; color: rgba(255, 255, 255, 0.5); white-space: nowrap; }
  }
  .side-menu {
    flex: 1;
    border-right: none;
    padding-top: 8px;
  }
  .header {
    background: white;
    border-bottom: 1px solid var(--cft-border);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    .header-left { display: flex; align-items: center; gap: 16px; }
    .collapse-btn {
      font-size: 20px; cursor: pointer; color: var(--cft-text-secondary);
      &:hover { color: var(--cft-primary); }
    }
  }
  .main-content {
    padding: 20px;
    overflow-y: auto;
    background: var(--cft-bg);
  }
  .fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
  .fade-enter-from, .fade-leave-to { opacity: 0; }
  .ml-2 { margin-left: 8px; }
}
</style>
