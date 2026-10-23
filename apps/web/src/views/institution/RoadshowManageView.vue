<template>
  <div class="roadshow-manage">
    <div class="page-header">
      <h1>路演管理</h1>
      <el-button type="primary" :icon="Plus" @click="openCreate">创建闭门路演</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="我创建的路演" name="mine">
        <el-table :data="myRoadshows">
          <el-table-column prop="title" label="路演名称" min-width="200">
            <template #default="{ row }">
              <el-link type="primary">{{ row.title }}</el-link>
              <el-tag v-if="row.isClosedDoor" type="warning" size="small" class="ml-2">闭门</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="时间" width="160" />
          <el-table-column prop="location" label="地点" width="140" />
          <el-table-column label="项目数" width="80" align="center">
            <template #default="{ row }">{{ row.projectCount }}</template>
          </el-table-column>
          <el-table-column label="报名" width="100" align="center">
            <template #default="{ row }">
              <strong>{{ row.applied }}</strong> / {{ row.maxProjects }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.status)">{{ row.statusLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" link @click="activeTab = 'review'">项目审核</el-button>
              <el-button size="small" link type="primary" @click="viewDetail(row)">详情</el-button>
              <el-button size="small" link type="danger" @click="editRoadshow(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="项目审核" name="review">
        <div class="cft-card">
          <div class="card-header">
            <h3>待审核项目（{{ pendingReviews.length }}）</h3>
          </div>
          <div v-for="item in pendingReviews" :key="item.id" class="review-item">
            <div class="ri-info">
              <div class="ri-title">{{ item.projectName }}</div>
              <div class="ri-meta">报名路演：{{ item.roadshowTitle }} · 申请人：{{ item.applicant }} · {{ item.applyTime }}</div>
            </div>
            <div class="ri-actions">
              <el-button size="small" type="success" :icon="CircleCheck" @click="approve(item)">通过</el-button>
              <el-button size="small" type="danger" :icon="CircleClose" @click="reject(item)">驳回</el-button>
              <el-button size="small" link @click="viewBp(item)">查看 BP</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建/编辑路演弹窗 -->
    <el-dialog v-model="showCreateDialog" :title="editingId ? '编辑路演' : '创建闭门路演'" width="640px">
      <el-form ref="formRef" label-position="top" :model="newRoadshow" :rules="rules">
        <el-form-item label="路演名称" prop="title">
          <el-input v-model="newRoadshow.title" placeholder="例如：人工智能闭门路演 · 第13期" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker v-model="newRoadshow.startTime" type="datetime" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker v-model="newRoadshow.endTime" type="datetime" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="路演地点">
          <el-input v-model="newRoadshow.location" placeholder="线上/城市+场地" />
        </el-form-item>
        <el-form-item label="路演介绍">
          <el-input v-model="newRoadshow.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="最多项目数">
              <el-input-number v-model="newRoadshow.maxProjects" :min="3" :max="20" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否闭门">
              <el-switch v-model="newRoadshow.isClosedDoor" />
              <span class="ml-2 text-muted">闭门路演仅受邀机构可见</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createRoadshow">{{ editingId ? '保存修改' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <!-- 路演详情弹窗 -->
    <el-dialog v-model="detailVisible" title="路演详情" width="520px">
      <el-descriptions v-if="detailRoadshow" :column="1" border size="small">
        <el-descriptions-item label="路演名称">{{ detailRoadshow.title }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ detailRoadshow.time }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ detailRoadshow.location }}</el-descriptions-item>
        <el-descriptions-item label="项目席位">{{ detailRoadshow.applied }} 报名 / {{ detailRoadshow.maxProjects }} 席位</el-descriptions-item>
        <el-descriptions-item label="是否闭门">
          <el-tag :type="detailRoadshow.isClosedDoor ? 'warning' : 'info'" size="small">{{ detailRoadshow.isClosedDoor ? '闭门（仅受邀机构）' : '公开' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(detailRoadshow.status)" size="small">{{ detailRoadshow.statusLabel }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="detailVisible = false; activeTab = 'review'">去审核项目</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import type { TagType } from '@chuangrongtong/shared';
import { Plus, CircleCheck, CircleClose } from '@element-plus/icons-vue';

const activeTab = ref('mine');
const showCreateDialog = ref(false);
const detailVisible = ref(false);
const detailRoadshow = ref<any>(null);
const editingId = ref<string | null>(null);
const formRef = ref<any>(null);

const rules = {
  title: [{ required: true, message: '请填写路演名称', trigger: 'blur' }],
};

const myRoadshows = ref([
  { id: '1', title: '人工智能闭门路演 · 第12期', time: '2026-11-15 14:00', location: '上海·陆家嘴', projectCount: 8, applied: 12, maxProjects: 8, isClosedDoor: true, status: 'ongoing', statusLabel: '进行中' },
  { id: '2', title: '新能源产业专场路演', time: '2026-11-20 10:00', location: '线上腾讯会议', projectCount: 6, applied: 15, maxProjects: 10, isClosedDoor: false, status: 'published', statusLabel: '报名中' },
  { id: '3', title: '医疗健康创新路演', time: '2026-12-05 14:00', location: '北京·中关村', projectCount: 0, applied: 8, maxProjects: 10, isClosedDoor: true, status: 'draft', statusLabel: '草稿' },
]);

const pendingReviews = ref([
  { id: '1', projectName: '下一代 AI Agent 协作平台', roadshowTitle: '人工智能闭门路演·第12期', applicant: '张创业', applyTime: '2小时前' },
  { id: '2', projectName: '智能影像诊断 SaaS', roadshowTitle: '人工智能闭门路演·第12期', applicant: '李明', applyTime: '4小时前' },
  { id: '3', projectName: '边缘计算芯片设计', roadshowTitle: '人工智能闭门路演·第12期', applicant: '王总', applyTime: '6小时前' },
]);

const newRoadshow = reactive({
  title: '', startTime: '', endTime: '', location: '', description: '', maxProjects: 8, isClosedDoor: true,
});

function statusTag(s: string): TagType {
  const map: Record<string, TagType> = { draft: 'info', published: 'primary', ongoing: 'success', completed: 'info', cancelled: 'danger' };
  return map[s] || 'info';
}

function approve(item: any) { ElMessage.success(`已通过「${item.projectName}」的路演申请`); pendingReviews.value = pendingReviews.value.filter(x => x.id !== item.id); }
function reject(item: any) { ElMessage.info(`已驳回「${item.projectName}」的路演申请`); pendingReviews.value = pendingReviews.value.filter(x => x.id !== item.id); }

function viewBp(item: any) {
  ElMessage.success(`已生成「${item.projectName}」BP 预览链接（file_id: bp_mock_${item.id}，演示环境为模拟文件）`);
}

function viewDetail(row: any) {
  detailRoadshow.value = row;
  detailVisible.value = true;
}

function editRoadshow(row: any) {
  editingId.value = row.id;
  Object.assign(newRoadshow, {
    title: row.title,
    startTime: row.time,
    endTime: '',
    location: row.location,
    description: '',
    maxProjects: row.maxProjects,
    isClosedDoor: row.isClosedDoor,
  });
  showCreateDialog.value = true;
}

async function openCreate() {
  editingId.value = null;
  Object.assign(newRoadshow, { title: '', startTime: '', endTime: '', location: '', description: '', maxProjects: 8, isClosedDoor: true });
  showCreateDialog.value = true;
  await nextTick();
  formRef.value?.clearValidate();
}

async function createRoadshow() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  if (editingId.value) {
    const target = myRoadshows.value.find((r) => r.id === editingId.value);
    if (target) {
      target.title = newRoadshow.title;
      target.location = newRoadshow.location || target.location;
      target.maxProjects = newRoadshow.maxProjects;
      target.isClosedDoor = newRoadshow.isClosedDoor;
    }
    ElMessage.success('路演信息已更新！');
  } else {
    myRoadshows.value.unshift({
      id: String(Date.now()),
      title: newRoadshow.title,
      time: newRoadshow.startTime ? String(newRoadshow.startTime).slice(0, 16) : '待定',
      location: newRoadshow.location || '待定',
      projectCount: 0,
      applied: 0,
      maxProjects: newRoadshow.maxProjects,
      isClosedDoor: newRoadshow.isClosedDoor,
      status: 'draft',
      statusLabel: '草稿',
    });
    ElMessage.success('路演创建成功！');
  }
  editingId.value = null;
  Object.assign(newRoadshow, { title: '', startTime: '', endTime: '', location: '', description: '', maxProjects: 8, isClosedDoor: true });
  showCreateDialog.value = false;
}
</script>

<style lang="scss" scoped>
.roadshow-manage {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; h1 { margin: 0; font-size: 24px; } }
  .review-item { display: flex; justify-content: space-between; align-items: center; padding: 14px; background: var(--cft-bg); border-radius: 8px; margin-bottom: 10px; .ri-title { font-weight: 500; margin-bottom: 4px; } .ri-meta { font-size: 12px; color: var(--cft-text-muted); } }
  .ml-2 { margin-left: 8px; }
  .text-muted { color: var(--cft-text-muted); font-size: 12px; }
  .w-full { width: 100%; }
}
</style>
