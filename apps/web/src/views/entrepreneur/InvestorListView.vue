<template>
  <div class="investor-list">
    <div class="page-header">
      <h1>找投资人</h1>
      <p>覆盖 500+ 头部投资机构，按行业、阶段、地区筛选最匹配的机构</p>
    </div>

    <!-- 筛选区 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filter" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="filter.keyword" placeholder="机构名称/合伙人" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="关注行业">
          <el-select v-model="filter.industry" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="i in industryOptions" :key="i.value" :label="i.label" :value="i.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="投资阶段">
          <el-select v-model="filter.stage" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="s in stageOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="地区">
          <el-select v-model="filter.region" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="r in regionOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search">搜索</el-button>
          <el-button :icon="Refresh" @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 结果列表 -->
    <div class="result-stats">
      共 <strong>{{ total }}</strong> 家机构
    </div>

    <el-row :gutter="16">
      <el-col :span="8" :lg="8" :md="12" :sm="24" v-for="inst in list" :key="inst.id">
        <div class="inst-card" @click="router.push(`/entrepreneur/investors/${inst.id}`)">
          <div class="inst-header">
            <div class="inst-logo">{{ inst.logoText }}</div>
            <div class="inst-info">
              <h3 class="inst-name">{{ inst.name }}</h3>
              <div class="inst-meta">{{ inst.region }} · {{ inst.aum }}</div>
            </div>
            <el-button size="small" type="primary" round>投递</el-button>
          </div>
          <div class="inst-focus">
            <span class="focus-label">关注：</span>
            <el-tag v-for="ind in inst.focusIndustries" :key="ind" size="small" effect="plain">{{ ind }}</el-tag>
          </div>
          <div class="inst-tags">
            <el-tag v-for="s in inst.preferredStages" :key="s" size="small" type="info">{{ s }}</el-tag>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-pagination
      v-model:current-page="page"
      :page-size="12"
      :total="total"
      layout="prev, pager, next, jumper"
      class="pagination"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { Search, Refresh } from '@element-plus/icons-vue';
import { INDUSTRY_OPTIONS, REGION_OPTIONS, STAGE_OPTIONS } from '@chuangrongtong/shared';

const router = useRouter();
const page = ref(1);
const total = ref(236);
const industryOptions = INDUSTRY_OPTIONS;
const regionOptions = REGION_OPTIONS;
const stageOptions = STAGE_OPTIONS;

const filter = reactive({ keyword: '', industry: '', stage: '', region: '' });

const list = [
  { id: '1', name: '红杉中国', logoText: '🌲', region: '上海', aum: '管理规模 ¥500亿+', focusIndustries: ['人工智能', '硬科技', '企业软件'], preferredStages: ['Pre-Seed', 'A轮', 'B轮'] },
  { id: '2', name: '达晨创投', logoText: '🏦', region: '深圳', aum: '管理规模 ¥300亿+', focusIndustries: ['医疗健康', '半导体', '新能源'], preferredStages: ['种子轮', 'A轮'] },
  { id: '3', name: 'IDG资本', logoText: '💡', region: '北京', aum: '管理规模 ¥400亿+', focusIndustries: ['人工智能', '消费科技', '金融科技'], preferredStages: ['天使轮', 'A轮', 'B轮'] },
  { id: '4', name: '高瓴创投', logoText: '📈', region: '北京', aum: '管理规模 ¥600亿+', focusIndustries: ['生物医药', '硬科技', '新能源'], preferredStages: ['A轮', 'B轮', 'Pre-IPO'] },
  { id: '5', name: '经纬创投', logoText: '🎯', region: '北京', aum: '管理规模 ¥250亿+', focusIndustries: ['人工智能', '企业软件', '消费'], preferredStages: ['Pre-Seed', '种子轮', 'A轮'] },
  { id: '6', name: '启明创投', logoText: '🚀', region: '上海', aum: '管理规模 ¥200亿+', focusIndustries: ['医疗健康', '半导体', '企业服务'], preferredStages: ['种子轮', 'A轮', 'B轮'] },
];

function resetFilter() {
  filter.keyword = ''; filter.industry = ''; filter.stage = ''; filter.region = '';
}
</script>

<style lang="scss" scoped>
.investor-list {
  .page-header { margin-bottom: 20px; h1 { margin: 0 0 4px; font-size: 24px; } p { margin: 0; color: var(--cft-text-secondary); font-size: 14px; } }
  .filter-card { margin-bottom: 16px; }
  .result-stats { font-size: 14px; color: var(--cft-text-secondary); margin-bottom: 12px; }
  .inst-card {
    background: white; border: 1px solid var(--cft-border); border-radius: 12px;
    padding: 20px; margin-bottom: 16px; cursor: pointer;
    transition: all 0.2s; height: 100%;
    &:hover { border-color: var(--cft-primary); box-shadow: 0 8px 24px rgba(37, 99, 235, 0.1); }
  }
  .inst-header { display: flex; gap: 12px; align-items: flex-start; margin-bottom: 16px; .inst-logo { width: 48px; height: 48px; border-radius: 12px; background: var(--cft-bg); display: flex; align-items: center; justify-content: center; font-size: 24px; flex-shrink: 0; } .inst-name { margin: 0; font-size: 16px; font-weight: 600; } .inst-meta { font-size: 12px; color: var(--cft-text-muted); margin-top: 2px; } .inst-info { flex: 1; min-width: 0; } }
  .inst-focus { font-size: 12px; margin-bottom: 10px; .focus-label { color: var(--cft-text-muted); margin-right: 4px; } }
  .inst-tags { display: flex; flex-wrap: wrap; gap: 4px; }
  .pagination { display: flex; justify-content: center; margin-top: 24px; }
}

@media (max-width: 1024px) {
  .investor-list {
    .filter-form {
      :deep(.el-form-item) { margin-bottom: 10px; }
      :deep(.el-input), :deep(.el-select) { max-width: 100%; }
    }
  }
}
</style>
