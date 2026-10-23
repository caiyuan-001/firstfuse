<template>
  <div class="billing">
    <div class="page-header">
      <h1>账单 & 租户设置</h1>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：租户信息 + 套餐 -->
      <el-col :span="12">
        <div class="cft-card">
          <h3>🏢 租户信息</h3>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="租户名称">{{ tenantForm.name }}</el-descriptions-item>
            <el-descriptions-item label="租户ID">demo-tenant-001</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ tenantForm.contact }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ tenantForm.phone }}</el-descriptions-item>
            <el-descriptions-item label="订阅套餐">
              <el-tag type="warning" effect="dark">{{ currentPlan }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="到期时间">2027-06-30</el-descriptions-item>
            <el-descriptions-item label="成员使用">{{ members.length }} / 20 人</el-descriptions-item>
          </el-descriptions>
          <div class="mt-3">
            <el-button type="primary" size="small" @click="upgradeVisible = true">续费/升级</el-button>
            <el-button size="small" @click="editVisible = true">修改信息</el-button>
          </div>
        </div>

        <div class="cft-card mt-4">
          <h3>📦 套餐对比</h3>
          <el-table :data="plans" size="small">
            <el-table-column prop="name" label="功能" width="160" />
            <el-table-column label="基础版" width="100" align="center">
              <template #default="{ row }">{{ row.basic || '✓' }}</template>
            </el-table-column>
            <el-table-column label="专业版" width="100" align="center">
              <template #default="{ row }">{{ row.pro || '✓' }}</template>
            </el-table-column>
            <el-table-column label="企业版（当前）" width="140" align="center">
              <template #default="{ row }">{{ row.enterprise || '✓' }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div class="cft-card mt-4">
          <div class="card-header">
            <h3>👥 成员管理（{{ members.length }}/20）</h3>
            <el-button type="primary" size="small" @click="inviteVisible = true">邀请成员</el-button>
          </div>
          <el-table :data="members" size="small">
            <el-table-column prop="name" label="姓名" width="90" />
            <el-table-column prop="email" label="邮箱" min-width="170" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="row.role === '管理员' ? 'warning' : 'info'" effect="plain">{{ row.role }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="row.active ? 'success' : 'danger'">{{ row.active ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template #default="{ row }">
                <el-button size="small" link :type="row.active ? 'danger' : 'success'" @click="toggleMember(row)">
                  {{ row.active ? '停用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- 右侧：账单 + 设置 -->
      <el-col :span="12">
        <div class="cft-card">
          <div class="card-header">
            <h3>💰 订单账单</h3>
            <el-button type="primary" size="small" :icon="Download" @click="downloadInvoice">下载发票</el-button>
          </div>
          <el-table :data="orders" size="small">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">{{ typeLabel(row.type) }}</template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'paid' ? 'success' : 'warning'" size="small">
                  {{ row.status === 'paid' ? '已支付' : '待支付' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button v-if="row.status !== 'paid'" size="small" link type="primary" @click="payOrder(row)">去支付</el-button>
                <span v-else class="paid-time">{{ row.paidAt }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="cft-card mt-4">
          <h3>⚙️ 租户设置</h3>
          <el-form label-position="top">
            <el-form-item label="机构 Logo">
              <el-upload
                action="#"
                :auto-upload="false"
                list-type="picture-card"
                :limit="1"
                :on-change="onLogoChange"
                :on-remove="() => (logoUrl = '')"
              >
                <img v-if="logoUrl" :src="logoUrl" class="logo-preview" alt="logo" />
                <el-icon v-else><Plus /></el-icon>
              </el-upload>
              <div class="upload-hint">仅前端预览，演示环境不会真实上传</div>
            </el-form-item>
            <el-form-item label="机构简介">
              <el-input v-model="tenantForm.intro" type="textarea" :rows="3" placeholder="介绍一下您的机构..." />
            </el-form-item>
            <el-form-item label="关注行业">
              <el-select v-model="tenantForm.industries" multiple placeholder="选择关注行业" class="w-full">
                <el-option v-for="i in industryOptions" :key="i.value" :label="i.label" :value="i.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>

    <!-- 续费/升级弹窗 -->
    <el-dialog v-model="upgradeVisible" title="续费 / 升级套餐" width="560px">
      <div class="plan-cards">
        <div
          v-for="p in planCards"
          :key="p.name"
          class="plan-card"
          :class="{ active: selectedPlan === p.name, current: currentPlan === p.name }"
          @click="selectedPlan = p.name"
        >
          <div class="pc-name">{{ p.name }} <el-tag v-if="currentPlan === p.name" size="small" type="warning">当前</el-tag></div>
          <div class="pc-price">¥{{ p.price }}<small>/年</small></div>
          <div class="pc-desc">{{ p.desc }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="upgradeVisible = false">取消</el-button>
        <el-button type="primary" :disabled="selectedPlan === currentPlan" @click="confirmUpgrade">确认{{ selectedPlan === '基础版' ? '降级' : '升级' }}</el-button>
      </template>
    </el-dialog>

    <!-- 修改信息弹窗 -->
    <el-dialog v-model="editVisible" title="修改租户信息" width="480px">
      <el-form label-position="top">
        <el-form-item label="租户名称"><el-input v-model="tenantForm.name" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="tenantForm.contact" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="tenantForm.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTenantInfo">保存</el-button>
      </template>
    </el-dialog>

    <!-- 邀请成员弹窗 -->
    <el-dialog v-model="inviteVisible" title="邀请成员" width="480px">
      <el-form label-position="top">
        <el-form-item label="姓名"><el-input v-model="inviteForm.name" placeholder="成员姓名" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="inviteForm.email" placeholder="用于接收邀请链接" /></el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="inviteForm.role">
            <el-radio value="投资人">投资人</el-radio>
            <el-radio value="分析师">分析师</el-radio>
            <el-radio value="管理员">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inviteVisible = false">取消</el-button>
        <el-button type="primary" @click="sendInvite">发送邀请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { Download, Plus } from '@element-plus/icons-vue';
import { INDUSTRY_OPTIONS } from '@chuangrongtong/shared';

const industryOptions = INDUSTRY_OPTIONS;
const currentPlan = ref('企业版');
const upgradeVisible = ref(false);
const editVisible = ref(false);
const inviteVisible = ref(false);
const selectedPlan = ref('企业版');
const saving = ref(false);
const logoUrl = ref('');

const tenantForm = reactive({
  name: '红杉创投（演示租户）',
  contact: '李投资',
  phone: '138****8000',
  intro: '',
  industries: ['ai', 'biotech'] as string[],
});

const inviteForm = reactive({ name: '', email: '', role: '投资人' });

const members = ref([
  { name: '李投资', email: 'li@sequoia-demo.cn', role: '管理员', active: true },
  { name: '王分析', email: 'wang@sequoia-demo.cn', role: '分析师', active: true },
  { name: '陈合伙', email: 'chen@sequoia-demo.cn', role: '投资人', active: true },
  { name: '赵实习', email: 'zhao@sequoia-demo.cn', role: '分析师', active: false },
]);

const plans = [
  { name: '项目浏览', basic: '✓', pro: '✓', enterprise: '✓' },
  { name: '收藏项目', basic: '10个', pro: '100个', enterprise: '无限' },
  { name: 'AI 评审简报', basic: '×', pro: '每月20份', enterprise: '无限' },
  { name: 'AI 智能筛选', basic: '×', pro: '✓', enterprise: '✓' },
  { name: '企业关系图谱', basic: '×', pro: '×', enterprise: '✓' },
  { name: '闭门路演参与', basic: '×', pro: '3次/月', enterprise: '无限' },
];

const planCards = [
  { name: '基础版', price: '9,800', desc: '项目浏览 + 基础收藏，适合个人投资人' },
  { name: '专业版', price: '19,800', desc: '含 AI 简报与智能筛选，适合小型机构' },
  { name: '企业版', price: '29,800', desc: '全功能 + 无限额度 + 专属客服' },
];

const orders = ref([
  { orderNo: 'ORD20260101001', type: 'subscription', amount: '29,800', status: 'paid', paidAt: '2026-07-01' },
  { orderNo: 'ORD20260615002', type: 'extension', amount: '5,000', status: 'paid', paidAt: '2026-06-15' },
  { orderNo: 'ORD20261001003', type: 'service', amount: '12,000', status: 'pending', paidAt: '' },
]);

function typeLabel(t: string) {
  return ({ subscription: '会员', extension: '增值服务', service: '单次服务' } as Record<string, string>)[t] || t;
}

function confirmUpgrade() {
  currentPlan.value = selectedPlan.value;
  upgradeVisible.value = false;
  ElMessage.success(`已切换为「${selectedPlan.value}」，差价将按剩余时长折算（演示 Mock）`);
}

function saveTenantInfo() {
  editVisible.value = false;
  ElMessage.success('租户信息已更新（演示 Mock）');
}

function downloadInvoice() {
  ElMessage.success('发票 PDF 已生成：invoice_202610.pdf（演示 Mock）');
}

function payOrder(row: any) {
  row.status = 'paid';
  row.paidAt = '2026-10-19';
  ElMessage.success(`订单 ${row.orderNo} 支付成功（演示 Mock）`);
}

function toggleMember(row: any) {
  row.active = !row.active;
  ElMessage.success(`已${row.active ? '启用' : '停用'}成员「${row.name}」`);
}

function sendInvite() {
  if (!inviteForm.name || !inviteForm.email) {
    ElMessage.warning('请填写姓名和邮箱');
    return;
  }
  members.value.push({ name: inviteForm.name, email: inviteForm.email, role: inviteForm.role, active: true });
  inviteVisible.value = false;
  inviteForm.name = '';
  inviteForm.email = '';
  ElMessage.success('邀请邮件已发送（演示 Mock）');
}

function onLogoChange(file: any) {
  if (file.raw) logoUrl.value = URL.createObjectURL(file.raw);
}

function saveSettings() {
  saving.value = true;
  setTimeout(() => {
    saving.value = false;
    ElMessage.success('设置已保存（演示 Mock）');
  }, 600);
}
</script>

<style lang="scss" scoped>
.billing {
  .page-header { margin-bottom: 20px; h1 { margin: 0; font-size: 24px; } }
  .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; h3 { margin: 0; font-size: 15px; } }
  h3 { margin: 0 0 16px; font-size: 16px; }
  .w-full { width: 100%; }
  .mt-3 { margin-top: 12px; }
  .mt-4 { margin-top: 16px; }
  .paid-time { font-size: 12px; color: var(--cft-text-muted); }
  .upload-hint { font-size: 12px; color: var(--cft-text-muted); margin-top: 4px; }
  .logo-preview { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }
  .plan-cards { display: flex; gap: 12px; }
  .plan-card {
    flex: 1; border: 2px solid var(--cft-border); border-radius: 12px; padding: 16px; cursor: pointer; transition: all 0.2s;
    &.active { border-color: var(--cft-primary); background: #eff6ff; }
    &.current { background: #fffbeb; }
    &.active.current { background: #eff6ff; }
    .pc-name { font-weight: 600; font-size: 14px; display: flex; align-items: center; gap: 6px; }
    .pc-price { font-size: 22px; font-weight: 700; color: var(--cft-primary); margin: 8px 0; small { font-size: 12px; color: var(--cft-text-muted); font-weight: 400; } }
    .pc-desc { font-size: 12px; color: var(--cft-text-secondary); line-height: 1.6; }
  }
}
</style>
