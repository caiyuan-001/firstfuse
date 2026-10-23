<template>
  <div class="admin-overview">
    <div class="page-header">
      <h1>运营总览</h1>
      <p>平台实时运营数据看板 — 创融通 FirstFuse</p>
    </div>

    <!-- KPI -->
    <el-row :gutter="16" class="kpi-row" v-loading="loading">
      <el-col :span="4" v-for="k in data.kpis" :key="k.label">
        <div class="kpi-card" :style="{ background: k.bg }">
          <div class="kpi-icon"><el-icon :size="24"><component :is="icons[k.icon]" /></el-icon></div>
          <div class="kpi-info">
            <div class="kpi-value">{{ k.value }}</div>
            <div class="kpi-label">{{ k.label }}</div>
            <div class="kpi-trend" :class="k.trend > 0 ? 'up' : 'down'">{{ k.trend > 0 ? '↑' : '↓' }} {{ Math.abs(k.trend) }}%</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="14">
        <div class="cft-card">
          <div class="card-header">
            <h3>📈 平台核心指标趋势（近30天）</h3>
          </div>
          <v-chart class="main-chart" :option="trendOption" autoresize />
        </div>

        <div class="cft-card mt-4">
          <div class="card-header">
            <h3>🏢 租户分布（按行业）</h3>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <v-chart class="pie-chart" :option="pieOption" autoresize />
            </el-col>
            <el-col :span="12">
              <el-table :data="data.tenantByIndustry" size="small">
                <el-table-column prop="industry" label="行业" />
                <el-table-column prop="count" label="租户数" width="100" />
                <el-table-column prop="newThisMonth" label="本月新增" width="100" />
              </el-table>
            </el-col>
          </el-row>
        </div>
      </el-col>

      <el-col :span="10">
        <div class="cft-card">
          <div class="card-header">
            <h3>🔔 今日待办</h3>
            <el-tag type="danger" size="small">{{ data.pendingCount }} 项</el-tag>
          </div>
          <div class="todo-list">
            <div v-for="t in data.todos" :key="t.id" class="todo-item">
              <div class="t-icon" :class="t.level"><el-icon><component :is="icons[t.icon]" /></el-icon></div>
              <div class="t-content">
                <div class="t-title">{{ t.title }}</div>
                <div class="t-count">{{ t.count }} 项</div>
              </div>
              <el-button size="small" link type="primary" @click="router.push(t.path)">处理 →</el-button>
            </div>
          </div>
        </div>

        <div class="cft-card mt-4">
          <div class="card-header">
            <h3>⭐ 热门项目 TOP5</h3>
          </div>
          <div class="top5-list">
            <div v-for="(p, i) in data.topProjects" :key="p.id" class="top-item">
              <div class="rank" :class="{ top: i < 3 }">{{ i + 1 }}</div>
              <div class="p-info">
                <div class="p-name">{{ p.title }}</div>
                <div class="p-meta">{{ p.industry }} · {{ p.stage }}</div>
              </div>
              <div class="p-views">{{ p.views }} 👁️</div>
            </div>
          </div>
        </div>

        <div class="cft-card mt-4">
          <div class="card-header">
            <h3>📊 实时消息流</h3>
          </div>
          <div class="feed-stream">
            <div v-for="m in data.feedStream" :key="m.id" class="feed-item">
              <div class="f-dot" :class="m.type"></div>
              <div class="f-content">
                <span class="f-text">{{ m.text }}</span>
                <span class="f-time">{{ m.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { UserFilled, OfficeBuilding, Folder, Money, Warning, Document, Bell } from '@element-plus/icons-vue';
import { httpGet } from '@/utils/request';

const router = useRouter();
const loading = ref(false);

const icons: Record<string, any> = { UserFilled, OfficeBuilding, Folder, Money, Warning, Document, Bell };

const data = ref<any>({
  kpis: [], trend: null, tenantByIndustry: [], pie: null, todos: [], topProjects: [], feedStream: [], pendingCount: 0,
});

const trendOption = ref<any>({});
const pieOption = ref<any>({});

async function loadData() {
  loading.value = true;
  try {
    const res = await httpGet<any>('/api/v1/admin/stats/overview');
    const d = res.data || {};
    data.value = d;
    trendOption.value = d.trend;
    pieOption.value = d.pie;
  } finally { loading.value = false; }
}

onMounted(() => loadData());
</script>

<style lang="scss" scoped>
.admin-overview {
  .page-header { margin-bottom: 20px; h1 { margin: 0 0 4px; font-size: 24px; } p { margin: 0; color: var(--cft-text-secondary); font-size: 14px; } }
  .kpi-row { margin-bottom: 16px; }
  .kpi-card {
    border-radius: 12px; padding: 20px;
    display: flex; gap: 14px; align-items: center;
    border: 1px solid var(--cft-border);
  }
  .kpi-icon { width: 48px; height: 48px; border-radius: 12px; background: white; display: flex; align-items: center; justify-content: center; color: var(--cft-primary); }
  .kpi-info { flex: 1; }
  .kpi-value { font-size: 24px; font-weight: 700; color: var(--cft-text-primary); }
  .kpi-label { font-size: 12px; color: var(--cft-text-muted); margin-top: 2px; }
  .kpi-trend { font-size: 11px; margin-top: 4px; &.up { color: var(--cft-success); } &.down { color: var(--cft-danger); } }
  .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h3 { margin: 0; font-size: 16px; } }
  .main-chart { width: 100%; height: 320px; }
  .pie-chart { width: 100%; height: 260px; }
  .todo-list { display: flex; flex-direction: column; gap: 10px; }
  .todo-item { display: flex; align-items: center; gap: 12px; padding: 10px 12px; background: var(--cft-bg); border-radius: 8px; .t-icon { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; &.danger { background: #fee2e2; color: #ef4444; } &.warning { background: #fef3c7; color: #f59e0b; } &.info { background: #dbeafe; color: #2563eb; } } .t-content { flex: 1; .t-title { font-size: 14px; font-weight: 500; } .t-count { font-size: 12px; color: var(--cft-text-muted); } } }
  .top5-list { .top-item { display: flex; gap: 12px; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--cft-border); &:last-child { border: none; } .rank { width: 24px; height: 24px; border-radius: 6px; background: var(--cft-bg); display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 12px; color: var(--cft-text-muted); &.top { background: linear-gradient(135deg, #f59e0b, #f97316); color: white; } } .p-info { flex: 1; .p-name { font-size: 13px; font-weight: 500; } .p-meta { font-size: 11px; color: var(--cft-text-muted); margin-top: 2px; } } .p-views { font-size: 12px; color: var(--cft-text-muted); } } }
  .feed-stream { max-height: 260px; overflow-y: auto; }
  .feed-item { display: flex; gap: 10px; padding: 6px 0; .f-dot { width: 8px; height: 8px; border-radius: 50%; margin-top: 6px; flex-shrink: 0; &.success { background: var(--cft-success); } &.warning { background: var(--cft-warning); } &.info { background: var(--cft-primary); } } .f-content { flex: 1; .f-text { font-size: 13px; } .f-time { font-size: 11px; color: var(--cft-text-muted); margin-left: 8px; } } }
  .mt-4 { margin-top: 16px; }
}
</style>
