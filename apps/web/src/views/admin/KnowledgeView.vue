<template>
  <div class="knowledge">
    <div class="page-header">
      <h1>知识库管理</h1>
      <el-button type="primary" :icon="Plus" @click="showAddDialog = true">新增文章</el-button>
    </div>

    <el-row :gutter="16" class="mb-4">
      <el-col :span="4" v-for="s in stats" :key="s.label">
        <div class="stat-card">
          <div class="icon" :style="{ background: s.bg, color: s.color }"><el-icon :size="20"><component :is="s.icon" /></el-icon></div>
          <div class="info">
            <div class="value">{{ s.value }}</div>
            <div class="label">{{ s.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card class="filter-card mb-4">
      <el-form :inline="true" :model="filter">
        <el-form-item label="标题"><el-input v-model="filter.keyword" style="width: 200px" clearable @keyup.enter="loadList(1)" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filter.category" placeholder="全部" clearable style="width: 160px" @change="loadList(1)">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadList(1)">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="240">
        <template #default="{ row }">
          <el-link type="primary">{{ row.title }}</el-link>
          <div class="tags" style="margin-top:4px">
            <el-tag v-for="t in (row.tags || [])" :key="t" size="small" effect="plain" class="mr-1">{{ t }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column prop="updatedAt" label="更新时间" width="160" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch v-model="row.published" @change="togglePublish(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary">编辑</el-button>
          <el-button size="small" link type="success">预览</el-button>
          <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
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

    <!-- 新增弹窗 -->
    <el-dialog v-model="showAddDialog" title="新增文章" width="520px">
      <el-form label-position="top" :model="newItem">
        <el-form-item label="标题 *"><el-input v-model="newItem.title" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="分类 *">
            <el-select v-model="newItem.category" class="w-full">
              <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="标签"><el-input v-model="newItem.tagsText" placeholder="逗号分隔" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="正文（摘要）"><el-input type="textarea" :rows="4" v-model="newItem.summary" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="add">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Reading, View, EditPen, TrendCharts } from '@element-plus/icons-vue';
import { httpGet, httpPost, httpPatch, httpDelete } from '@/utils/request';

interface KItem { id: string; title: string; category: string; viewCount: number; tags?: string[]; createdAt: string; updatedAt: string; published: boolean; }

const loading = ref(false);
const creating = ref(false);
const showAddDialog = ref(false);
const total = ref(0);
const list = ref<KItem[]>([]);

const filter = reactive({ keyword: '', category: '', pageNum: 1, pageSize: 20 });
const categories = ['BP写作', '估值方法', '融资流程', '法律文件', '行业研究', '常见问题'];

const stats = ref([
  { label: '文章总数', value: '156', icon: Reading, color: '#2563eb', bg: '#dbeafe' },
  { label: '本月新增', value: '12', icon: EditPen, color: '#22c55e', bg: '#dcfce7' },
  { label: '累计浏览', value: '86,200', icon: View, color: '#f59e0b', bg: '#fef3c7' },
  { label: '热门文章', value: '5', icon: TrendCharts, color: '#8b5cf6', bg: '#f3e8ff' },
]);

const newItem = reactive({ title: '', category: 'BP写作', tagsText: '', summary: '' });

async function loadList(page?: number) {
  if (page) filter.pageNum = page;
  loading.value = true;
  try {
    const res = await httpGet<any>('/api/v1/knowledge', { params: { keyword: filter.keyword, category: filter.category, pageNum: filter.pageNum, pageSize: filter.pageSize } });
    list.value = res.data?.list || [];
    total.value = res.data?.total || 0;
  } finally { loading.value = false; }
}

async function togglePublish(row: any) {
  await httpPatch(`/api/v1/knowledge/${row.id}/publish`, { published: row.published });
  ElMessage.success(row.published ? '已发布' : '已取消发布');
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除文章「${row.title}」吗？`, '警告', { type: 'warning' });
  await httpDelete(`/api/v1/knowledge/${row.id}`);
  ElMessage.success('已删除');
  loadList();
}

async function add() {
  creating.value = true;
  try {
    await httpPost('/api/v1/knowledge', {
      title: newItem.title, category: newItem.category,
      tags: newItem.tagsText.split(',').map((s: string) => s.trim()).filter(Boolean),
      viewCount: 0, published: true,
    });
    ElMessage.success('已保存');
    showAddDialog.value = false;
    Object.assign(newItem, { title: '', tagsText: '', summary: '' });
    loadList(1);
  } finally { creating.value = false; }
}

onMounted(() => loadList());
</script>

<style lang="scss" scoped>
.knowledge {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h1 { margin: 0; font-size: 24px; } }
  .stat-card { display: flex; gap: 14px; align-items: center; background: white; border: 1px solid var(--cft-border); border-radius: 10px; padding: 14px 16px; .icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; } .value { font-size: 20px; font-weight: 700; color: var(--cft-text-primary); } .label { font-size: 12px; color: var(--cft-text-muted); } }
  .mr-1 { margin-right: 4px; }
  .mb-4 { margin-bottom: 16px; }
  .w-full { width: 100%; }
  .pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
}
</style>
