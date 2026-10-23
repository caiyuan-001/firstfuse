<template>
  <div class="ai-config">
    <div class="page-header">
      <h1>AI 配置中心</h1>
      <el-button type="primary" :icon="Plus" @click="showEditDialog = true">新增 Prompt 模板</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="Prompt 模板管理" name="prompts">
        <el-table :data="prompts" v-loading="loading" @row-click="showPrompt">
          <el-table-column prop="name" label="模板名称" min-width="180">
            <template #default="{ row }">
              <el-link type="primary">{{ row.name }}</el-link>
              <el-tag v-if="row.isDefault" size="small" class="ml-2">默认</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="scene" label="使用场景" width="140">
            <template #default="{ row }">{{ sceneLabel(row.scene) }}</template>
          </el-table-column>
          <el-table-column prop="model" label="绑定模型" width="140" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" @change="togglePrompt(row)" />
            </template>
          </el-table-column>
          <el-table-column prop="usageCount" label="使用次数" width="100" align="center" />
          <el-table-column prop="avgScore" label="平均评分" width="120" align="center">
            <template #default="{ row }">
              <el-rate :model-value="row.avgScore" disabled size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="small" link type="primary" @click="showPrompt(row)">编辑</el-button>
              <el-button size="small" link @click="testPrompt(row)">测试</el-button>
              <el-button size="small" link type="danger" @click="removePrompt(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="模型路由配置" name="models">
        <div class="cft-card">
          <h3>📡 模型路由表</h3>
          <p class="desc">系统会根据路由规则自动选择最合适的模型处理请求</p>
          <el-table :data="modelRoutes" v-loading="modelsLoading">
            <el-table-column prop="routeKey" label="路由键" width="160" />
            <el-table-column prop="scene" label="场景" width="140" />
            <el-table-column prop="primaryModel" label="主模型" width="180" />
            <el-table-column prop="fallbackModel" label="降级模型" width="180" />
            <el-table-column prop="temperature" label="温度" width="80" />
            <el-table-column prop="maxTokens" label="Max Tokens" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }"><el-switch v-model="row.enabled" @change="toggleRoute(row)" /></template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }"><el-button size="small" link type="primary">编辑</el-button></template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="用量统计" name="usage">
        <div class="cft-card">
          <h3>📊 API 用量趋势（近30天）</h3>
          <v-chart class="usage-chart" :option="usageOption" autoresize />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { httpGet, httpPut, httpDelete } from '@/utils/request';

interface PromptItem { id: string; name: string; scene: string; model: string; enabled: boolean; usageCount: number; avgScore: number; isDefault: boolean; }
interface RouteItem { routeKey: string; scene: string; primaryModel: string; fallbackModel: string; temperature: number; maxTokens: number; enabled: boolean; }

const activeTab = ref('prompts');
const showEditDialog = ref(false);
const loading = ref(false);
const modelsLoading = ref(false);
const prompts = ref<PromptItem[]>([]);
const modelRoutes = ref<RouteItem[]>([]);

const usageOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['token消耗', '请求次数'] },
  xAxis: { type: 'category', data: Array.from({ length: 30 }, (_, i) => `${i + 1}日`) },
  yAxis: [
    { type: 'value', name: 'Tokens', position: 'left' },
    { type: 'value', name: '次数', position: 'right' },
  ],
  series: [
    { name: 'token消耗', type: 'bar', data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 50000) + 10000), itemStyle: { color: '#2563eb' } },
    { name: '请求次数', type: 'line', yAxisIndex: 1, data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 500) + 100), itemStyle: { color: '#22c55e' } },
  ],
  grid: { left: 60, right: 60, top: 40, bottom: 30 },
}));

function sceneLabel(s: string) {
  return { diagnosis: '融资诊断', bp_assistant: 'BP打磨', ai_filter: '项目筛选', review_brief: '评审简报', chat: '通用对话' }[s] || s;
}

async function loadPrompts() {
  loading.value = true;
  try {
    const res = await httpGet<any>('/api/v1/admin/ai/prompts');
    prompts.value = res.data || [];
  } finally { loading.value = false; }
}
async function loadModelRoutes() {
  modelsLoading.value = true;
  try {
    const res = await httpGet<any>('/api/v1/admin/ai/models');
    modelRoutes.value = res.data || [];
  } finally { modelsLoading.value = false; }
}
async function togglePrompt(row: any) {
  await httpPut(`/api/v1/admin/ai/prompts/${row.id}`, { enabled: row.enabled });
}
async function toggleRoute(row: any) {
  await httpPut(`/api/v1/admin/ai/models/${row.routeKey}`, { enabled: row.enabled });
}
function showPrompt(row: any) { ElMessage.info(`编辑模板：${row.name}`); }
function testPrompt(row: any) { ElMessage.success(`测试模板：${row.name}`); }
async function removePrompt(row: any) {
  await ElMessageBox.confirm(`确定删除模板「${row.name}」吗？`, '警告', { type: 'warning' });
  await httpDelete(`/api/v1/admin/ai/prompts/${row.id}`);
  ElMessage.success('已删除');
  loadPrompts();
}

onMounted(() => { loadPrompts(); loadModelRoutes(); });
</script>

<style lang="scss" scoped>
.ai-config {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h1 { margin: 0; font-size: 24px; } }
  .usage-chart { width: 100%; height: 320px; }
  .desc { font-size: 13px; color: var(--cft-text-muted); margin: 0 0 16px; }
  .ml-2 { margin-left: 8px; }
}
</style>
