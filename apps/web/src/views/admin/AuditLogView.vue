<template>
  <div class="audit-log-view">
    <div class="page-header">
      <h1>操作审计日志</h1>
      <span class="page-desc">记录平台全部写操作与文件下载，仅平台管理员可见</span>
    </div>

    <el-card class="filter-card mb-4">
      <el-form :inline="true" :model="filter">
        <el-form-item label="请求路径">
          <el-input
            v-model="filter.path"
            placeholder="如 /admin/projects"
            style="width: 220px"
            clearable
            @keyup.enter="loadList(1)"
            @clear="loadList(1)"
          />
        </el-form-item>
        <el-form-item label="操作人 ID">
          <el-input
            v-model="filter.userId"
            placeholder="用户 ID"
            style="width: 180px"
            clearable
            @keyup.enter="loadList(1)"
            @clear="loadList(1)"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadList(1)">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="list" v-loading="loading" row-key="id">
      <el-table-column prop="createdAt" label="操作时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column prop="userId" label="操作人" width="150" show-overflow-tooltip />
      <el-table-column prop="role" label="角色" width="110">
        <template #default="{ row }">{{ roleLabel(row.role) }}</template>
      </el-table-column>
      <el-table-column prop="tenantId" label="租户" width="120" show-overflow-tooltip />
      <el-table-column prop="method" label="方法" width="90">
        <template #default="{ row }">
          <el-tag :type="methodType(row.method)" size="small">{{ row.method }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="请求路径" min-width="240" show-overflow-tooltip />
      <el-table-column prop="statusCode" label="状态码" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.statusCode)" size="small">{{ row.statusCode }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="costMs" label="耗时" width="100" align="right">
        <template #default="{ row }">{{ row.costMs >= 0 ? row.costMs + ' ms' : '-' }}</template>
      </el-table-column>
      <el-table-column prop="clientIp" label="来源 IP" width="140" show-overflow-tooltip />
      <el-table-column prop="detail" label="参数/详情" min-width="200">
        <template #default="{ row }">
          <el-tooltip v-if="row.detail" :content="row.detail" placement="top">
            <span class="detail-text">{{ row.detail }}</span>
          </el-tooltip>
          <span v-else class="empty-text">-</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="filter.pageNum"
        v-model:page-size="filter.pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadList"
        @size-change="loadList"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import type { TagType } from '@chuangrongtong/shared';
import { httpGet } from '@/utils/request';

interface AuditLogItem {
  id: number;
  userId: string;
  role: string;
  tenantId: string;
  method: string;
  path: string;
  statusCode: number;
  costMs: number;
  clientIp: string;
  detail: string | null;
  createdAt: string;
}

const loading = ref(false);
const total = ref(0);
const list = ref<AuditLogItem[]>([]);

const filter = reactive({ path: '', userId: '', pageNum: 1, pageSize: 20 });

function roleLabel(role: string) {
  const map: Record<string, string> = {
    entrepreneur: '创业者',
    investor: '投资机构',
    platform_admin: '平台管理员',
  };
  return map[role] || role || '-';
}

function methodType(method: string): TagType {
  const map: Record<string, TagType> = {
    POST: 'success',
    PUT: 'warning',
    PATCH: 'warning',
    DELETE: 'danger',
    GET: 'info',
  };
  return map[method] || 'info';
}

function statusType(code: number): TagType {
  if (code >= 500) return 'danger';
  if (code >= 400) return 'warning';
  return 'success';
}

function formatTime(v: string) {
  if (!v) return '-';
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return v;
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

async function loadList(page?: number) {
  if (page) filter.pageNum = page;
  loading.value = true;
  try {
    const res = await httpGet<any>('/api/v1/admin/logs', {
      params: {
        path: filter.path || undefined,
        userId: filter.userId || undefined,
        pageNum: filter.pageNum,
        pageSize: filter.pageSize,
      },
    });
    list.value = res.data?.list || [];
    total.value = res.data?.total || 0;
  } finally {
    loading.value = false;
  }
}

function resetFilter() {
  filter.path = '';
  filter.userId = '';
  loadList(1);
}

onMounted(() => loadList());
</script>

<style lang="scss" scoped>
.audit-log-view {
  .page-header {
    margin-bottom: 20px;
    display: flex;
    align-items: baseline;
    gap: 12px;
    h1 { margin: 0; font-size: 24px; }
    .page-desc { font-size: 13px; color: var(--cft-text-muted); }
  }
  .mb-4 { margin-bottom: 16px; }
  .detail-text {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-family: monospace;
    font-size: 12px;
    color: var(--cft-text-secondary);
  }
  .empty-text { color: var(--cft-text-muted); }
  .pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
}
</style>
