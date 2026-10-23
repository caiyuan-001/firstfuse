<template>
  <div class="diagnosis-page">
    <div class="page-header">
      <h1>AI 融资诊断</h1>
      <p>AI 将从商业模式、团队、市场、财务、技术壁垒等维度对您的项目进行全方位融资健康度诊断</p>
    </div>

    <!-- 问卷/结果切换 -->
    <div class="diagnosis-content">
      <!-- 雷达图结果 -->
      <el-row :gutter="24">
        <el-col :span="10" :lg="10" :md="12" :sm="24">
          <div class="cft-card chart-card">
            <h3>综合诊断报告</h3>
            <div class="score-wrap">
              <div class="score-circle">
                <div class="score-value">{{ report.overallScore }}</div>
                <div class="score-label">融资健康度</div>
              </div>
            </div>
            <el-divider />
            <el-tag :type="scoreLevel" size="large">{{ scoreText }}</el-tag>
            <p class="score-desc">{{ scoreDesc }}</p>
            <el-button type="primary" class="w-full" @click="generateReport">
              <el-icon><RefreshRight /></el-icon>
              重新诊断
            </el-button>
          </div>
        </el-col>

        <el-col :span="14" :lg="14" :md="12" :sm="24">
          <div class="cft-card">
            <h3>五维雷达分析</h3>
            <v-chart class="radar-chart" :option="radarOption" autoresize />
          </div>
        </el-col>
      </el-row>

      <!-- 各维度详情 -->
      <div class="cft-card dimensions-card mt-4">
        <h3>各维度详情评分</h3>
        <el-row :gutter="16">
          <el-col v-for="dim in report.dimensions" :key="dim.name" :span="8" :lg="8" :md="12" :sm="24">
            <div class="dim-item">
              <div class="dim-header">
                <span class="dim-name">{{ dim.name }}</span>
                <span class="dim-score">{{ dim.score }}/{{ dim.fullMark }}</span>
              </div>
              <el-progress :percentage="(dim.score / dim.fullMark) * 100" :color="getColor(dim.score, dim.fullMark)" />
              <p class="dim-desc">{{ dim.description }}</p>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- AI 建议 -->
      <div class="cft-card mt-4">
        <h3>💡 AI 改进建议</h3>
        <div class="ai-suggestions">
          <div v-for="(s, i) in suggestions" :key="i" class="suggestion-item">
            <div class="s-index">{{ i + 1 }}</div>
            <div class="s-content">
              <h4>{{ s.title }}</h4>
              <p>{{ s.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { RefreshRight } from '@element-plus/icons-vue';
import type { DiagnosisDimension } from '@chuangrongtong/shared';

const report = ref({
  overallScore: 78,
  dimensions: [
    { name: '商业模式', score: 82, fullMark: 100, description: '清晰的 SaaS 订阅模式，客户留存率高' },
    { name: '团队实力', score: 75, fullMark: 100, description: '核心团队有行业经验，建议补充 BD 负责人' },
    { name: '市场空间', score: 85, fullMark: 100, description: '市场规模足够大，行业处于上升期' },
    { name: '财务状况', score: 68, fullMark: 100, description: '营收增长良好，但毛利率有待提升' },
    { name: '技术壁垒', score: 80, fullMark: 100, description: '核心算法有一定壁垒，建议加速专利布局' },
  ] as DiagnosisDimension[],
});

const suggestions = [
  { title: '补充 BD 负责人', content: '当前团队缺少 BD 负责人，投资人通常非常看重销售能力的完整性' },
  { title: '优化毛利率', content: '当前毛利率 55%，建议通过产品标准化和自动化提升至 65%+' },
  { title: '强化技术壁垒叙事', content: '建议在 BP 中更清晰地说明核心算法的独特性和专利布局计划' },
];

const radarOption = computed(() => {
  const dims = report.value.dimensions;
  return {
    tooltip: {},
    radar: {
      indicator: dims.map(d => ({ name: d.name, max: 100 })),
      shape: 'polygon',
    },
    series: [{
      type: 'radar',
      data: [{
        value: dims.map(d => d.score),
        name: '当前水平',
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#2563eb' },
        lineStyle: { color: '#2563eb', width: 2 },
      }],
    }],
  };
});

const scoreLevel = computed(() => {
  const s = report.value.overallScore;
  if (s >= 85) return 'success';
  if (s >= 70) return 'warning';
  return 'danger';
});

const scoreText = computed(() => {
  const s = report.value.overallScore;
  if (s >= 85) return '非常优秀';
  if (s >= 70) return '良好，有改进空间';
  return '需要重点优化';
});

const scoreDesc = computed(() => {
  const s = report.value.overallScore;
  if (s >= 85) return '您的项目整体质量优秀，具备较好的融资竞争力。';
  if (s >= 70) return '您的项目整体不错，但某些维度还有明显提升空间，优化后会更有竞争力。';
  return '您的项目在多个维度存在短板，建议先优化再启动融资。';
});

function getColor(score: number, full: number) {
  const ratio = score / full;
  if (ratio >= 0.85) return '#22c55e';
  if (ratio >= 0.7) return '#f59e0b';
  return '#ef4444';
}

async function generateReport() {
  report.value.overallScore = Math.floor(Math.random() * 20) + 70;
  report.value.dimensions.forEach(d => {
    d.score = Math.floor(Math.random() * 25) + 60;
  });
}
</script>

<style lang="scss" scoped>
.diagnosis-page {
  .page-header { margin-bottom: 24px; h1 { margin: 0 0 4px; font-size: 24px; } p { margin: 0; color: var(--cft-text-secondary); font-size: 14px; } }
  .chart-card { .score-wrap { display: flex; justify-content: center; padding: 20px 0; } .score-circle { text-align: center; .score-value { font-size: 48px; font-weight: 700; color: var(--cft-primary); } .score-label { font-size: 13px; color: var(--cft-text-muted); margin-top: 4px; } } .score-desc { font-size: 13px; color: var(--cft-text-secondary); line-height: 1.8; margin: 12px 0 16px; } }
  .radar-chart { width: 100%; height: 360px; }
  .dim-item { padding: 12px; border-radius: 8px; background: var(--cft-bg); margin-bottom: 12px; }
  .dim-header { display: flex; justify-content: space-between; margin-bottom: 8px; .dim-name { font-weight: 500; } .dim-score { font-weight: 600; color: var(--cft-primary); } }
  .dim-desc { font-size: 12px; color: var(--cft-text-muted); margin: 8px 0 0; line-height: 1.6; }
  .suggestion-item { display: flex; gap: 12px; padding: 16px; background: var(--cft-bg); border-radius: 8px; margin-bottom: 12px; }
  .s-index { width: 28px; height: 28px; border-radius: 50%; background: var(--cft-primary); color: white; display: flex; align-items: center; justify-content: center; font-weight: 600; flex-shrink: 0; font-size: 14px; }
  .s-content h4 { margin: 0 0 4px; font-size: 14px; } .s-content p { margin: 0; font-size: 13px; color: var(--cft-text-secondary); line-height: 1.7; }
  .w-full { width: 100%; }
  .mt-4 { margin-top: 16px; }
}
</style>
