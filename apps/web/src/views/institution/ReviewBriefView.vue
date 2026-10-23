<template>
  <div class="review-brief">
    <el-button link :icon="ArrowLeft" @click="router.back()" class="mb-4">返回项目库</el-button>

    <!-- 简报头部 -->
    <div class="brief-header">
      <div class="title-section">
        <div class="badge">AI 产业评审简报</div>
        <h1>{{ project.title }}</h1>
        <div class="meta">
          <el-tag size="small">{{ project.industry }}</el-tag>
          <el-tag size="small">{{ project.stageLabel }}</el-tag>
          <span class="sep">|</span>
          <span>💰 融资 {{ project.amount }} 万</span>
          <span class="sep">·</span>
          <span>📅 生成时间：{{ generatedAt }}</span>
        </div>
      </div>
      <div class="actions">
        <el-button @click="exportPdf"><el-icon><Download /></el-icon>导出 PDF</el-button>
        <el-button type="primary" @click="reviseVisible = true"><el-icon><Edit /></el-icon>人工修订</el-button>
      </div>
    </div>

    <!-- 综合评分 -->
    <el-row :gutter="20" class="mt-4">
      <el-col :span="6">
        <div class="score-card overall">
          <div class="label">综合评分</div>
          <div class="value">{{ overallScore }}</div>
          <el-tag :type="overallTagType">{{ overallTagText }}</el-tag>
        </div>
      </el-col>
      <el-col :span="6" v-for="dim in dimensions" :key="dim.name">
        <div class="score-card">
          <div class="label">{{ dim.name }}</div>
          <div class="value" :style="{ color: dim.color }">{{ dim.score }}</div>
          <el-progress :percentage="dim.score" :show-text="false" :color="dim.color" />
        </div>
      </el-col>
    </el-row>

    <!-- 雷达图 -->
    <el-row :gutter="20" class="mt-4">
      <el-col :span="12">
        <div class="cft-card">
          <h3>📊 五维雷达分析</h3>
          <v-chart class="radar" :option="radarOption" autoresize />
        </div>
      </el-col>
      <el-col :span="12">
        <div class="cft-card">
          <h3>💡 核心亮点</h3>
          <div class="section-body">
            <div v-for="(h, i) in highlights" :key="i" class="hl-item">
              <el-icon color="#22c55e"><Check /></el-icon>
              <span>{{ h }}</span>
            </div>
          </div>
          <el-divider />
          <h3>⚠️ 主要风险点</h3>
          <div class="section-body">
            <div v-for="(r, i) in risks" :key="i" class="risk-item">
              <el-icon color="#f59e0b"><Warning /></el-icon>
              <span>{{ r }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 详细评审内容 -->
    <div class="cft-card mt-4">
      <div class="brief-section" v-for="(section, i) in sections" :key="i">
        <div class="section-title">
          <el-icon><Document /></el-icon>
          <span>{{ section.title }}</span>
          <el-tag size="small" :type="section.score >= 70 ? 'success' : 'warning'">AI评分 {{ section.score }}</el-tag>
        </div>
        <p class="section-text">{{ section.content }}</p>
      </div>
    </div>

    <!-- 投资建议 -->
    <div class="cft-card invest-recommend mt-4">
      <h3>🎯 AI 投资建议</h3>
      <div class="recommend-banner">
        <div class="rec-score">
          <el-progress type="dashboard" :percentage="recommendScore" :width="100" :color="recColor" />
        </div>
        <div class="rec-content">
          <div class="rec-title" :style="{ color: recColor }">{{ recommendText }}</div>
          <p class="rec-desc">综合各维度评估，该项目具备 {{ recommendScore >= 75 ? '较强' : '一定' }}的投资价值。建议深入尽调重点关注：{{ recommendFocus }}</p>
          <div class="rec-actions">
            <el-button type="primary" @click="router.push('/institution/roadshows')">安排对接</el-button>
            <el-button :type="collected ? 'warning' : ''" @click="toggleCollect">{{ collected ? '已收藏 ✓' : '加入收藏' }}</el-button>
            <el-button @click="ddVisible = true">生成尽调清单</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 人工修订弹窗 -->
    <el-dialog v-model="reviseVisible" title="人工修订评审内容" width="640px">
      <el-tabs v-model="reviseTab">
        <el-tab-pane v-for="(section, i) in sections" :key="i" :label="section.title" :name="String(i)">
          <el-input v-model="section.content" type="textarea" :rows="8" />
          <div class="revise-score">
            <span>AI 评分微调：</span>
            <el-input-number v-model="section.score" :min="0" :max="100" size="small" />
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="reviseVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRevision">保存修订</el-button>
      </template>
    </el-dialog>

    <!-- 尽调清单弹窗 -->
    <el-dialog v-model="ddVisible" title="尽调清单（AI 生成）" width="560px">
      <el-checkbox-group v-model="ddChecked" class="dd-list">
        <el-checkbox v-for="item in ddItems" :key="item" :value="item" class="dd-item">{{ item }}</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <span class="dd-progress">已完成 {{ ddChecked.length }} / {{ ddItems.length }}</span>
        <el-button @click="ddVisible = false">关闭</el-button>
        <el-button type="primary" @click="exportDd">导出清单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Download, Edit, Document, Check, Warning } from '@element-plus/icons-vue';

const router = useRouter();
const generatedAt = '2026-10-19 14:30';

const reviseVisible = ref(false);
const reviseTab = ref('0');
const ddVisible = ref(false);
const ddChecked = ref<string[]>([]);
const collected = ref(false);

const ddItems = [
  '核实创始团队背景与履历真实性（含期权激励方案）',
  '查验 12 家付费客户合同原件与回款记录',
  '技术尽调：多 Agent 框架 benchmark 复测与代码审查',
  '财务尽调：近三年财务报表、银行流水与纳税记录',
  '法律尽调：工商信息、知识产权归属、涉诉情况',
  '竞品对标：与 Microsoft Copilot、AutoGPT 的差异化验证',
  '客户访谈：至少 3 家头部客户的使用满意度回访',
  '数据合规：企业数据隐私与私有化部署方案评估',
];

const project = {
  title: '下一代 AI Agent 协作平台',
  industry: '人工智能',
  stageLabel: 'Pre-Seed',
  amount: 300,
};

const overallScore = 82;
const overallTagText = computed(() => overallScore >= 80 ? '强烈推荐' : overallScore >= 70 ? '值得关注' : '谨慎');
const overallTagType = computed(() => overallScore >= 80 ? 'success' : overallScore >= 70 ? 'warning' : 'danger');

const dimensions = [
  { name: '团队实力', score: 85, color: '#22c55e' },
  { name: '技术壁垒', score: 88, color: '#22c55e' },
  { name: '市场空间', score: 80, color: '#22c55e' },
  { name: '商业模式', score: 72, color: '#f59e0b' },
  { name: '财务健康', score: 70, color: '#f59e0b' },
];

const highlights = [
  '核心团队来自百度、阿里 AI 部门，有完整的大厂 AI 产品研发经验',
  '自研多 Agent 编排框架，在公开 benchmark 上优于竞品 30%+',
  '已签约 12 家付费企业客户，其中 3 家为行业头部客户',
  '商业模式清晰的 SaaS 订阅制，客户留存率达 92%',
];
const risks = [
  '目前营收体量较小，需要验证规模化增长能力',
  '核心技术人员流失风险，建议查看期权激励方案',
  '竞品如 OpenAI、Microsoft Copilot 等巨头的潜在竞争压力',
];

const sections = reactive([
  { title: '团队背景分析', score: 85, content: '创始人及核心团队均来自一线科技公司，在 AI Agent、大模型应用、企业级 SaaS 领域拥有丰富经验。CEO 前身为百度智能云 AI 产品线负责人，CTO 曾任阿里巴巴达摩院高级算法专家。团队配置完整，技术、产品、商业化均有核心负责人。' },
  { title: '技术与产品', score: 88, content: '公司核心技术为自研的多 Agent 协作编排框架，支持多种大模型后端灵活切换。在企业级场景下，单个 Agent 可独立完成 80%+ 的业务任务，多 Agent 协同任务完成率较单 Agent 提升 3 倍。产品已完成从 MVP 到商业化版本的迭代，具备良好的产品完成度。' },
  { title: '市场与竞争', score: 80, content: '企业级 AI Agent 市场处于快速增长期，预计 2027 年全球市场规模超过 500 亿美元。公司定位清晰，聚焦垂直场景下的深度方案，与通用大模型形成差异化。主要竞品为 Microsoft Copilot、AutoGPT 系列等，但公司在私有化部署、行业定制方面具有优势。' },
  { title: '商业模式与财务', score: 72, content: '公司采用 SaaS 订阅 + 项目制的混合模式。SaaS 订阅按席位收费，项目制为定制化解决方案。目前年经常性收入（ARR）约 500 万元人民币，处于早期阶段。毛利率约 65%，接近成熟 SaaS 公司水平。' },
]);

const radarOption = computed(() => ({
  tooltip: {},
  radar: { indicator: dimensions.map(d => ({ name: d.name, max: 100 })), shape: 'polygon' },
  series: [{
    type: 'radar',
    data: [{
      value: dimensions.map(d => d.score),
      name: '该项目',
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#2563eb' },
    }, {
      value: [75, 72, 78, 70, 68],
      name: '同行业均值',
      lineStyle: { type: 'dashed' },
      itemStyle: { color: '#9ca3af' },
    }],
  }],
  legend: { bottom: 0 },
}));

const recommendScore = 78;
const recColor = computed(() => recommendScore >= 75 ? '#22c55e' : '#f59e0b');
const recommendText = computed(() => recommendScore >= 75 ? '✅ 建议积极推进' : '⚖️ 有条件推进');
const recommendFocus = '团队稳定性、客户留存深度、竞品动态';

function exportPdf() {
  ElMessage.success('评审简报 PDF 已生成：review_brief_1019.pdf（演示 Mock）');
}

function saveRevision() {
  reviseVisible.value = false;
  ElMessage.success('修订已保存，简报版本号 +1（演示 Mock）');
}

function toggleCollect() {
  collected.value = !collected.value;
  ElMessage.success(collected.value ? '已加入收藏' : '已取消收藏');
}

function exportDd() {
  ElMessage.success(`尽调清单已导出（${ddChecked.value.length}/${ddItems.length} 项已勾选，演示 Mock）`);
}
</script>

<style lang="scss" scoped>
.review-brief {
  .brief-header {
    background: white; border-radius: 12px; padding: 24px;
    border: 1px solid var(--cft-border);
    display: flex; justify-content: space-between; align-items: flex-start;
    .badge { display: inline-block; padding: 4px 12px; background: linear-gradient(135deg, #2563eb, #7c3aed); color: white; border-radius: 20px; font-size: 12px; font-weight: 500; margin-bottom: 12px; }
    h1 { margin: 0 0 8px; font-size: 22px; }
    .meta { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--cft-text-secondary); .sep { color: var(--cft-border); } }
    .actions { display: flex; gap: 10px; }
  }
  .score-card { background: white; border-radius: 12px; padding: 20px; border: 1px solid var(--cft-border); text-align: center; .label { font-size: 13px; color: var(--cft-text-muted); } .value { font-size: 32px; font-weight: 700; color: var(--cft-primary); margin: 4px 0 8px; } &.overall { background: linear-gradient(135deg, #eff6ff, #dbeafe); border-color: #bfdbfe; .value { font-size: 48px; color: var(--cft-primary); } } }
  .radar { width: 100%; height: 280px; }
  .section-body { display: flex; flex-direction: column; gap: 10px; }
  .hl-item, .risk-item { display: flex; align-items: flex-start; gap: 8px; font-size: 13px; color: var(--cft-text-primary); line-height: 1.6; }
  .brief-section { padding: 20px 0; border-bottom: 1px solid var(--cft-border); &:last-child { border: none; } }
  .section-title { display: flex; align-items: center; gap: 8px; font-weight: 600; margin-bottom: 12px; font-size: 15px; }
  .section-text { font-size: 14px; color: var(--cft-text-secondary); line-height: 1.9; margin: 0; }
  .invest-recommend { background: linear-gradient(135deg, #f0fdf4, #dcfce7); border-color: #86efac; }
  .recommend-banner { display: flex; gap: 24px; align-items: flex-start; padding-top: 8px; }
  .rec-content { flex: 1; .rec-title { font-size: 18px; font-weight: 600; margin-bottom: 8px; } .rec-desc { font-size: 14px; color: var(--cft-text-secondary); line-height: 1.8; margin: 0 0 16px; } .rec-actions { display: flex; gap: 10px; } }
  .mt-4 { margin-top: 16px; }
  .mb-4 { margin-bottom: 16px; }
  .revise-score { display: flex; align-items: center; gap: 10px; margin-top: 12px; font-size: 13px; color: var(--cft-text-secondary); }
  .dd-list { display: flex; flex-direction: column; gap: 12px; .dd-item { align-items: flex-start; height: auto; :deep(.el-checkbox__label) { line-height: 1.6; } } }
  .dd-progress { float: left; font-size: 13px; color: var(--cft-text-muted); line-height: 32px; }
}
</style>
