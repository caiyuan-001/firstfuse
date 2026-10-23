<template>
  <div class="blacklist">
    <div class="page-header">
      <h1>风控黑名单管理</h1>
      <el-button type="primary" :icon="Plus" @click="showAddDialog = true">加入黑名单</el-button>
    </div>

    <el-alert type="warning" :closable="false" class="mb-4">
      <template #title>
        <strong>⚠️ 风险提示</strong>
        <span class="ml-2">加入黑名单的实体将无法正常使用平台服务。请谨慎操作，并记录合理的封禁原因。</span>
      </template>
    </el-alert>

    <el-row :gutter="16" class="mb-4">
      <el-col :span="4" v-for="s in stats" :key="s.label">
        <div class="stat-card" :style="{ borderLeftColor: s.color }">
          <div class="value" :style="{ color: s.color }">{{ s.value }}</div>
          <div class="label">{{ s.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-card class="filter-card mb-4">
      <el-form :inline="true" :model="filter">
        <el-form-item label="目标值"><el-input v-model="filter.keyword" style="width: 200px" clearable @keyup.enter="loadList(1)" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filter.type" placeholder="全部" clearable style="width: 140px" @change="loadList(1)">
            <el-option label="用户" value="user" />
            <el-option label="租户" value="tenant" />
            <el-option label="项目" value="project" />
            <el-option label="手机号" value="phone" />
            <el-option label="IP" value="ip" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadList(1)">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="typeColor(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetValue" label="目标值" min-width="200">
        <template #default="{ row }">
          <el-link type="danger">{{ row.targetValue }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="封禁原因" min-width="200" show-overflow-tooltip />
      <el-table-column prop="operatorName" label="操作人" width="120" />
      <el-table-column prop="createdAt" label="加入时间" width="160" />
      <el-table-column prop="expiresAt" label="到期时间" width="160">
        <template #default="{ row }">{{ row.expiresAt || '永久' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary">详情</el-button>
          <el-button size="small" link type="success" @click="unblock(row)">解除</el-button>
          <el-button size="small" link type="warning">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="filter.pageNum"
        v-model:page-size="filter.pageSize"
        :total="total"
        :page-sizes="[10, 20]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadList"
        @size-change="loadList"
      />
    </div>

    <!-- 新增弹窗 -->
    <el-dialog v-model="showAddDialog" title="加入黑名单" width="480px">
      <el-form label-position="top" :model="newEntry">
        <el-form-item label="类型 *">
          <el-select v-model="newEntry.type" class="w-full">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标值 *">
          <el-input v-model="newEntry.targetValue" placeholder="用户ID / 租户ID / 手机号 / IP..." />
        </el-form-item>
        <el-form-item label="封禁原因 *">
          <el-input type="textarea" :rows="3" v-model="newEntry.reason" placeholder="请详细说明封禁原因..." />
        </el-form-item>
        <el-form-item label="到期时间">
          <el-date-picker v-model="newEntry.expiresAt" type="datetime" class="w-full" placeholder="留空表示永久封禁" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="danger" :loading="creating" @click="addBlock">确认封禁</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { TagType } from '@chuangrongtong/shared';
import { Plus } from '@element-plus/icons-vue';
import { httpGet, httpPost, httpDelete } from '@/utils/request';

interface BlackItem { id: string; type: string; targetValue: string; reason: string; operatorName: string; createdAt: string; expiresAt: string; }

const showAddDialog = ref(false);
const loading = ref(false);
const creating = ref(false);
const total = ref(0);
const list = ref<BlackItem[]>([]);

const filter = reactive({ keyword: '', type: '', pageNum: 1, pageSize: 20 });

const stats = ref([
  { label: '黑名单总数', value: '18', color: '#ef4444' },
  { label: '封禁用户', value: '8', color: '#f59e0b' },
  { label: '封禁租户', value: '3', color: '#8b5cf6' },
  { label: '本月新增', value: '5', color: '#2563eb' },
]);

const typeOptions = [
  { value: 'user', label: '用户' },
  { value: 'tenant', label: '租户' },
  { value: 'project', label: '项目' },
  { value: 'phone', label: '手机号' },
  { value: 'ip', label: 'IP 地址' },
];

const newEntry = reactive({ type: 'user', targetValue: '', reason: '', expiresAt: null as any });

function typeLabel(t: string) { return typeOptions.find(o => o.value === t)?.label || t; }
function typeColor(t: string): TagType {
  const map: Record<string, TagType> = { user: 'primary', tenant: 'danger', project: 'warning', phone: 'success', ip: 'info' };
  return map[t] || 'primary';
}

async function loadList(page?: number) {
  if (page) filter.pageNum = page;
  loading.value = true;
  try {
    const res = await httpGet<any>('/api/v1/blacklist', { params: { keyword: filter.keyword, type: filter.type, pageNum: filter.pageNum, pageSize: filter.pageSize } });
    list.value = res.data?.list || [];
    total.value = res.data?.total || 0;
  } finally { loading.value = false; }
}

async function unblock(row: any) {
  await ElMessageBox.confirm(`确定解除对「${row.targetValue}」的封禁吗？`, '确认', { type: 'warning' });
  await httpDelete(`/api/v1/blacklist/${row.id}`);
  ElMessage.success('已解除封禁');
  loadList();
}

async function addBlock() {
  creating.value = true;
  try {
    await httpPost('/api/v1/blacklist', { ...newEntry, operatorName: '王运营' });
    ElMessage.success('已加入黑名单');
    showAddDialog.value = false;
    Object.assign(newEntry, { type: 'user', targetValue: '', reason: '', expiresAt: null });
    loadList(1);
  } finally { creating.value = false; }
}

onMounted(() => loadList());
</script>

<style lang="scss" scoped>
.blacklist {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h1 { margin: 0; font-size: 24px; } }
  .stat-card { background: white; border: 1px solid var(--cft-border); border-radius: 10px; padding: 16px 20px; border-left: 3px solid; text-align: center; .value { font-size: 24px; font-weight: 700; } .label { font-size: 13px; color: var(--cft-text-muted); margin-top: 4px; } }
  .ml-2 { margin-left: 8px; }
  .mb-4 { margin-bottom: 16px; }
  .w-full { width: 100%; }
  .pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
}
</style>
