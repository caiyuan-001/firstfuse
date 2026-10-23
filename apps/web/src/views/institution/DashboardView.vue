<template>
  <div class="institution-dashboard">
    <h1 class="page-title">工作台首页</h1>
    <p class="page-desc">欢迎回来，{{ userStore.nickname }} 👋 今天有 {{ pendingCount }} 个项目待评审</p>

    <!-- KPI 卡片 -->
    <el-row :gutter="16" class="kpi-row">
      <el-col :span="6" v-for="k in kpis" :key="k.label">
        <div class="kpi-card" :style="{ borderLeftColor: k.color }">
          <div class="kpi-label">{{ k.label }}</div>
          <div class="kpi-value">{{ k.value }}</div>
          <div class="kpi-trend" :class="k.trend > 0 ? 'up' : 'down'">
            {{ k.trend > 0 ? '↑' : '↓' }} {{ Math.abs(k.trend) }}% 较上周
          </div>
          <div class="kpi-icon" :style="{ background: k.color + '20', color: k.color }">
            <el-icon :size="20"><component :is="k.icon" /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 左侧：图表 + 今日新项目 -->
      <el-col :span="16">
        <div class="cft-card">
          <div class="card-header">
            <h3>近30天项目浏览 & 收藏趋势</h3>
            <el-radio-group v-model="chartMode" size="small">
              <el-radio-button label="view">浏览</el-radio-button>
              <el-radio-button label="favorite">收藏</el-radio-button>
            </el-radio-group>
          </div>
          <v-chart class="trend-chart" :option="trendOption" autoresize />
        </div>

        <div class="cft-card mt-4">
          <div class="card-header">
            <h3>🔥 今日新项目（{{ todayProjects.length }}）</h3>
            <el-button type="primary" size="small" link @click="router.push('/institution/projects')">查看全部 →</el-button>
          </div>
          <el-table :data="todayProjects" size="small">
            <el-table-column prop="title" label="项目名称" min-width="180">
              <template #default="{ row }">
                <el-link type="primary" @click="router.push('/institution/projects')">{{ row.title }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="industry" label="行业" width="100" />
            <el-table-column prop="stage" label="阶段" width="80" />
            <el-table-column prop="amount" label="融资金额" width="100" />
            <el-table-column prop="time" label="发布时间" width="120" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link :type="row.favorited ? 'warning' : 'primary'" @click="toggleFavorite(row)">
                  {{ row.favorited ? '已收藏' : '收藏' }}
                </el-button>
                <el-button size="small" link type="success" @click="generateBrief(row)">生成简报</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- 右侧：待评审 + 快捷操作 -->
      <el-col :span="8">
        <div class="cft-card">
          <div class="card-header">
            <h3>⏳ 待评审（{{ pendingCount }}）</h3>
            <el-tag type="warning" size="small">优先处理</el-tag>
          </div>
          <div class="pending-list">
            <div v-for="p in pendingList" :key="p.id" class="pending-item">
              <div class="p-title">{{ p.title }}</div>
              <div class="p-meta">{{ p.industry }} · {{ p.stage }}</div>
              <div class="p-footer">
                <el-tag size="small" :type="urgencyType(p.urgency)">{{ p.urgency }}</el-tag>
                <el-button size="small" type="primary" link @click="generateBrief(p)">立即评审 →</el-button>
              </div>
            </div>
          </div>
        </div>

        <div class="cft-card mt-4">
          <div class="card-header">
            <h3>⚡ 快捷操作</h3>
          </div>
          <div class="quick-grid">
            <div class="quick-item" @click="router.push('/institution/ai-filter')">
              <div class="qi-icon">🤖</div>
              <div class="qi-label">AI 智能筛选</div>
            </div>
            <div class="quick-item" @click="router.push('/institution/graph')">
              <div class="qi-icon">🕸️</div>
              <div class="qi-label">企业图谱</div>
            </div>
            <div class="quick-item" @click="router.push('/institution/roadshows')">
              <div class="qi-icon">🎤</div>
              <div class="qi-label">创建路演</div>
            </div>
            <div class="quick-item" @click="router.push('/institution/portfolio')">
              <div class="qi-icon">📊</div>
              <div class="qi-label">投后跟踪</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { TagType } from '@chuangrongtong/shared';
import { DataAnalysis, Folder, Wallet, TrendCharts } from '@element-plus/icons-vue';
import { useUserStore } from '@/store';

const router = useRouter();
const userStore = useUserStore();
const chartMode = ref('view');
const pendingCount = ref(3);

const kpis = [
  { label: '浏览项目总数', value: '1,286', trend: 12.5, color: '#2563eb', icon: Folder },
  { label: '已收藏项目', value: '86', trend: 8.3, color: '#22c55e', icon: TrendCharts },
  { label: '生成评审简报', value: '32', trend: 15.2, color: '#f59e0b', icon: DataAnalysis },
  { label: '本周活跃租户', value: '12', trend: -2.1, color: '#8b5cf6', icon: Wallet },
];

const todayProjects = ref([
  { id: '1', title: '下一代 AI Agent 协作平台', industry: '人工智能', stage: 'Pre-Seed', amount: '300万', time: '2小时前', favorited: false },
  { id: '2', title: '固态电池关键材料技术', industry: '新能源', stage: '种子轮', amount: '1200万', time: '4小时前', favorited: false },
  { id: '3', title: '智能影像诊断 SaaS', industry: '医疗健康', stage: 'A轮', amount: '5000万', time: '6小时前', favorited: true },
  { id: '4', title: '工业机器人核心控制器', industry: '机器人', stage: 'Pre-Seed', amount: '500万', time: '8小时前', favorited: false },
]);

const pendingList = [
  { id: '1', title: '下一代 AI Agent 协作平台', industry: '人工智能', stage: 'Pre-Seed', urgency: '紧急' },
  { id: '2', title: '智能影像诊断 SaaS', industry: '医疗健康', stage: 'A轮', urgency: '一般' },
  { id: '3', title: '边缘计算芯片设计', industry: '半导体', stage: '种子轮', urgency: '低' },
];

// 静态演示数据（避免每次渲染随机跳动）
const trendData = {
  view: [32, 45, 38, 52, 49, 61, 58, 72, 65, 78, 70, 83, 76, 90, 84, 95, 88, 102, 96, 108, 99, 115, 106, 120, 112, 126, 118, 132, 124, 138],
  favorite: [5, 8, 6, 9, 7, 11, 9, 13, 10, 14, 12, 16, 13, 17, 15, 19, 16, 20, 18, 22, 19, 24, 21, 25, 23, 27, 24, 28, 26, 30],
};

const trendOption = computed(() => {
  const isView = chartMode.value === 'view';
  const color = isView ? '#2563eb' : '#22c55e';
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Array.from({ length: 30 }, (_, i) => `${i + 1}日`), boundaryGap: false },
    yAxis: { type: 'value' },
    series: [{
      name: isView ? '项目浏览量' : '收藏数',
      type: 'line', data: isView ? trendData.view : trendData.favorite, smooth: true,
      itemStyle: { color },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: `${color}4d` }, { offset: 1, color: `${color}00` }] } },
    }],
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
  };
});

function toggleFavorite(row: any) {
  row.favorited = !row.favorited;
  ElMessage.success(row.favorited ? `已收藏「${row.title}」` : '已取消收藏');
}

function generateBrief(row: any) {
  ElMessage.success(`正在为「${row.title}」生成 AI 评审简报...`);
  setTimeout(() => router.push(`/institution/review/${row.id}`), 600);
}

function urgencyType(u: string): TagType {
  const map: Record<string, TagType> = { '紧急': 'danger', '一般': 'warning', '低': 'info' };
  return map[u] || 'info';
}
</script>

<style lang="scss" scoped>
.institution-dashboard {
  .page-title { margin: 0 0 4px; font-size: 24px; }
  .page-desc { margin: 0 0 24px; color: var(--cft-text-secondary); font-size: 14px; }
  .kpi-row { margin-bottom: 16px; }
  .kpi-card {
    background: white; border: 1px solid var(--cft-border); border-radius: 12px; padding: 20px;
    position: relative; overflow: hidden; border-left: 4px solid;
  }
  .kpi-label { font-size: 13px; color: var(--cft-text-muted); }
  .kpi-value { font-size: 28px; font-weight: 700; margin: 4px 0; color: var(--cft-text-primary); }
  .kpi-trend { font-size: 12px; &.up { color: var(--cft-success); } &.down { color: var(--cft-danger); } }
  .kpi-icon { position: absolute; right: 16px; top: 50%; transform: translateY(-50%); width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
  .trend-chart { width: 100%; height: 320px; }
  .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h3 { margin: 0; font-size: 16px; } }
  .pending-list { .pending-item { padding: 14px; background: var(--cft-bg); border-radius: 8px; margin-bottom: 10px; .p-title { font-weight: 500; margin-bottom: 4px; } .p-meta { font-size: 12px; color: var(--cft-text-muted); margin-bottom: 8px; } .p-footer { display: flex; justify-content: space-between; align-items: center; } } }
  .quick-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .quick-item { background: var(--cft-bg); border-radius: 10px; padding: 16px; text-align: center; cursor: pointer; transition: all 0.2s; &:hover { background: var(--cft-primary); color: white; .qi-label { color: white; } } .qi-icon { font-size: 24px; margin-bottom: 6px; } .qi-label { font-size: 12px; color: var(--cft-text-secondary); font-weight: 500; } }
  .mt-4 { margin-top: 16px; }
}
</style>
