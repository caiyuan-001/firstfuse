<template>
  <div class="project-library">
    <div class="page-header">
      <h1>项目库</h1>
      <p>共 <strong>{{ filteredList.length }}</strong> 个项目，支持多维度筛选、收藏、AI 生成评审简报</p>
    </div>

    <el-row :gutter="16">
      <!-- 左侧筛选 -->
      <el-col :span="6">
        <div class="cft-card filter-panel">
          <h3>🔍 筛选</h3>
          <el-form :model="filter" label-position="top" size="default">
            <el-form-item label="关键词">
              <el-input v-model="filter.keyword" placeholder="项目名称/描述" clearable />
            </el-form-item>
            <el-form-item label="行业">
              <el-select v-model="filter.industries" multiple placeholder="全部" class="w-full">
                <el-option v-for="i in industryOptions" :key="i.value" :label="i.label" :value="i.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="投资阶段">
              <el-checkbox-group v-model="filter.stages">
                <el-checkbox value="pre_seed">Pre-Seed</el-checkbox>
                <el-checkbox value="angel">天使轮</el-checkbox>
                <el-checkbox value="seed">种子轮</el-checkbox>
                <el-checkbox value="series_a">A轮</el-checkbox>
                <el-checkbox value="series_b">B轮</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="融资金额（万元）">
              <el-slider v-model="filter.amountRange" :min="0" :max="10000" :step="100" range show-input />
            </el-form-item>
            <el-form-item label="地区">
              <el-select v-model="filter.regions" multiple placeholder="全部" class="w-full">
                <el-option v-for="r in regionOptions" :key="r.value" :label="r.label" :value="r.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button class="w-full" :disabled="!hasFilter" @click="resetFilter">重置筛选</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <!-- 右侧列表 -->
      <el-col :span="18">
        <div class="result-toolbar">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button value="list">列表视图</el-radio-button>
            <el-radio-button value="card">卡片视图</el-radio-button>
          </el-radio-group>
          <div class="toolbar-right">
            <el-tag v-if="hasFilter" size="small" type="warning" effect="plain" class="mr-2">已应用筛选</el-tag>
            <el-select v-model="sortBy" size="small" style="width: 160px">
              <el-option label="最新发布" value="newest" />
              <el-option label="浏览最多" value="viewed" />
              <el-option label="融资金额最大" value="amount" />
              <el-option label="匹配度最高" value="match" />
            </el-select>
          </div>
        </div>

        <el-empty v-if="pagedList.length === 0" description="没有符合条件的项目，试试调整筛选条件" />

        <!-- 列表视图 -->
        <div v-else-if="viewMode === 'list'">
          <div v-for="p in pagedList" :key="p.id" class="project-row" @click="openDetail(p)">
            <div class="p-main">
              <div class="p-title">
                <el-link type="primary">{{ p.title }}</el-link>
                <el-tag :type="matchTag(p.matchScore)" size="small" class="ml-2">匹配度 {{ p.matchScore }}%</el-tag>
                <el-tag size="small">{{ p.stageLabel }}</el-tag>
              </div>
              <div class="p-desc">{{ p.shortDesc }}</div>
              <div class="p-meta">
                <span>🏷️ {{ p.industry }}</span>
                <span>📍 {{ p.region }}</span>
                <span>💰 融资 {{ p.amount }} 万</span>
                <span>👁️ {{ p.viewCount }} 次浏览</span>
              </div>
            </div>
            <div class="p-actions">
              <el-button :icon="Star" circle :type="p.favorited ? 'warning' : ''" @click.stop="toggleFavorite(p)" />
              <el-button :icon="MagicStick" circle type="primary" @click.stop="generateBrief(p)" />
            </div>
          </div>
        </div>

        <!-- 卡片视图 -->
        <el-row v-else :gutter="16">
          <el-col :span="8" v-for="p in pagedList" :key="p.id">
            <div class="project-card" @click="openDetail(p)">
              <div class="pc-header">
                <el-tag :type="matchTag(p.matchScore)" size="small">匹配 {{ p.matchScore }}%</el-tag>
                <el-button :icon="Star" link :type="p.favorited ? 'warning' : 'info'" @click.stop="toggleFavorite(p)" />
              </div>
              <h3>{{ p.title }}</h3>
              <p class="pc-desc">{{ p.shortDesc }}</p>
              <div class="pc-tags">
                <el-tag size="small" effect="plain">{{ p.industry }}</el-tag>
                <el-tag size="small" effect="plain">{{ p.stageLabel }}</el-tag>
              </div>
              <div class="pc-footer">
                <span>💰 {{ p.amount }}万</span>
                <el-button size="small" type="primary" link>查看详情</el-button>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="filteredList.length"
          layout="prev, pager, next, total"
          class="pagination mt-4"
          hide-on-single-page
        />
      </el-col>
    </el-row>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="项目详情" size="560px" direction="rtl">
      <div v-if="currentProject">
        <h2>{{ currentProject.title }}</h2>
        <el-tag size="small" class="mb-2">{{ currentProject.stageLabel }} · {{ currentProject.amount }}万</el-tag>
        <el-descriptions :column="1" border size="small" class="mt-3">
          <el-descriptions-item label="行业">{{ currentProject.industry }}</el-descriptions-item>
          <el-descriptions-item label="地区">{{ currentProject.region }}</el-descriptions-item>
          <el-descriptions-item label="估值">{{ currentProject.valuation }}万</el-descriptions-item>
          <el-descriptions-item label="出让股权">{{ currentProject.equityOffered }}%</el-descriptions-item>
          <el-descriptions-item label="成立时间">{{ currentProject.foundedAt }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <h4>项目简介</h4>
        <p class="long-desc">{{ currentProject.longDesc }}</p>
        <el-divider />
        <div class="drawer-actions">
          <el-button type="primary" @click="generateBrief(currentProject)"><el-icon><MagicStick /></el-icon>生成 AI 评审简报</el-button>
          <el-button :type="currentProject.favorited ? 'warning' : ''" @click="toggleFavorite(currentProject)">
            <el-icon><Star /></el-icon>{{ currentProject.favorited ? '取消收藏' : '收藏项目' }}
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Star, MagicStick } from '@element-plus/icons-vue';
import { INDUSTRY_OPTIONS, REGION_OPTIONS } from '@chuangrongtong/shared';

const router = useRouter();
const industryOptions = INDUSTRY_OPTIONS;
const regionOptions = REGION_OPTIONS;
const viewMode = ref<'list' | 'card'>('list');
const sortBy = ref('newest');
const page = ref(1);
const pageSize = 5;
const detailVisible = ref(false);
const currentProject = ref<any>(null);

const filter = reactive({
  keyword: '',
  industries: [] as string[],
  stages: [] as string[],
  amountRange: [0, 10000] as [number, number],
  regions: [] as string[],
});

interface LibraryProject {
  id: string; title: string; matchScore: number; favorited: boolean;
  stageLabel: string; stageKey: string; industry: string; region: string;
  amount: number; viewCount: number; shortDesc: string; longDesc: string;
  valuation: number; equityOffered: number; foundedAt: string; publishedAt: string;
}

const allProjects = ref<LibraryProject[]>([
  { id: '1', title: '下一代 AI Agent 协作平台', matchScore: 92, favorited: false, stageLabel: 'Pre-Seed', stageKey: 'pre_seed', industry: '人工智能', region: '上海', amount: 300, viewCount: 1256, publishedAt: '2026-10-18', shortDesc: '面向企业级的 AI Agent 协作编排平台，支持多 Agent 协同完成复杂业务流程。', longDesc: '本项目是一个面向企业级用户的 AI Agent 协作编排平台，核心创新在于通过可视化的流程编排，让多个 AI Agent 能够协同工作，共同完成复杂的业务任务。已签约 12 家付费企业客户，复购率 92%。', valuation: 3000, equityOffered: 15, foundedAt: '2025-06' },
  { id: '2', title: '固态电池关键材料技术', matchScore: 87, favorited: true, stageLabel: '种子轮', stageKey: 'seed', industry: '新能源', region: '深圳', amount: 1200, viewCount: 980, publishedAt: '2026-10-16', shortDesc: '新型硫化物固态电解质材料，解决传统液态电池安全性问题。', longDesc: '公司研发的硫化物固态电解质材料，能量密度达到 450Wh/kg，循环寿命超过 1000 次，已通过两家头部电池厂中试验证。', valuation: 8000, equityOffered: 20, foundedAt: '2024-03' },
  { id: '3', title: '智能影像诊断 SaaS', matchScore: 85, favorited: false, stageLabel: 'A轮', stageKey: 'series_a', industry: '医疗健康', region: '北京', amount: 5000, viewCount: 3420, publishedAt: '2026-10-12', shortDesc: '基于深度学习的医学影像辅助诊断系统，覆盖 CT/MRI/X 光。', longDesc: '公司产品覆盖放射科、病理科等多个科室，AI 辅助诊断准确率达到三甲医院专家水平，已落地 200+ 医院，ARR 突破 3000 万。', valuation: 30000, equityOffered: 10, foundedAt: '2022-08' },
  { id: '4', title: '工业机器人核心控制器', matchScore: 79, favorited: false, stageLabel: 'Pre-Seed', stageKey: 'pre_seed', industry: '机器人', region: '苏州', amount: 500, viewCount: 456, publishedAt: '2026-10-10', shortDesc: '自主研发的六轴工业机器人控制器，打破国外垄断。', longDesc: '核心团队来自 KUKA、ABB 等国际机器人巨头，拥有完整的控制器正向研发能力，样机已在 3 家灯塔工厂完成测试。', valuation: 2500, equityOffered: 18, foundedAt: '2025-09' },
  { id: '5', title: '企业级向量数据库', matchScore: 88, favorited: true, stageLabel: 'A轮', stageKey: 'series_a', industry: '企业软件/SaaS', region: '杭州', amount: 3000, viewCount: 2100, publishedAt: '2026-10-08', shortDesc: '国产高性能向量数据库，支持千亿级向量毫秒级检索。', longDesc: '团队在数据库领域深耕多年，产品已在多家头部客户完成 PoC 验证，性能对标 Milvus，兼容主流 AI 框架生态。', valuation: 20000, equityOffered: 12, foundedAt: '2023-05' },
  { id: '6', title: '第三代半导体氮化镓器件', matchScore: 74, favorited: false, stageLabel: 'B轮', stageKey: 'series_b', industry: '半导体/芯片', region: '南京', amount: 8000, viewCount: 1678, publishedAt: '2026-10-05', shortDesc: '650V 氮化镓功率器件量产，应用于快充与数据中心电源。', longDesc: '公司已建成 6 英寸 GaN-on-Si 量产线，良率稳定在 92% 以上，客户覆盖头部快充品牌与服务器电源厂商。', valuation: 60000, equityOffered: 8, foundedAt: '2021-11' },
  { id: '7', title: '可降解生物基材料', matchScore: 68, favorited: false, stageLabel: '天使轮', stageKey: 'angel', industry: '新材料', region: '成都', amount: 800, viewCount: 320, publishedAt: '2026-09-28', shortDesc: '以玉米淀粉为原料的全降解包装材料，成本接近传统塑料。', longDesc: '团队来自中科院材料所，突破低成本改性工艺，已获得两项发明专利，正在建设千吨级产线。', valuation: 4000, equityOffered: 20, foundedAt: '2025-01' },
]);

const selectedIndustryLabels = computed(() =>
  industryOptions.filter((o) => filter.industries.includes(o.value)).map((o) => o.label),
);
const selectedRegionLabels = computed(() =>
  regionOptions.filter((o) => filter.regions.includes(o.value)).map((o) => o.label),
);

const hasFilter = computed(
  () =>
    !!filter.keyword ||
    filter.industries.length > 0 ||
    filter.stages.length > 0 ||
    filter.regions.length > 0 ||
    filter.amountRange[0] > 0 ||
    filter.amountRange[1] < 10000,
);

const filteredList = computed(() => {
  let list = allProjects.value.filter((p) => {
    if (filter.keyword && !p.title.includes(filter.keyword) && !p.shortDesc.includes(filter.keyword)) return false;
    if (selectedIndustryLabels.value.length && !selectedIndustryLabels.value.includes(p.industry)) return false;
    if (filter.stages.length && !filter.stages.includes(p.stageKey)) return false;
    if (p.amount < filter.amountRange[0] || p.amount > filter.amountRange[1]) return false;
    if (selectedRegionLabels.value.length && !selectedRegionLabels.value.includes(p.region)) return false;
    return true;
  });
  switch (sortBy.value) {
    case 'viewed':
      list = [...list].sort((a, b) => b.viewCount - a.viewCount);
      break;
    case 'amount':
      list = [...list].sort((a, b) => b.amount - a.amount);
      break;
    case 'match':
      list = [...list].sort((a, b) => b.matchScore - a.matchScore);
      break;
    default:
      list = [...list].sort((a, b) => b.publishedAt.localeCompare(a.publishedAt));
  }
  return list;
});

const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredList.value.slice(start, start + pageSize);
});

watch(filteredList, () => {
  if ((page.value - 1) * pageSize >= filteredList.value.length) page.value = 1;
});

function matchTag(score: number) {
  if (score >= 90) return 'success';
  if (score >= 80) return 'warning';
  return 'info';
}

function openDetail(p: any) {
  currentProject.value = p;
  detailVisible.value = true;
}

function toggleFavorite(p: any) {
  p.favorited = !p.favorited;
  ElMessage.success(p.favorited ? `已收藏「${p.title}」` : '已取消收藏');
}

function generateBrief(p: any) {
  ElMessage.success(`正在为「${p.title}」生成 AI 评审简报...`);
  detailVisible.value = false;
  setTimeout(() => router.push(`/institution/review/${p.id}`), 600);
}

function resetFilter() {
  filter.keyword = ''; filter.industries = []; filter.stages = []; filter.regions = []; filter.amountRange = [0, 10000];
  page.value = 1;
}
</script>

<style lang="scss" scoped>
.project-library {
  .page-header { margin-bottom: 20px; h1 { margin: 0 0 4px; font-size: 24px; } p { margin: 0; color: var(--cft-text-secondary); font-size: 14px; } }
  .filter-panel { h3 { margin: 0 0 16px; font-size: 15px; } }
  .result-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; .toolbar-right { display: flex; align-items: center; } }
  .project-row {
    background: white; border: 1px solid var(--cft-border); border-radius: 10px;
    padding: 18px 20px; margin-bottom: 12px; cursor: pointer;
    display: flex; justify-content: space-between; align-items: center;
    transition: all 0.2s;
    &:hover { border-color: var(--cft-primary); box-shadow: 0 4px 16px rgba(37,99,235,0.08); }
  }
  .p-title { display: flex; align-items: center; margin-bottom: 6px; }
  .p-desc { color: var(--cft-text-secondary); font-size: 13px; margin: 0 0 8px; }
  .p-meta { display: flex; gap: 20px; font-size: 12px; color: var(--cft-text-muted); }
  .p-actions { display: flex; gap: 8px; }
  .project-card {
    background: white; border: 1px solid var(--cft-border); border-radius: 10px;
    padding: 18px; margin-bottom: 16px; cursor: pointer; height: 100%;
    transition: all 0.2s;
    &:hover { border-color: var(--cft-primary); box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
    .pc-header { display: flex; justify-content: space-between; align-items: center; }
    h3 { margin: 10px 0 8px; font-size: 15px; }
    .pc-desc { font-size: 12px; color: var(--cft-text-secondary); line-height: 1.6; height: 38px; overflow: hidden; margin: 0 0 10px; }
    .pc-tags { display: flex; gap: 6px; margin-bottom: 12px; }
    .pc-footer { display: flex; justify-content: space-between; align-items: center; font-size: 13px; color: var(--cft-text-muted); }
  }
  .drawer-actions { display: flex; gap: 12px; }
  .long-desc { font-size: 13px; color: var(--cft-text-secondary); line-height: 1.8; }
  .w-full { width: 100%; }
  .ml-2 { margin-left: 8px; }
  .mr-2 { margin-right: 8px; }
  .mt-3 { margin-top: 12px; }
  .mt-4 { margin-top: 16px; }
  .mb-2 { margin-bottom: 8px; }
  .pagination { display: flex; justify-content: flex-end; }
}
</style>
