<template>
  <div class="project-review">
    <div class="page-header">
      <h1>项目审核工作台</h1>
      <el-tag type="warning" size="large">待审核：{{ pendingCount }} 项</el-tag>
    </div>

    <el-tabs v-model="activeTab" @tab-change="loadList">
      <el-tab-pane label="待审核" name="pending" />
      <el-tab-pane label="已通过" name="pass" />
      <el-tab-pane label="已驳回" name="reject" />
      <el-tab-pane label="全部" name="all" />
    </el-tabs>

    <div class="review-queue" v-loading="loading">
      <div v-for="item in filteredList" :key="item.id" class="review-item cft-card">
        <div class="ri-main">
          <div class="ri-title-row">
            <h3>{{ item.title }}</h3>
            <el-tag size="small">{{ item.industry }}</el-tag>
            <el-tag size="small">{{ item.stageLabel }}</el-tag>
            <el-tag type="warning" v-if="item.aiSuspicious" size="small">AI 标记可疑</el-tag>
          </div>
          <p class="ri-short">{{ item.shortDesc }}</p>
          <div class="ri-meta">
            <span>👤 申请人：{{ item.ownerName }}</span>
            <span>📅 提交时间：{{ item.submitTime }}</span>
            <span>💰 融资需求：{{ item.amount }} 万</span>
          </div>
        </div>
        <div class="ri-actions">
          <el-button type="success" :loading="passingId === item.id" @click="doPass(item)">
            <el-icon><CircleCheck /></el-icon>通过
          </el-button>
          <el-button type="danger" :loading="rejectingId === item.id" @click="doReject(item)">
            <el-icon><CircleClose /></el-icon>驳回
          </el-button>
          <el-button type="primary" plain @click="showDetail(item)">
            <el-icon><View /></el-icon>查看详情
          </el-button>
        </div>
      </div>
    </div>

    <div class="pagination-wrap" style="display:flex;justify-content:flex-end;margin-top:16px" v-if="filteredList.length">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20]" layout="total, sizes, prev, pager, next" @current-change="loadList" @size-change="loadList" />
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="项目详情" size="600px" direction="rtl">
      <div v-if="currentItem">
        <h2>{{ currentItem.title }}</h2>
        <el-divider content-position="left">基本信息</el-divider>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="项目名称">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="所属行业">{{ currentItem.industry }}</el-descriptions-item>
          <el-descriptions-item label="融资阶段">{{ currentItem.stageLabel }}</el-descriptions-item>
          <el-descriptions-item label="融资金额">{{ currentItem.amount }} 万</el-descriptions-item>
          <el-descriptions-item label="地区">{{ currentItem.region }}</el-descriptions-item>
          <el-descriptions-item label="估值">{{ currentItem.valuation }} 万</el-descriptions-item>
          <el-descriptions-item label="出让股权">{{ currentItem.equityOffered }}%</el-descriptions-item>
          <el-descriptions-item label="成立时间">{{ currentItem.foundedAt }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">项目简介</el-divider>
        <p>{{ currentItem.longDesc }}</p>
        <el-divider content-position="left">AI 预审核结果</el-divider>
        <div class="ai-review">
          <div class="ar-score">
            <el-progress type="dashboard" :percentage="currentItem.aiScore" :width="80" :color="currentItem.aiScore >= 60 ? '#22c55e' : '#ef4444'" />
          </div>
          <div class="ar-content">
            <h4>AI 建议：{{ currentItem.aiScore >= 60 ? '建议通过' : '建议人工关注' }}</h4>
            <p>{{ currentItem.aiComment }}</p>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { CircleCheck, CircleClose, View } from '@element-plus/icons-vue';
import { httpGet, httpPut } from '@/utils/request';

interface ReviewItem { id: string; title: string; industry: string; stageLabel: string; amount: number; ownerName: string; submitTime: string; shortDesc: string; longDesc: string; region: string; valuation: number; equityOffered: number; foundedAt: string; aiScore: number; aiComment: string; aiSuspicious: boolean; status: string; rejectReason?: string; }

const activeTab = ref('pending');
const loading = ref(false);
const pendingCount = ref(0);
const detailVisible = ref(false);
const currentItem = ref<ReviewItem | null>(null);
const passingId = ref('');
const rejectingId = ref('');

const list = ref<ReviewItem[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const filteredList = computed(() => list.value); // 状态筛选已由 mock 侧完成，前端只做分页切片

async function loadPendingCount() {
  const res = await httpGet<any>('/api/v1/admin/projects/reviews', { params: { status: 'pending', pageNum: 1, pageSize: 1 } });
  pendingCount.value = res.data?.total ?? 0;
}

async function loadList() {
  loading.value = true;
  try {
    const res = await httpGet<any>('/api/v1/admin/projects/reviews', { params: { status: activeTab.value, pageNum: pageNum.value, pageSize: pageSize.value } });
    list.value = res.data?.list || [];
    total.value = res.data?.total || 0;
  } finally { loading.value = false; }
}

async function doPass(item: ReviewItem) {
  await ElMessageBox.confirm(`确定审核通过「${item.title}」吗？`, '审核确认');
  passingId.value = item.id;
  try {
    await httpPut(`/api/v1/admin/projects/reviews/${item.id}`, { decision: 'pass' });
    ElMessage.success('已通过');
    loadPendingCount(); loadList();
  } finally { passingId.value = ''; }
}

async function doReject(item: ReviewItem) {
  let reason = '';
  try {
    reason = await ElMessageBox.prompt('请填写驳回原因', '驳回确认', {
      inputPattern: /\S+/,
      inputErrorMessage: '驳回原因不能为空',
      inputPlaceholder: '请输入驳回原因...',
    }).then(r => r.value);
  } catch { return; }

  rejectingId.value = item.id;
  try {
    await httpPut(`/api/v1/admin/projects/reviews/${item.id}`, { decision: 'reject', reason });
    ElMessage.success('已驳回');
    loadPendingCount(); loadList();
  } finally { rejectingId.value = ''; }
}

function showDetail(item: ReviewItem) { currentItem.value = item; detailVisible.value = true; }

onMounted(() => { loadPendingCount(); loadList(); });
</script>

<style lang="scss" scoped>
.project-review {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h1 { margin: 0; font-size: 24px; } }
  .review-queue { display: flex; flex-direction: column; gap: 12px; margin-top: 16px; }
  .review-item { display: flex; justify-content: space-between; align-items: flex-start; }
  .ri-main { flex: 1; margin-right: 24px; }
  .ri-title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; h3 { margin: 0; font-size: 16px; } }
  .ri-short { color: var(--cft-text-secondary); font-size: 14px; margin: 0 0 10px; line-height: 1.6; }
  .ri-meta { display: flex; gap: 20px; font-size: 12px; color: var(--cft-text-muted); }
  .ri-actions { display: flex; gap: 10px; flex-shrink: 0; }
  .ai-review { display: flex; gap: 20px; .ar-content { flex: 1; h4 { margin: 0 0 6px; } p { margin: 0; font-size: 13px; color: var(--cft-text-secondary); line-height: 1.6; } } }
}
</style>
