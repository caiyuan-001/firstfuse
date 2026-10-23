<template>
  <div class="roadshow-op">
    <div class="page-header">
      <h1>路演运营管理</h1>
      <el-button type="primary" :icon="Plus" @click="showCreateDialog = true">创建官方路演</el-button>
    </div>

    <el-row :gutter="16" class="mb-4">
      <el-col :span="6"><div class="stat-card"><div class="label">进行中路演</div><div class="value" style="color:#22c55e">{{ stat.ongoing }}</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="label">本月路演总数</div><div class="value" style="color:#2563eb">{{ stat.totalThisMonth }}</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="label">参与总人次</div><div class="value" style="color:#f59e0b">{{ stat.totalParticipants }}</div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="label">成功撮合</div><div class="value" style="color:#8b5cf6">{{ stat.matches }}</div></div></el-col>
    </el-row>

    <el-card class="filter-card mb-4">
      <el-form :inline="true" :model="filter">
        <el-form-item label="路演名称"><el-input v-model="filter.keyword" style="width: 200px" clearable @keyup.enter="loadList(1)" /></el-form-item>
        <el-form-item label="主办机构"><el-input v-model="filter.organizer" style="width: 160px" clearable @keyup.enter="loadList(1)" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 140px" @change="loadList(1)">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已结束" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadList(1)">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="title" label="路演名称" min-width="220">
        <template #default="{ row }">
          <el-link type="primary">{{ row.title }}</el-link>
          <div class="meta-small">{{ row.isOfficial ? '🏛️ 官方路演' : '机构主办' }} · {{ row.isClosedDoor ? '🔒 闭门' : '公开' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="organizer" label="主办机构" width="140" />
      <el-table-column prop="startTime" label="开始时间" width="160" />
      <el-table-column prop="endTime" label="结束时间" width="160" />
      <el-table-column label="项目数" width="80" align="center">
        <template #default="{ row }">{{ row.projectCount }}</template>
      </el-table-column>
      <el-table-column label="报名数" width="80" align="center">
        <template #default="{ row }">{{ row.participants }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary">详情</el-button>
          <el-button size="small" link type="warning">编辑</el-button>
          <el-button size="small" link type="danger" @click="cancel(row)">取消</el-button>
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

    <!-- 创建官方路演弹窗 -->
    <el-dialog v-model="showCreateDialog" title="创建官方路演" width="520px">
      <el-form label-position="top" :model="newItem">
        <el-form-item label="路演名称 *"><el-input v-model="newItem.title" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="开始时间 *"><el-date-picker v-model="newItem.startTime" type="datetime" class="w-full" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结束时间 *"><el-date-picker v-model="newItem.endTime" type="datetime" class="w-full" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="类型"><el-select v-model="newItem.type" class="w-full"><el-option label="线上" value="online" /><el-option label="线下" value="offline" /><el-option label="混合" value="hybrid" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="是否闭门"><el-switch v-model="newItem.isClosedDoor" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createRoadshow">立即创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { TagType } from '@chuangrongtong/shared';
import { Plus } from '@element-plus/icons-vue';
import { httpGet, httpPost, httpPut } from '@/utils/request';

interface RoadshowItem { id: string; title: string; organizer: string; startTime: string; endTime: string; projectCount: number; participants: number; status: string; statusLabel: string; isOfficial: boolean; isClosedDoor: boolean; }

const loading = ref(false);
const creating = ref(false);
const showCreateDialog = ref(false);
const total = ref(0);
const list = ref<RoadshowItem[]>([]);
const allData = ref<RoadshowItem[]>([]);

const filter = reactive({ keyword: '', organizer: '', status: '', pageNum: 1, pageSize: 10 });

const stat = computed(() => ({
  ongoing: allData.value.filter(r => r.status === 'ongoing').length,
  totalThisMonth: allData.value.length,
  totalParticipants: allData.value.reduce((s, r) => s + (r.participants || 0), 0),
  matches: 42, // demo 固定
}));

const newItem = reactive({ title: '', startTime: null as any, endTime: null as any, type: 'hybrid', isClosedDoor: false });

function statusType(s: string): TagType {
  const map: Record<string, TagType> = { draft: 'info', published: 'primary', ongoing: 'success', completed: 'info' };
  return map[s] || 'info';
}

async function loadAll() {
  const res = await httpGet<any>('/api/v1/roadshows', { params: { pageNum: 1, pageSize: 100 } });
  allData.value = res.data?.list || [];
}

async function loadList(page?: number) {
  if (page) filter.pageNum = page;
  loading.value = true;
  try {
    const res = await httpGet<any>('/api/v1/roadshows', { params: { keyword: filter.keyword, organizer: filter.organizer, status: filter.status, pageNum: filter.pageNum, pageSize: filter.pageSize } });
    list.value = res.data?.list || [];
    total.value = res.data?.total || 0;
  } finally { loading.value = false; }
}

async function cancel(row: any) {
  await ElMessageBox.confirm(`确定取消路演「${row.title}」吗？`, '警告', { type: 'warning' });
  await httpPut(`/api/v1/roadshows/${row.id}`, { status: 'cancelled', statusLabel: '已取消' });
  ElMessage.success('已取消');
  loadList();
}

async function createRoadshow() {
  creating.value = true;
  try {
    await httpPost('/api/v1/roadshows', {
      ...newItem,
      organizer: '慧联创融（官方）',
      isOfficial: true,
      status: 'draft',
      statusLabel: '草稿',
      projectCount: 0,
      participants: 0,
    });
    ElMessage.success('创建成功');
    showCreateDialog.value = false;
    Object.assign(newItem, { title: '', startTime: null, endTime: null, type: 'hybrid', isClosedDoor: false });
    loadAll(); loadList(1);
  } finally { creating.value = false; }
}

onMounted(() => { loadAll(); loadList(); });
</script>

<style lang="scss" scoped>
.roadshow-op {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h1 { margin: 0; font-size: 24px; } }
  .stat-card { background: white; border-radius: 10px; padding: 16px 20px; border: 1px solid var(--cft-border); text-align: center; .label { font-size: 13px; color: var(--cft-text-muted); } .value { font-size: 24px; font-weight: 700; margin-top: 4px; } }
  .meta-small { font-size: 11px; color: var(--cft-text-muted); margin-top: 2px; }
  .mb-4 { margin-bottom: 16px; }
  .w-full { width: 100%; }
  .pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
}
</style>
