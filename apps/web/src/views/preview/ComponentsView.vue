<template>
  <div class="components-preview">
    <!-- 页面头 -->
    <CftPageHeader
      title="全局公共组件"
      subtitle="Cft* 系列 · 9 个业务组件 · Demo 级别，可直接用于 25 页"
    />

    <!-- CftCard -->
    <CftCard title="① CftCard · 通用卡片容器">
      <p style="color: var(--cft-text-secondary); margin: 0 0 12px;">Overview / TenantManage / Knowledge 等 7 个 admin 页都在用的容器。</p>
      <div style="display: flex; gap: 12px; margin-bottom: 16px;">
        <el-switch v-model="demo.bordered" active-text="bordered" />
        <el-switch v-model="demo.shadowHover" active-text="hover-shadow" />
      </div>
      <CftCard title="嵌套示例" :bordered="demo.bordered" :shadow="demo.shadowHover ? 'hover' : 'always'">
        <p style="color: var(--cft-text-secondary); margin: 0;">CftCard 支持 header slot + body slot + 三种 shadow 模式。</p>
      </CftCard>
      <el-divider content-position="left">Props</el-divider>
      <PropsTable :items="propsDefs.CftCard" />
    </CftCard>

    <!-- CftPageHeader -->
    <CftCard title="② CftPageHeader · 页面头部">
      <p style="color: var(--cft-text-secondary); margin: 0 0 12px;">所有列表页的 h1 + 副标题 + 右侧操作区。</p>
      <CftPageHeader title="租户管理（示例）" subtitle="平台所有租户一览，支持开通 / 冻结 / 到期提醒">
        <template #actions>
          <el-button type="primary">开通新租户</el-button>
        </template>
      </CftPageHeader>
      <el-divider content-position="left">Props</el-divider>
      <PropsTable :items="propsDefs.CftPageHeader" />
    </CftCard>

    <!-- CftFilterBar -->
    <CftCard title="③ CftFilterBar · 查询过滤条">
      <p style="color: var(--cft-text-secondary); margin: 0 0 12px;">TenantManage / Knowledge 的 inline 查询表单封装。</p>
      <CftFilterBar>
        <el-form-item label="关键词">
          <el-input v-model="demo.keyword" placeholder="搜索租户" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="demo.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="开通" value="active" />
            <el-option label="冻结" value="frozen" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">查询</el-button>
          <el-button>重置</el-button>
        </el-form-item>
      </CftFilterBar>
      <el-divider content-position="left">Props</el-divider>
      <PropsTable :items="propsDefs.CftFilterBar" />
    </CftCard>

    <!-- CftPagination -->
    <CftCard title="④ CftPagination · 分页控件">
      <p style="color: var(--cft-text-secondary); margin: 0 0 12px;">所有列表页统一的分页条。当前演示数据：{{ demo.total }} 条。</p>
      <el-table :data="demo.items" size="small" v-loading="false">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="value" label="值" />
      </el-table>
      <CftPagination v-model:current-page="demo.page" v-model:page-size="demo.pageSize" :total="demo.total" />
      <el-divider content-position="left">Props</el-divider>
      <PropsTable :items="propsDefs.CftPagination" />
    </CftCard>

    <!-- CftStatusTag -->
    <CftCard title="⑤ CftStatusTag · 状态标签（内置映射）">
      <p style="color: var(--cft-text-secondary); margin: 0 0 12px;">TenantManage 的 statusType/statusLabel 内聚到组件里。</p>
      <div style="display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 16px;">
        <span>租户：</span>
        <CftStatusTag status="active" />
        <CftStatusTag status="frozen" />
        <CftStatusTag status="trial" />
      </div>
      <div style="display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 16px;">
        <span>审核：</span>
        <CftStatusTag status="pending" />
        <CftStatusTag status="pass" />
        <CftStatusTag status="reject" />
      </div>
      <div style="display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 16px;">
        <span>自定义映射：</span>
        <CftStatusTag status="foo" :label-map="{ foo: '我的状态' }" :type-map="{ foo: 'primary' }" />
      </div>
      <el-divider content-position="left">Props</el-divider>
      <PropsTable :items="propsDefs.CftStatusTag" />
    </CftCard>

    <!-- CftStatCard -->
    <CftCard title="⑥ CftStatCard · KPI 小卡片">
      <p style="color: var(--cft-text-secondary); margin: 0 0 12px;">Overview / TenantManage / Knowledge 的统计条。</p>
      <el-row :gutter="12">
        <el-col :span="6">
          <CftStatCard label="总租户数" value="126" color="#2563eb" :trend="12" />
        </el-col>
        <el-col :span="6">
          <CftStatCard label="正常开通" value="118" color="#22c55e" />
        </el-col>
        <el-col :span="6">
          <CftStatCard label="试用中" value="5" color="#f59e0b" :trend="-2" />
        </el-col>
        <el-col :span="6">
          <CftStatCard label="已冻结" value="3" color="#ef4444" icon="Warning" />
        </el-col>
      </el-row>
      <el-divider content-position="left">Props</el-divider>
      <PropsTable :items="propsDefs.CftStatCard" />
    </CftCard>

    <!-- CftConfirmButton -->
    <CftCard title="⑦ CftConfirmButton · 确认动作按钮">
      <p style="color: var(--cft-text-secondary); margin: 0 0 12px;">ProjectReview / TenantManage 的 ElMessageBox.confirm → API → ElMessage 封装。</p>
      <div style="display: flex; gap: 12px;">
        <CftConfirmButton
          btn-text="执行模拟写操作"
          btn-type="danger"
          icon="Delete"
          confirm-title="警告"
          confirm-content="确定执行此操作吗？这只是演示，不会真的写数据。"
          confirm-type="warning"
          :api-fn="demoConfirmApi"
          success-text="操作成功（demo）"
          @success="onConfirmSuccess"
        />
        <el-button size="small" link type="info" @click="demoConfirmVisible = true">看 Demo API 实现</el-button>
      </div>
      <el-divider content-position="left">Props</el-divider>
      <PropsTable :items="propsDefs.CftConfirmButton" />
    </CftCard>

    <!-- CftEmpty -->
    <CftCard title="⑧ CftEmpty · 空状态">
      <p style="color: var(--cft-text-secondary); margin: 0 0 12px;">所有列表页统一的空状态提示。</p>
      <CftEmpty description="暂无匹配数据，试试调整筛选条件" />
      <el-divider content-position="left">Props</el-divider>
      <PropsTable :items="propsDefs.CftEmpty" />
    </CftCard>

    <!-- CftDrawerDetail -->
    <CftCard title="⑨ CftDrawerDetail · 详情抽屉">
      <p style="color: var(--cft-text-secondary); margin: 0 0 12px;">ProjectReview 的 el-drawer + el-descriptions 封装。</p>
      <el-button type="primary" @click="demoDrawer = true">打开详情抽屉</el-button>
      <CftDrawerDetail v-model="demoDrawer" title="项目详情" size="500px">
        <h3 style="margin: 0 0 12px;">智能诊断医疗系统</h3>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="所属行业">医疗健康</el-descriptions-item>
          <el-descriptions-item label="融资阶段">Pre-A</el-descriptions-item>
          <el-descriptions-item label="融资金额">500 万</el-descriptions-item>
          <el-descriptions-item label="地区">北京</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">项目简介</el-divider>
        <p>基于大模型的 AI 辅助诊断系统，已在 3 家三甲医院试点使用。</p>
      </CftDrawerDetail>
      <el-divider content-position="left">Props</el-divider>
      <PropsTable :items="propsDefs.CftDrawerDetail" />
    </CftCard>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';

// CftConfirmButton 需要的 apiFn（不能是字符串，演示用实际 Promise）
const demoConfirmApi = async () => {
  await new Promise((r) => setTimeout(r, 500));
  return { ok: true };
};

const demo = reactive({
  bordered: true,
  shadowHover: true,
  keyword: '',
  status: '',
  page: 1,
  pageSize: 10,
  total: 45,
  items: Array.from({ length: 5 }).map((_, i) => ({ name: `示例项目 ${i + 1}`, value: Math.random().toFixed(2) })),
});

const demoDrawer = ref(false);
const demoConfirmVisible = ref(false);

function onConfirmSuccess(_d: any) {
  // CftConfirmButton demo 成功回调，此处仅占位
}

// ============ Props 定义表 ============
interface PropDef { name: string; type: string; default: string; desc: string; }
const propsDefs: Record<string, PropDef[]> = {
  CftCard: [
    { name: 'title', type: 'string', default: '-', desc: '卡片标题（也可用 header slot 覆盖）' },
    { name: 'bordered', type: 'boolean', default: 'true', desc: '是否显示边框' },
    { name: 'shadow', type: "'always' | 'hover' | 'never'", default: "'always'", desc: '阴影模式' },
  ],
  CftPageHeader: [
    { name: 'title', type: 'string', default: '-', desc: '页面 H1 标题（必填）' },
    { name: 'subtitle', type: 'string', default: '-', desc: '副标题/描述文字' },
    { name: 'actions', type: 'slot', default: '-', desc: '右侧操作按钮区' },
  ],
  CftFilterBar: [
    { name: 'inline', type: 'boolean', default: 'true', desc: '表单是否 inline 模式' },
    { name: 'default', type: 'slot', default: '-', desc: '内部 el-form-item 列表' },
  ],
  CftPagination: [
    { name: 'total', type: 'number', default: '-', desc: '总条数（>0 才显示）' },
    { name: 'currentPage', type: 'number', default: '1', desc: '当前页码，v-model:currentPage' },
    { name: 'pageSize', type: 'number', default: '10', desc: '每页条数，v-model:pageSize' },
    { name: 'pageSizes', type: 'number[]', default: '[10, 20, 50]', desc: '可选 pageSize' },
  ],
  CftStatusTag: [
    { name: 'status', type: 'string | number', default: '-', desc: '状态枚举值（active/frozen/pending...）' },
    { name: 'labelMap', type: 'Record<string,string>', default: '-', desc: '自定义 label 映射，覆盖默认' },
    { name: 'typeMap', type: 'Record<string,TagType>', default: '-', desc: '自定义 el-tag type 映射' },
    { name: 'size', type: "'large'|'default'|'small'", default: "'small'", desc: 'el-tag 尺寸' },
  ],
  CftStatCard: [
    { name: 'label', type: 'string', default: '-', desc: '统计项名称' },
    { name: 'value', type: 'string | number', default: '-', desc: '统计值' },
    { name: 'icon', type: 'string', default: '-', desc: 'Element Plus icon 名（可选）' },
    { name: 'color', type: 'string', default: "'#2563eb'", desc: '主色调（顶部边框/icon 背景）' },
    { name: 'trend', type: 'number', default: '-', desc: '趋势百分比，正↑负↓（可选）' },
  ],
  CftConfirmButton: [
    { name: 'confirmTitle', type: 'string', default: '-', desc: 'MessageBox 标题' },
    { name: 'confirmContent', type: 'string', default: '-', desc: 'MessageBox 内容' },
    { name: 'confirmType', type: "'warning'|'error'|'info'|'success'", default: "'warning'", desc: '图标类型' },
    { name: 'apiFn', type: '() => Promise<any>', default: '-', desc: '点击确认后执行的异步写操作' },
    { name: 'successText', type: 'string', default: '-', desc: '成功后 ElMessage 文案' },
    { name: 'btnText', type: 'string', default: "'确定'", desc: '按钮文字' },
    { name: 'btnType', type: "'primary'|'danger'|'success'|'warning'", default: "'primary'", desc: '按钮颜色' },
  ],
  CftEmpty: [
    { name: 'description', type: 'string', default: "'暂无数据'", desc: '空状态描述' },
  ],
  CftDrawerDetail: [
    { name: 'modelValue', type: 'boolean', default: '-', desc: '是否打开（v-model）' },
    { name: 'title', type: 'string', default: "'详情'", desc: '抽屉标题' },
    { name: 'size', type: 'string', default: "'600px'", desc: '抽屉宽度' },
    { name: 'direction', type: "'rtl'|'ltr'|'ttb'|'btt'", default: "'rtl'", desc: '弹出方向' },
  ],
};
</script>

<!-- Props 表小组件：直接写内嵌，避免单文件碎片化 -->
<script lang="ts">
// 这个 <script> 块只声明 PropsTable 组件供上面 template 使用
import { defineComponent, h } from 'vue';
import { ElTable, ElTableColumn } from 'element-plus';

export const PropsTable = defineComponent({
  props: { items: { type: Array as () => Array<{ name: string; type: string; default: string; desc: string }>, required: true } },
  setup(props) {
    return () => h(ElTable, { data: props.items, size: 'small', border: true }, () => [
      h(ElTableColumn, { prop: 'name', label: 'Prop', width: 160 }),
      h(ElTableColumn, { prop: 'type', label: 'Type', width: 220 }),
      h(ElTableColumn, { prop: 'default', label: 'Default', width: 180 }),
      h(ElTableColumn, { prop: 'desc', label: '说明' }),
    ]);
  },
});
</script>

<style lang="scss" scoped>
.components-preview {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
