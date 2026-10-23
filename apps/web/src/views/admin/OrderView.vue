<template>
  <div class="order-view">
    <div class="page-header">
      <h1>订单结算管理</h1>
    </div>

    <el-row :gutter="16" class="mb-4">
      <el-col :span="6"><div class="stat-card"><div class="label">本月订单数</div><div class="value">{{ stat.monthCount }}</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="label">本月流水</div><div class="value">¥{{ formatMoney(stat.monthAmount) }}</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="label">待结算</div><div class="value" style="color:#f59e0b">¥{{ formatMoney(stat.pendingAmount) }}</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="label">退款订单</div><div class="value" style="color:#ef4444">{{ stat.refundCount }}</div></div></el-col>
    </el-row>

    <el-card class="filter-card mb-4">
      <el-form :inline="true" :model="filter">
        <el-form-item label="订单号"><el-input v-model="filter.orderNo" style="width: 200px" clearable @keyup.enter="loadList(1)" /></el-form-item>
        <el-form-item label="租户"><el-input v-model="filter.tenant" style="width: 160px" clearable @keyup.enter="loadList(1)" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 140px" @change="loadList(1)">
            <el-option label="待支付" value="pending" />
            <el-option label="已支付" value="paid" />
            <el-option label="已退款" value="refunded" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="filter.dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadList(1)">查询</el-button>
          <el-button :icon="Download">导出 Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="200">
        <template #default="{ row }">
          <el-link type="primary">{{ row.orderNo }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="tenantName" label="租户" width="140" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">{{ typeLabel(row.type) }}</template>
      </el-table-column>
      <el-table-column prop="title" label="商品/服务" min-width="180" />
      <el-table-column prop="amount" label="金额" width="120" align="right">
        <template #default="{ row }">
          <strong :style="{ color: row.status === 'refunded' ? '#ef4444' : '' }">¥{{ formatMoney(row.amount) }}</strong>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column prop="paidAt" label="支付时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary">详情</el-button>
          <el-button size="small" link v-if="row.status === 'paid'" type="warning">开发票</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import type { TagType } from '@chuangrongtong/shared';
import { Download } from '@element-plus/icons-vue';
import { httpGet } from '@/utils/request';

interface OrderItem { orderNo: string; tenantId: string; tenantName: string; type: string; title: string; amount: number; status: string; createdAt: string; paidAt: string; }

const loading = ref(false);
const total = ref(0);
const list = ref<OrderItem[]>([]);
const allData = ref<OrderItem[]>([]);

const filter = reactive({ orderNo: '', tenant: '', status: '', dateRange: null as any, pageNum: 1, pageSize: 20 });

const stat = computed(() => {
  const now = new Date();
  return {
    monthCount: allData.value.filter(o => new Date(o.createdAt).getMonth() === now.getMonth()).length,
    monthAmount: allData.value.filter(o => o.status === 'paid').reduce((s, o) => s + Number(o.amount), 0),
    pendingAmount: allData.value.filter(o => o.status === 'pending').reduce((s, o) => s + Number(o.amount), 0),
    refundCount: allData.value.filter(o => o.status === 'refunded').length,
  };
});

function formatMoney(v: any) {
  return Number(v || 0).toLocaleString('zh-CN');
}
function typeLabel(t: string) { return { subscription: '订阅', service: '服务', extension: '升级' }[t] || t; }
function statusType(s: string): TagType {
  const map: Record<string, TagType> = { pending: 'warning', paid: 'success', refunded: 'danger', cancelled: 'info' };
  return map[s] || 'info';
}
function statusLabel(s: string) { return { pending: '待支付', paid: '已支付', refunded: '已退款', cancelled: '已取消' }[s] || s; }

async function loadAll() {
  const res = await httpGet<any>('/api/v1/orders', { params: { pageNum: 1, pageSize: 200 } });
  allData.value = res.data?.list || [];
}

async function loadList(page?: number) {
  if (page) filter.pageNum = page;
  loading.value = true;
  try {
    const res = await httpGet<any>('/api/v1/orders', { params: { orderNo: filter.orderNo, tenant: filter.tenant, status: filter.status, pageNum: filter.pageNum, pageSize: filter.pageSize } });
    list.value = res.data?.list || [];
    total.value = res.data?.total || 0;
  } finally { loading.value = false; }
}

onMounted(() => { loadAll(); loadList(); });
</script>

<style lang="scss" scoped>
.order-view {
  .page-header { margin-bottom: 20px; h1 { margin: 0; font-size: 24px; } }
  .stat-card { background: white; border-radius: 10px; padding: 16px 20px; border: 1px solid var(--cft-border); text-align: center; .label { font-size: 13px; color: var(--cft-text-muted); } .value { font-size: 24px; font-weight: 700; margin-top: 4px; color: var(--cft-primary); } }
  .mb-4 { margin-bottom: 16px; }
  .pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
}
</style>
