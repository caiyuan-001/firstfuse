<template>
  <div class="portfolio">
    <div class="page-header">
      <h1>被投项目跟踪</h1>
      <p>投后管理：跟踪被投企业的关键指标、里程碑和退出状态</p>
    </div>

    <!-- 概览卡片 -->
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6" v-for="o in overview" :key="o.label">
        <div class="overview-card">
          <div class="o-icon" :style="{ background: o.color + '20', color: o.color }">
            <el-icon :size="22"><component :is="o.icon" /></el-icon>
          </div>
          <div class="o-content">
            <div class="o-label">{{ o.label }}</div>
            <div class="o-value">{{ o.value }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-tabs v-model="activeStatus">
      <el-tab-pane :label="`全部（${portfolioList.length}）`" name="all" />
      <el-tab-pane :label="`正常运营（${countBy('normal')}）`" name="normal" />
      <el-tab-pane :label="`退出中（${countBy('exiting')}）`" name="exiting" />
      <el-tab-pane :label="`已退出（${countBy('exited')}）`" name="exited" />
    </el-tabs>

    <div class="portfolio-list">
      <el-empty v-if="visibleList.length === 0" description="该状态下暂无被投项目" />
      <div v-for="p in visibleList" :key="p.id" class="p-card" @click="openDetail(p)">
        <div class="p-header">
          <div class="p-title-row">
            <h3>{{ p.projectName }}</h3>
            <el-tag :type="statusType(p.status)" size="small">{{ statusLabel(p.status) }}</el-tag>
          </div>
          <div class="p-meta">
            <span>投资金额：<strong>¥{{ formatWan(p.amount) }}</strong></span>
            <span>持股比例：<strong>{{ p.equityPercent }}%</strong></span>
            <span>投后估值：<strong>¥{{ formatWan(p.postMoney) }}</strong></span>
          </div>
        </div>
        <div class="p-metrics">
          <div class="metric">
            <span class="metric-label">最新营收</span>
            <span class="metric-value positive">¥{{ formatWan(p.revenue) }} <small>+{{ p.revenueGrowth }}%</small></span>
          </div>
          <div class="metric">
            <span class="metric-label">最新利润</span>
            <span class="metric-value positive">¥{{ formatWan(p.profit) }}</span>
          </div>
          <div class="metric">
            <span class="metric-label">团队规模</span>
            <span class="metric-value">{{ p.teamSize }}人</span>
          </div>
          <div class="metric">
            <span class="metric-label">最近更新</span>
            <span class="metric-value">{{ p.lastUpdate }}</span>
          </div>
        </div>
        <div class="p-footer">
          <div class="milestone">
            <span class="ms-label">里程碑：</span>
            <el-tag v-for="(m, i) in p.milestones" :key="i" size="small" :type="m.achieved ? 'success' : 'info'" effect="plain">
              {{ m.achieved ? '✓ ' : '' }}{{ m.name }}
            </el-tag>
          </div>
          <el-button size="small" type="primary" link>查看详情 →</el-button>
        </div>
      </div>
    </div>

    <!-- 项目详情抽屉 -->
    <el-drawer v-model="drawerVisible" :title="current?.projectName" size="600px" direction="rtl">
      <div v-if="current">
        <div class="drawer-head">
          <el-tag :type="statusType(current.status)" size="small">{{ statusLabel(current.status) }}</el-tag>
          <span class="drawer-sub">最近更新：{{ current.lastUpdate }}</span>
        </div>

        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="投资金额">¥{{ formatWan(current.amount) }}</el-descriptions-item>
          <el-descriptions-item label="持股比例">{{ current.equityPercent }}%</el-descriptions-item>
          <el-descriptions-item label="投后估值">¥{{ formatWan(current.postMoney) }}</el-descriptions-item>
          <el-descriptions-item label="团队规模">{{ current.teamSize }}人</el-descriptions-item>
        </el-descriptions>

        <h4 class="drawer-section-title">📈 近 6 个季度营收趋势（万元）</h4>
        <v-chart class="revenue-chart" :option="revenueOption" autoresize />

        <h4 class="drawer-section-title">🚩 里程碑进展</h4>
        <el-timeline class="ms-timeline">
          <el-timeline-item
            v-for="(m, i) in current.milestones"
            :key="i"
            :type="m.achieved ? 'success' : 'info'"
            :hollow="!m.achieved"
            :timestamp="m.achieved ? '已达成' : '待达成'"
          >
            {{ m.name }}
          </el-timeline-item>
        </el-timeline>

        <h4 class="drawer-section-title">📝 投后备注</h4>
        <el-input v-model="noteDraft" type="textarea" :rows="3" placeholder="记录本次沟通、经营变化、风险提示..." />
        <div class="drawer-actions">
          <el-button type="primary" @click="saveNote">保存备注</el-button>
          <el-button @click="remindFollow">设置跟进提醒</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import type { TagType } from '@chuangrongtong/shared';
import { Folder, TrendCharts, CircleCheck, SwitchButton } from '@element-plus/icons-vue';

const activeStatus = ref('all');
const drawerVisible = ref(false);
const current = ref<any>(null);
const noteDraft = ref('');

const overview = [
  { label: '被投项目总数', value: '23', color: '#2563eb', icon: Folder },
  { label: '总投资金额', value: '¥18.6亿', color: '#22c55e', icon: TrendCharts },
  { label: '正常运营', value: '19', color: '#2563eb', icon: CircleCheck },
  { label: '已退出', value: '4', color: '#f59e0b', icon: SwitchButton },
];

const portfolioList = [
  { id: '1', projectName: '字节跳动', status: 'normal', amount: 80000, equityPercent: 12.5, postMoney: 640000, revenue: 120000, revenueGrowth: 45, profit: 15000, teamSize: 15000, lastUpdate: '2026-10-10', revenueTrend: [52000, 68000, 81000, 95000, 108000, 120000], milestones: [{ name: 'D轮融资', achieved: true }, { name: '营收百亿', achieved: true }, { name: '国际化', achieved: false }] },
  { id: '2', projectName: '美团点评', status: 'normal', amount: 50000, equityPercent: 15, postMoney: 330000, revenue: 85000, revenueGrowth: 28, profit: 8000, teamSize: 8000, lastUpdate: '2026-10-08', revenueTrend: [48000, 55000, 62000, 71000, 79000, 85000], milestones: [{ name: 'C轮融资', achieved: true }, { name: '全国覆盖', achieved: true }, { name: '港股上市', achieved: true }] },
  { id: '3', projectName: '拼多多', status: 'exiting', amount: 30000, equityPercent: 20, postMoney: 150000, revenue: 60000, revenueGrowth: 35, profit: 12000, teamSize: 3000, lastUpdate: '2026-10-05', revenueTrend: [28000, 35000, 42000, 49000, 55000, 60000], milestones: [{ name: 'B轮融资', achieved: true }, { name: '美股上市', achieved: true }, { name: '退出方案', achieved: false }] },
  { id: '4', projectName: '瓜子二手车', status: 'exited', amount: 12000, equityPercent: 0, postMoney: 80000, revenue: 45000, revenueGrowth: 12, profit: 3200, teamSize: 2500, lastUpdate: '2026-06-30', revenueTrend: [22000, 26000, 31000, 36000, 41000, 45000], milestones: [{ name: 'B轮融资', achieved: true }, { name: '盈利转正', achieved: true }, { name: '并购退出（3.2x）', achieved: true }] },
];

const visibleList = computed(() =>
  activeStatus.value === 'all' ? portfolioList : portfolioList.filter((p) => p.status === activeStatus.value),
);

const quarters = ['25Q2', '25Q3', '25Q4', '26Q1', '26Q2', '26Q3'];
const revenueOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: quarters, boundaryGap: false },
  yAxis: { type: 'value' },
  series: [{
    type: 'line', data: current.value?.revenueTrend || [], smooth: true,
    itemStyle: { color: '#2563eb' },
    areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(37,99,235,0.25)' }, { offset: 1, color: 'rgba(37,99,235,0)' }] } },
  }],
  grid: { left: 60, right: 20, top: 20, bottom: 30 },
}));

function countBy(s: string) {
  return portfolioList.filter((p) => p.status === s).length;
}

function formatWan(v: number) {
  return v >= 10000 ? `${(v / 10000).toFixed(1)}亿` : `${v}万`;
}

function statusType(s: string): TagType {
  const map: Record<string, TagType> = { normal: 'success', exiting: 'warning', exited: 'info' };
  return map[s] || 'info';
}
function statusLabel(s: string) {
  return ({ normal: '正常运营', exiting: '退出中', exited: '已退出' } as Record<string, string>)[s] || s;
}

function openDetail(p: any) {
  current.value = p;
  noteDraft.value = '';
  drawerVisible.value = true;
}

function saveNote() {
  if (!noteDraft.value.trim()) {
    ElMessage.warning('请先填写备注内容');
    return;
  }
  ElMessage.success(`已保存「${current.value.projectName}」的投后备注（演示 Mock）`);
}

function remindFollow() {
  ElMessage.success(`已创建「${current.value.projectName}」7 天后的跟进提醒（演示 Mock）`);
}
</script>

<style lang="scss" scoped>
.portfolio {
  .page-header { margin-bottom: 20px; h1 { margin: 0 0 4px; font-size: 24px; } p { margin: 0; color: var(--cft-text-secondary); font-size: 14px; } }
  .overview-card { background: white; border: 1px solid var(--cft-border); border-radius: 12px; padding: 18px; display: flex; gap: 14px; align-items: center; .o-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; } .o-label { font-size: 13px; color: var(--cft-text-muted); } .o-value { font-size: 22px; font-weight: 700; margin-top: 2px; } }
  .p-card {
    background: white; border: 1px solid var(--cft-border); border-radius: 12px;
    padding: 20px; margin-bottom: 16px; cursor: pointer;
    transition: all 0.2s;
    &:hover { border-color: var(--cft-primary); box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
  }
  .p-header { border-bottom: 1px solid var(--cft-border); padding-bottom: 12px; margin-bottom: 12px; }
  .p-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; h3 { margin: 0; } }
  .p-meta { display: flex; gap: 24px; font-size: 13px; color: var(--cft-text-secondary); span strong { color: var(--cft-text-primary); margin-left: 4px; } }
  .p-metrics { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 12px; }
  .metric { .metric-label { display: block; font-size: 12px; color: var(--cft-text-muted); margin-bottom: 4px; } .metric-value { font-size: 14px; font-weight: 500; &.positive { color: var(--cft-success); } small { font-size: 11px; } } }
  .p-footer { display: flex; justify-content: space-between; align-items: center; .milestone { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; .ms-label { font-size: 13px; color: var(--cft-text-muted); } } }
  .mb-4 { margin-bottom: 16px; }
  .drawer-head { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .drawer-sub { font-size: 12px; color: var(--cft-text-muted); } }
  .drawer-section-title { margin: 22px 0 10px; font-size: 14px; }
  .revenue-chart { width: 100%; height: 220px; }
  .ms-timeline { padding-left: 4px; }
  .drawer-actions { display: flex; gap: 12px; margin-top: 12px; }
}
</style>
