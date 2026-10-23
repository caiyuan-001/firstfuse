<template>
  <div class="page-placeholder">
    <div class="page-header">
      <h1>{{ pageTitle }}</h1>
      <p v-if="pageDesc" class="desc">{{ pageDesc }}</p>
      <el-tag type="info" size="small">Step4 将完整实现此页面</el-tag>
    </div>
    <div class="placeholder-content">
      <div class="mock-banner">
        <el-icon :size="48" color="#2563eb"><component :is="icon" /></el-icon>
        <h3>{{ pageTitle }}</h3>
        <p>本页面为 Demo 占位，完整 UI、交互、Mock 数据将在 Step4 实现。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h } from 'vue';
import { useRoute } from 'vue-router';
import {
  DataAnalysis, Folder, MagicStick, Share, Calendar, TrendCharts, Wallet,
  DataBoard, OfficeBuilding, CircleCheck, Promotion, Money, Cpu, Reading, Warning,
  User, Upload, Monitor, ChatDotRound, Search, Document, Setting,
} from '@element-plus/icons-vue';

const route = useRoute();

const iconMap: Record<string, unknown> = {
  // 创业者端
  workspace: User,
  project: Upload,
  diagnosis: Monitor,
  bp: ChatDotRound,
  investor: Search,
  roadshow: Calendar,
  profile: User,
  // 机构端
  dashboard: DataAnalysis,
  library: Folder,
  ai: MagicStick,
  review: Document,
  graph: Share,
  portfolio: TrendCharts,
  billing: Wallet,
  // 管理后台
  overview: DataBoard,
  tenant: OfficeBuilding,
  check: CircleCheck,
  op: Promotion,
  order: Money,
  aiconfig: Cpu,
  knowledge: Reading,
  blacklist: Warning,
};

const pageTitle = computed(() => (route.meta.title as string) || '页面占位');
const pageDesc = computed(() => {
  const descs: Record<string, string> = {
    workspace: '创业者工作台首页 - 我的项目、融资进度、待办事项',
    project: '项目档案表单 - BP上传、AI预填表单、项目信息维护',
    diagnosis: 'AI融资诊断 - 智能问卷、融资健康检查、雷达图报告',
    bp: 'BP打磨助手 - 对话式AI，打磨您的商业计划书',
    investor: '找投资人列表 - 机构检索、筛选、投递',
    roadshow: '路演列表与详情 - 浏览、报名、在线路演',
    profile: '个人中心 - 我的订单、消息、知识库',
    dashboard: '机构工作台首页 - 数据指标、今日新项目、待评审',
    library: '项目库检索 - 多维度筛选、收藏、详情抽屉',
    ai: 'AI项目筛选 - 输入投资策略，AI匹配推荐',
    review: '产业评审简报详情 - AI生成的结构化评审报告',
    graph: '企业关系图谱 - 前端占位可视化组件',
    portfolio: '被投项目跟踪 - 投后管理、里程碑、数据看板',
    billing: '机构账单与租户设置 - 会员管理、套餐、发票',
    overview: '运营总览数据看板 - 平台核心指标、趋势、分布',
    tenant: '租户管理 - 租户列表、开通/冻结、套餐',
    check: '项目审核工作台 - 待审项目列表、评审、状态流转',
    op: '路演运营管理 - 全平台路演运营活动管理',
    order: '订单结算管理 - 全平台订单流水结算',
    aiconfig: 'AI配置中心 - Prompt模板、模型路由、用量统计',
    knowledge: '知识库内容管理 - 全平台文章、问答库',
    blacklist: '风控黑名单管理 - 用户/租户/项目黑名单',
  };
  const match = Object.keys(descs).find(k => route.path.includes(k));
  return match ? descs[match] : '';
});

const icon = computed(() => {
  for (const [key, val] of Object.entries(iconMap)) {
    if (route.path.includes(key)) return val;
  }
  return Document;
});
</script>

<style lang="scss" scoped>
.page-placeholder {
  .page-header { margin-bottom: 24px; h1 { margin: 0 0 4px; font-size: 24px; color: var(--cft-text-primary); } .desc { margin: 0 0 8px; color: var(--cft-text-secondary); font-size: 14px; } }
  .placeholder-content {
    background: white; border-radius: 12px; border: 1px dashed var(--cft-border);
    padding: 80px 40px; text-align: center;
  }
  .mock-banner {
    display: flex; flex-direction: column; align-items: center; gap: 12px;
    h3 { margin: 0; font-size: 18px; }
    p { margin: 0; color: var(--cft-text-muted); font-size: 13px; }
  }
}
</style>
