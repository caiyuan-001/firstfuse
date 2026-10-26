import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useUserStore } from '@/store/user';
import { ElMessage } from 'element-plus';

// ========== 路由元信息类型 ==========
declare module 'vue-router' {
  interface RouteMeta {
    title?: string;
    icon?: string;
    requiresAuth?: boolean;
    roles?: string[];  // 允许的角色
    hidden?: boolean;   // 是否在菜单中隐藏
    layout?: 'pc-admin' | 'pc-institution' | 'pc-entrepreneur-preview' | 'blank';
  }
}

// ========== 公共路由 ==========
const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', layout: 'blank', hidden: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/RegisterView.vue'),
    meta: { title: '注册账号', layout: 'blank', hidden: true },
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/login/ForgotPasswordView.vue'),
    meta: { title: '忘记密码', layout: 'blank', hidden: true },
  },
  {
    path: '/',
    redirect: '/preview',
  },
];

// ========== 创业者端 PC 预览路由 ==========
const entrepreneurRoutes: RouteRecordRaw[] = [
  {
    path: '/entrepreneur',
    component: () => import('@/layout/EntrepreneurPreviewLayout.vue'),
    meta: { layout: 'pc-entrepreneur-preview' },
    children: [
      {
        path: '',
        redirect: '/entrepreneur/workspace',
      },
      {
        path: 'workspace',
        name: 'EntrepreneurWorkspace',
        component: () => import('@/views/entrepreneur/WorkspaceView.vue'),
        meta: { title: '工作台首页', requiresAuth: true, roles: ['entrepreneur'] },
      },
      {
        path: 'project/new',
        name: 'ProjectForm',
        component: () => import('@/views/entrepreneur/ProjectFormView.vue'),
        meta: { title: '项目档案', requiresAuth: true, roles: ['entrepreneur'] },
      },
      {
        path: 'diagnosis',
        name: 'Diagnosis',
        component: () => import('@/views/entrepreneur/DiagnosisView.vue'),
        meta: { title: 'AI融资诊断', requiresAuth: true, roles: ['entrepreneur'] },
      },
      {
        path: 'bp-assistant',
        name: 'BPAssistant',
        component: () => import('@/views/entrepreneur/BPAssistantView.vue'),
        meta: { title: 'BP打磨助手', requiresAuth: true, roles: ['entrepreneur'] },
      },
      {
        path: 'investors',
        name: 'FindInvestors',
        component: () => import('@/views/entrepreneur/InvestorListView.vue'),
        meta: { title: '找投资人', requiresAuth: true, roles: ['entrepreneur'] },
      },
      {
        path: 'investors/:id',
        name: 'InvestorDetail',
        component: () => import('@/views/entrepreneur/InvestorDetailView.vue'),
        meta: { title: '机构详情', requiresAuth: true, roles: ['entrepreneur'], hidden: true },
      },
      {
        path: 'roadshows',
        name: 'EntrepreneurRoadshows',
        component: () => import('@/views/entrepreneur/RoadshowListView.vue'),
        meta: { title: '路演', requiresAuth: true, roles: ['entrepreneur'] },
      },
      {
        path: 'roadshows/:id',
        name: 'EntrepreneurRoadshowDetail',
        component: () => import('@/views/entrepreneur/RoadshowDetailView.vue'),
        meta: { title: '路演详情', requiresAuth: true, roles: ['entrepreneur'], hidden: true },
      },
      {
        path: 'profile',
        name: 'EntrepreneurProfile',
        component: () => import('@/views/entrepreneur/ProfileView.vue'),
        meta: { title: '个人中心', requiresAuth: true, roles: ['entrepreneur'] },
      },
    ],
  },
];

// ========== 创业者端 H5 路由（移动端） ==========
const h5Routes: RouteRecordRaw[] = [
  {
    path: '/h5/login',
    name: 'H5Login',
    component: () => import('@/views/h5/H5LoginView.vue'),
    meta: { title: '创业者端登录', layout: 'blank', hidden: true },
  },
  {
    path: '/h5',
    component: () => import('@/layout/H5Layout.vue'),
    meta: { layout: 'blank' },
    children: [
      {
        path: '',
        redirect: '/h5/home',
      },
      {
        path: 'home',
        name: 'H5Home',
        component: () => import('@/views/h5/H5HomeView.vue'),
        meta: { title: '创业者端首页', requiresAuth: true, roles: ['entrepreneur'] },
      },
      {
        path: 'project',
        name: 'H5ProjectForm',
        component: () => import('@/views/h5/H5ProjectView.vue'),
        meta: { title: '项目档案', requiresAuth: true, roles: ['entrepreneur'] },
      },
      {
        path: 'diagnosis',
        name: 'H5Diagnosis',
        component: () => import('@/views/h5/H5DiagnosisView.vue'),
        meta: { title: 'AI融资诊断', requiresAuth: true, roles: ['entrepreneur'] },
      },
      {
        path: 'bp',
        name: 'H5BPAssistant',
        component: () => import('@/views/h5/H5BPView.vue'),
        meta: { title: 'BP打磨助手', requiresAuth: true, roles: ['entrepreneur'] },
      },
      {
        path: 'investors',
        name: 'H5Investors',
        component: () => import('@/views/h5/H5InvestorsView.vue'),
        meta: { title: '找投资人', requiresAuth: true, roles: ['entrepreneur'] },
      },
      {
        path: 'roadshows',
        name: 'H5Roadshows',
        component: () => import('@/views/h5/H5RoadshowsView.vue'),
        meta: { title: '路演', requiresAuth: true, roles: ['entrepreneur'] },
      },
      {
        path: 'profile',
        name: 'H5Profile',
        component: () => import('@/views/h5/H5ProfileView.vue'),
        meta: { title: '个人中心', requiresAuth: true, roles: ['entrepreneur'] },
      },
    ],
  },
];

// ========== 投资机构 PC 工作台路由 ==========
const institutionRoutes: RouteRecordRaw[] = [
  {
    path: '/institution',
    component: () => import('@/layout/InstitutionLayout.vue'),
    meta: { layout: 'pc-institution' },
    children: [
      {
        path: '',
        redirect: '/institution/dashboard',
      },
      {
        path: 'dashboard',
        name: 'InstitutionDashboard',
        component: () => import('@/views/institution/DashboardView.vue'),
        meta: { title: '工作台首页', icon: 'DataAnalysis', requiresAuth: true, roles: ['investor'] },
      },
      {
        path: 'projects',
        name: 'ProjectLibrary',
        component: () => import('@/views/institution/ProjectLibraryView.vue'),
        meta: { title: '项目库', icon: 'Folder', requiresAuth: true, roles: ['investor'] },
      },
      {
        path: 'ai-filter',
        name: 'AIProjectFilter',
        component: () => import('@/views/institution/AIProjectFilterView.vue'),
        meta: { title: 'AI项目筛选', icon: 'MagicStick', requiresAuth: true, roles: ['investor'] },
      },
      {
        path: 'review/:id',
        name: 'ReviewBrief',
        component: () => import('@/views/institution/ReviewBriefView.vue'),
        meta: { title: '评审简报', requiresAuth: true, roles: ['investor'], hidden: true },
      },
      {
        path: 'graph',
        name: 'RelationGraph',
        component: () => import('@/views/institution/RelationGraphView.vue'),
        meta: { title: '企业关系图谱', icon: 'Share', requiresAuth: true, roles: ['investor'] },
      },
      {
        path: 'roadshows',
        name: 'InstitutionRoadshows',
        component: () => import('@/views/institution/RoadshowManageView.vue'),
        meta: { title: '路演管理', icon: 'Calendar', requiresAuth: true, roles: ['investor'] },
      },
      {
        path: 'portfolio',
        name: 'PortfolioTrack',
        component: () => import('@/views/institution/PortfolioView.vue'),
        meta: { title: '被投项目跟踪', icon: 'TrendCharts', requiresAuth: true, roles: ['investor'] },
      },
      {
        path: 'billing',
        name: 'InstitutionBilling',
        component: () => import('@/views/institution/BillingView.vue'),
        meta: { title: '账单与租户设置', icon: 'Wallet', requiresAuth: true, roles: ['investor'] },
      },
    ],
  },
];

// ========== 平台管理后台路由 ==========
const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { layout: 'pc-admin' },
    children: [
      {
        path: '',
        redirect: '/admin/overview',
      },
      {
        path: 'overview',
        name: 'AdminOverview',
        component: () => import('@/views/admin/OverviewView.vue'),
        meta: { title: '运营总览', icon: 'DataBoard', requiresAuth: true, roles: ['platform_admin'] },
      },
      {
        path: 'tenants',
        name: 'TenantManage',
        component: () => import('@/views/admin/TenantManageView.vue'),
        meta: { title: '租户管理', icon: 'OfficeBuilding', requiresAuth: true, roles: ['platform_admin'] },
      },
      {
        path: 'projects',
        name: 'ProjectReview',
        component: () => import('@/views/admin/ProjectReviewView.vue'),
        meta: { title: '项目审核', icon: 'CircleCheck', requiresAuth: true, roles: ['platform_admin'] },
      },
      {
        path: 'roadshows',
        name: 'RoadshowOperation',
        component: () => import('@/views/admin/RoadshowOpView.vue'),
        meta: { title: '路演运营', icon: 'Promotion', requiresAuth: true, roles: ['platform_admin'] },
      },
      {
        path: 'orders',
        name: 'OrderSettlement',
        component: () => import('@/views/admin/OrderView.vue'),
        meta: { title: '订单结算', icon: 'Money', requiresAuth: true, roles: ['platform_admin'] },
      },
      {
        path: 'ai-config',
        name: 'AIConfigCenter',
        component: () => import('@/views/admin/AIConfigView.vue'),
        meta: { title: 'AI配置中心', icon: 'Cpu', requiresAuth: true, roles: ['platform_admin'] },
      },
      {
        path: 'knowledge',
        name: 'KnowledgeManage',
        component: () => import('@/views/admin/KnowledgeView.vue'),
        meta: { title: '知识库管理', icon: 'Reading', requiresAuth: true, roles: ['platform_admin'] },
      },
      {
        path: 'blacklist',
        name: 'BlacklistManage',
        component: () => import('@/views/admin/BlacklistView.vue'),
        meta: { title: '风控黑名单', icon: 'Warning', requiresAuth: true, roles: ['platform_admin'] },
      },
      {
        path: 'logs',
        name: 'AuditLog',
        component: () => import('@/views/admin/AuditLogView.vue'),
        meta: { title: '操作审计日志', icon: 'Document', requiresAuth: true, roles: ['platform_admin'] },
      },
    ],
  },
];

// ========== 演示模式快捷入口 ==========
const previewRoutes: RouteRecordRaw[] = [
  {
    path: '/preview',
    name: 'PreviewHome',
    component: () => import('@/views/preview/PreviewHomeView.vue'),
    meta: { title: 'Demo 预览中心', layout: 'blank' },
  },
  {
    path: '/preview/components',
    name: 'ComponentsPreview',
    component: () => import('@/views/preview/ComponentsView.vue'),
    meta: { title: '全局公共组件预览', layout: 'blank' },
  },
];

// ========== 404 ==========
const notFoundRoute: RouteRecordRaw = {
  path: '/:pathMatch(.*)*',
  name: 'NotFound',
  component: () => import('@/views/NotFound.vue'),
  meta: { title: '页面不存在', layout: 'blank' },
};

const routes: RouteRecordRaw[] = [
  ...publicRoutes,
  ...previewRoutes,
  ...h5Routes,
  ...entrepreneurRoutes,
  ...institutionRoutes,
  ...adminRoutes,
  notFoundRoute,
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

// ========== 全局路由守卫 ==========
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore();

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 创融通 FirstFuse`;
  }

  // 不需要鉴权的页面直接放行
  if (!to.meta.requiresAuth) {
    // 已登录用户访问登录页则跳转对应首页
    if (userStore.isLoggedIn && (to.path === '/login' || to.path === '/h5/login')) {
      const role = userStore.role;
      if (role === 'entrepreneur') {
        return next(to.path === '/h5/login' ? '/h5/home' : '/entrepreneur/workspace');
      }
      if (role === 'investor') return next('/institution/dashboard');
      if (role === 'platform_admin') return next('/admin/overview');
    }
    return next();
  }

  // 未登录 → 跳转登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    return next({ path: '/login', query: { redirect: to.fullPath } });
  }

  // 角色权限检查
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!to.meta.roles.includes(userStore.role)) {
      ElMessage.error('无权访问该页面');
      return next('/preview');
    }
  }

  next();
});

export default router;
