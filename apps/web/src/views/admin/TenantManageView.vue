<template>
  <div class="tenant-manage">
    <div class="page-header">
      <h1>租户管理</h1>
      <el-button type="primary" :icon="Plus" @click="showCreateDialog = true">开通新租户</el-button>
    </div>

    <el-row :gutter="16" class="mb-4">
      <el-col :span="6" v-for="s in summaryData" :key="s.label">
        <div class="summary-card" :style="{ borderTopColor: s.color }">
          <div class="s-label">{{ s.label }}</div>
          <div class="s-value" :style="{ color: s.color }">{{ s.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-card class="filter-card mb-4">
      <el-form :inline="true" :model="filter" @submit.prevent="loadList(1)">
        <el-form-item label="租户名称">
          <el-input v-model="filter.keyword" placeholder="搜索" clearable style="width: 180px" @keyup.enter="loadList(1)" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filter.type" placeholder="全部" clearable style="width: 140px">
            <el-option label="机构租户" value="institution" />
            <el-option label="平台租户" value="platform" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="开通" value="active" />
            <el-option label="冻结" value="frozen" />
            <el-option label="试用" value="trial" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadList(1)">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="name" label="租户名称" min-width="160">
        <template #default="{ row }">
          <el-link type="primary">{{ row.name }}</el-link>
          <div class="tenant-id">{{ row.id }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'platform' ? 'danger' : 'primary'" size="small">
            {{ row.type === 'platform' ? '平台' : '机构' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="contactName" label="联系人" width="120" />
      <el-table-column prop="contactPhone" label="联系电话" width="140" />
      <el-table-column prop="subscriptionPlan" label="订阅套餐" width="120" />
      <el-table-column prop="expireAt" label="到期时间" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="maxUsers" label="成员数" width="100" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary">详情</el-button>
          <el-button size="small" link type="warning" v-if="row.status === 'active'" @click="freeze(row)">冻结</el-button>
          <el-button size="small" link type="success" v-else @click="unfreeze(row)">开通</el-button>
          <el-button size="small" link type="danger">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="filter.pageNum"
        v-model:page-size="filter.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadList"
        @size-change="loadList"
      />
    </div>

    <!-- 创建租户弹窗 -->
    <el-dialog v-model="showCreateDialog" title="开通新租户" width="520px">
      <el-form label-position="top" :model="newTenant">
        <el-form-item label="租户名称 *"><el-input v-model="newTenant.name" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="类型 *">
            <el-select v-model="newTenant.type" class="w-full">
              <el-option label="机构租户" value="institution" />
              <el-option label="平台租户" value="platform" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="订阅套餐 *">
            <el-select v-model="newTenant.plan" class="w-full">
              <el-option label="基础版（免费试用）" value="basic" />
              <el-option label="专业版 ¥9,800/年" value="pro" />
              <el-option label="企业版 ¥29,800/年" value="enterprise" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="联系人 *"><el-input v-model="newTenant.contactName" /></el-form-item>
        <el-form-item label="联系电话 *"><el-input v-model="newTenant.contactPhone" /></el-form-item>
        <el-form-item label="联系邮箱"><el-input v-model="newTenant.contactEmail" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createTenant">立即开通</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { TagType } from '@chuangrongtong/shared';
import { Plus } from '@element-plus/icons-vue';
import { httpGet, httpPost, httpPut } from '@/utils/request';

interface Tenant { id: string; name: string; type: string; status: string; contactName: string; contactPhone: string; contactEmail?: string; subscriptionPlan?: string; expireAt: string; maxUsers?: number; industry?: string; createdAt?: string; updatedAt?: string; }

const showCreateDialog = ref(false);
const loading = ref(false);
const creating = ref(false);
const total = ref(0);
const list = ref<Tenant[]>([]);

const filter = reactive({ keyword: '', type: '', status: '', pageNum: 1, pageSize: 20 });

const summaryData = ref([
  { label: '总租户数', value: '126', color: '#2563eb' },
  { label: '正常开通', value: '118', color: '#22c55e' },
  { label: '试用中', value: '5', color: '#f59e0b' },
  { label: '已冻结', value: '3', color: '#ef4444' },
]);

const newTenant = reactive({ name: '', type: 'institution', plan: 'pro', contactName: '', contactPhone: '', contactEmail: '' });

function statusType(s: string): TagType {
  const map: Record<string, TagType> = { active: 'success', frozen: 'danger', trial: 'warning' };
  return map[s] || 'info';
}
function statusLabel(s: string) { return { active: '开通', frozen: '冻结', trial: '试用' }[s] || s; }
function resetFilter() { filter.keyword = ''; filter.type = ''; filter.status = ''; filter.pageNum = 1; loadList(1); }

async function loadList(page?: number) {
  if (page) filter.pageNum = page;
  loading.value = true;
  try {
    const res = await httpGet<any>('/api/v1/tenants', { params: { keyword: filter.keyword, type: filter.type, status: filter.status, pageNum: filter.pageNum, pageSize: filter.pageSize } });
    list.value = res.data?.list || [];
    total.value = res.data?.total || 0;
  } finally { loading.value = false; }
}

async function freeze(row: any) {
  await ElMessageBox.confirm(`确定要冻结租户「${row.name}」吗？冻结后该租户所有用户将无法登录。`, '警告', { type: 'warning' });
  await httpPut(`/api/v1/tenants/${row.id}/status`, { status: 'frozen' });
  row.status = 'frozen';
  ElMessage.success('已冻结');
}
async function unfreeze(row: any) {
  await httpPut(`/api/v1/tenants/${row.id}/status`, { status: 'active' });
  row.status = 'active';
  ElMessage.success('已开通');
}
async function createTenant() {
  creating.value = true;
  try {
    await httpPost('/api/v1/tenants', {
      name: newTenant.name, type: newTenant.type, contactName: newTenant.contactName, contactPhone: newTenant.contactPhone, contactEmail: newTenant.contactEmail, subscriptionPlan: newTenant.plan, expireAt: new Date(Date.now() + 365 * 86400000).toISOString().slice(0, 10), maxUsers: 10, status: 'active',
    });
    ElMessage.success('租户开通成功！');
    showCreateDialog.value = false;
    Object.assign(newTenant, { name: '', contactName: '', contactPhone: '', contactEmail: '' });
    loadList(1);
  } finally { creating.value = false; }
}

onMounted(() => loadList());
</script>

<style lang="scss" scoped>
.tenant-manage {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h1 { margin: 0; font-size: 24px; } }
  .summary-card { background: white; border-radius: 10px; padding: 16px 20px; border: 1px solid var(--cft-border); border-top: 3px solid; .s-label { font-size: 13px; color: var(--cft-text-muted); } .s-value { font-size: 24px; font-weight: 700; margin-top: 4px; } }
  .tenant-id { font-size: 11px; color: var(--cft-text-muted); margin-top: 2px; font-family: monospace; }
  .w-full { width: 100%; }
  .mb-4 { margin-bottom: 16px; }
  .pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
}
</style>
