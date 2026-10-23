<template>
  <div class="preview-home">
    <!-- 顶部 Hero -->
    <div class="hero">
      <div class="hero-content">
        <div class="badge">Frontend Demo v0.1.0</div>
        <h1>创融通 FirstFuse</h1>
        <p class="subtitle">{{ PLATFORM_INFO.slogan }}</p>
        <p class="desc">本页面仅用于前端 Demo 预览入口，所有数据均为 Mock 模拟，不连接真实后端。</p>
      </div>
      <div class="hero-login">
        <el-button type="primary" size="large" @click="router.push('/login')">
          <el-icon><User /></el-icon>
          登录系统（推荐）
        </el-button>
        <el-button size="large" @click="doQuickLogin">
          一键快速体验
        </el-button>
        <el-button size="large" @click="router.push('/h5/login')">
          <el-icon><Cellphone /></el-icon>
          创业者端 H5 预览
        </el-button>
      </div>
    </div>

    <!-- 三端入口卡片 -->
    <div class="sections">
      <h2 class="section-title">三端页面预览入口</h2>
      <div class="three-columns">
        <div class="entry-card entrepreneur" @click="enterAs('entrepreneur')">
          <div class="card-header">
            <el-icon :size="32"><User /></el-icon>
            <span class="card-tag">P1-P8</span>
          </div>
          <h3>创业者端</h3>
          <p class="card-desc">工作台 · 项目档案 · AI诊断 · BP打磨 · 找投资人 · 路演</p>
          <div class="page-count">8 个页面</div>
          <div class="card-hint">点击进入预览</div>
        </div>

        <div class="entry-card institution" @click="enterAs('investor')">
          <div class="card-header">
            <el-icon :size="32"><OfficeBuilding /></el-icon>
            <span class="card-tag">I1-I8</span>
          </div>
          <h3>投资机构工作台</h3>
          <p class="card-desc">数据看板 · 项目库 · AI筛选 · 评审简报 · 图谱 · 路演 · 跟踪</p>
          <div class="page-count">8 个页面</div>
          <div class="card-hint">点击进入预览</div>
        </div>

        <div class="entry-card admin" @click="enterAs('platform_admin')">
          <div class="card-header">
            <el-icon :size="32"><Setting /></el-icon>
            <span class="card-tag">A1-A8</span>
          </div>
          <h3>平台管理后台</h3>
          <p class="card-desc">运营总览 · 租户管理 · 项目审核 · AI配置 · 风控 · 结算</p>
          <div class="page-count">8 个页面</div>
          <div class="card-hint">点击进入预览</div>
        </div>
      </div>

      <!-- 公共组件预览入口 -->
      <div class="entry-card cft-components" @click="router.push('/preview/components')">
        <div class="card-header">
          <el-icon :size="32"><Grid /></el-icon>
          <span class="card-tag">Cft*</span>
        </div>
        <h3>全局公共组件预览</h3>
        <p class="card-desc">CftCard · CftPageHeader · CftFilterBar · CftPagination · CftStatusTag · CftStatCard · CftConfirmButton · CftEmpty · CftDrawerDetail</p>
        <div class="page-count">9 个业务组件 · 全局注册可直接用</div>
        <div class="card-hint">点击查看组件用法 + Props 表</div>
      </div>
    </div>
    <div class="sections">
      <h2 class="section-title">完整路由清单</h2>
      <el-collapse>
        <el-collapse-item title="创业者端路由" name="entrepreneur">
          <ul class="route-list">
            <li v-for="r in entrepreneurRoutes" :key="r.path">
              <code>{{ r.path }}</code> <span>— {{ r.name }}</span>
            </li>
          </ul>
        </el-collapse-item>
        <el-collapse-item title="机构工作台路由" name="institution">
          <ul class="route-list">
            <li v-for="r in institutionRoutes" :key="r.path">
              <code>{{ r.path }}</code> <span>— {{ r.name }}</span>
            </li>
          </ul>
        </el-collapse-item>
        <el-collapse-item title="平台管理后台路由" name="admin">
          <ul class="route-list">
            <li v-for="r in adminRoutes" :key="r.path">
              <code>{{ r.path }}</code> <span>— {{ r.name }}</span>
            </li>
          </ul>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 技术说明 -->
    <div class="sections">
      <h2 class="section-title">技术栈</h2>
      <div class="tech-grid">
        <div class="tech-item">Vue 3.5</div>
        <div class="tech-item">Vite 5.4</div>
        <div class="tech-item">TypeScript 5.6</div>
        <div class="tech-item">Pinia 2.2</div>
        <div class="tech-item">Vue Router 4.4</div>
        <div class="tech-item">Element Plus 2.8</div>
        <div class="tech-item">TailwindCSS 3.4</div>
        <div class="tech-item">ECharts 5.5</div>
        <div class="tech-item">Axios 1.7</div>
        <div class="tech-item">VueUse 11.7</div>
        <div class="tech-item">pnpm Workspace</div>
      </div>
    </div>

    <footer class="footer">
      <p>慧联创融 FirstFuse · 演示环境（前端已连接真实后端服务）</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, OfficeBuilding, Setting, Cellphone, Grid } from '@element-plus/icons-vue';
import { useUserStore } from '@/store';
import { DEFAULT_TENANT_ID, PLATFORM_INFO, TenantType, TenantStatus } from '@chuangrongtong/shared';

const router = useRouter();
const userStore = useUserStore();

const entrepreneurRoutes = [
  { path: '/entrepreneur/workspace', name: '工作台首页' },
  { path: '/entrepreneur/project/new', name: '项目档案表单' },
  { path: '/entrepreneur/diagnosis', name: 'AI融资诊断' },
  { path: '/entrepreneur/bp-assistant', name: 'BP打磨助手' },
  { path: '/entrepreneur/investors', name: '找投资人列表' },
  { path: '/entrepreneur/investors/:id', name: '机构详情' },
  { path: '/entrepreneur/roadshows', name: '路演列表' },
  { path: '/entrepreneur/roadshows/:id', name: '路演详情' },
  { path: '/entrepreneur/profile', name: '个人中心' },
];

const institutionRoutes = [
  { path: '/institution/dashboard', name: '工作台首页' },
  { path: '/institution/projects', name: '项目库检索' },
  { path: '/institution/ai-filter', name: 'AI项目筛选' },
  { path: '/institution/review/:id', name: '评审简报' },
  { path: '/institution/graph', name: '企业关系图谱' },
  { path: '/institution/roadshows', name: '路演管理' },
  { path: '/institution/portfolio', name: '被投项目跟踪' },
  { path: '/institution/billing', name: '账单与租户设置' },
];

const adminRoutes = [
  { path: '/admin/overview', name: '运营总览数据看板' },
  { path: '/admin/tenants', name: '租户管理' },
  { path: '/admin/projects', name: '项目审核' },
  { path: '/admin/roadshows', name: '路演运营管理' },
  { path: '/admin/orders', name: '订单结算管理' },
  { path: '/admin/ai-config', name: 'AI配置中心' },
  { path: '/admin/knowledge', name: '知识库内容管理' },
  { path: '/admin/blacklist', name: '风控黑名单管理' },
];

function doQuickLogin() {
  // 默认以机构身份登录演示
  enterAs('investor');
}

function enterAs(role: 'entrepreneur' | 'investor' | 'platform_admin') {
  const roleMap: Record<string, { nickname: string; path: string }> = {
    entrepreneur: { nickname: '张创业', path: '/entrepreneur/workspace' },
    investor: { nickname: '李投资', path: '/institution/dashboard' },
    platform_admin: { nickname: '王运营', path: '/admin/overview' },
  };
  const target = roleMap[role];

  userStore.login({
    token: `mock-token-${role}`,
    user: {
      id: `user-${role}-001`,
      tenantId: DEFAULT_TENANT_ID,
      role: role as never,
      nickname: target.nickname,
      avatar: '',
      realNameVerified: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    },
    tenant: {
      id: DEFAULT_TENANT_ID,
      name: role === 'platform_admin' ? '慧联创融平台' : `${target.nickname}的租户`,
      type: role === 'platform_admin' ? TenantType.PLATFORM : TenantType.INSTITUTION,
      status: TenantStatus.ACTIVE,
      contactName: target.nickname,
      contactPhone: '13800138000',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    },
  });

  ElMessage.success(`已以「${target.nickname}」身份登录`);
  router.push(target.path);
}
</script>

<style lang="scss" scoped>
.preview-home {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding-bottom: 40px;
}
.hero {
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
  color: white; padding: 80px 60px 60px;
  display: flex; justify-content: space-between; align-items: flex-end; gap: 40px;
  .badge {
    display: inline-block; padding: 4px 12px;
    background: rgba(255,255,255,0.2); border-radius: 20px;
    font-size: 12px; margin-bottom: 20px;
  }
  h1 { font-size: 48px; font-weight: 700; margin: 0 0 12px; }
  .subtitle { font-size: 20px; margin: 0 0 8px; opacity: 0.9; }
  .desc { font-size: 14px; opacity: 0.7; margin: 0; }
  .hero-login { display: flex; flex-direction: column; gap: 12px; }
}
.sections {
  max-width: 1200px; margin: 0 auto; padding: 48px 24px 0;
  .section-title {
    font-size: 20px; font-weight: 600; margin: 0 0 24px;
    color: var(--cft-text-primary);
  }
}
.three-columns {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px;
}
.entry-card {
  background: white; border-radius: 12px; padding: 28px;
  border: 1px solid var(--cft-border); cursor: pointer;
  transition: all 0.2s ease;
  &:hover { transform: translateY(-4px); box-shadow: 0 12px 32px rgba(0,0,0,0.08); }
  .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
  .card-tag {
    font-size: 11px; background: var(--cft-bg); padding: 3px 8px; border-radius: 4px;
    color: var(--cft-text-secondary); font-weight: 500;
  }
  h3 { font-size: 18px; margin: 0 0 8px; }
  .card-desc { font-size: 13px; color: var(--cft-text-secondary); margin: 0 0 16px; line-height: 1.6; }
  .page-count { font-size: 12px; color: var(--cft-primary); font-weight: 600; margin-bottom: 4px; }
  .card-hint { font-size: 12px; color: var(--cft-text-muted); }
  &.entrepreneur .card-header :deep(.el-icon) { color: #2563eb; }
  &.institution .card-header :deep(.el-icon) { color: #0d9488; }
  &.admin .card-header :deep(.el-icon) { color: #dc2626; }
  &.cft-components {
    grid-column: 1 / -1;
    border: 1.5px dashed var(--cft-border);
    & .card-header :deep(.el-icon) { color: #7c3aed; }
  }
}
.route-list { list-style: none; padding: 0; margin: 0; li { padding: 6px 0; font-size: 13px; code { background: var(--cft-bg); padding: 2px 6px; border-radius: 4px; margin-right: 8px; color: var(--cft-primary); } span { color: var(--cft-text-secondary); } } }
.tech-grid {
  display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px;
}
.tech-item {
  background: white; border-radius: 8px; padding: 12px 16px;
  border: 1px solid var(--cft-border); text-align: center;
  font-size: 13px; color: var(--cft-text-primary); font-weight: 500;
}
.footer { text-align: center; margin-top: 60px; color: var(--cft-text-muted); font-size: 13px; }
</style>
