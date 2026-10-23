<template>
  <div class="ai-filter">
    <div class="page-header">
      <h1>AI 项目智能筛选</h1>
      <p>用自然语言描述您的投资偏好，AI 将为您匹配最合适的项目</p>
    </div>

    <el-row :gutter="20">
      <!-- 左侧输入面板 -->
      <el-col :span="10">
        <div class="cft-card input-panel">
          <h3>🤖 投资策略输入</h3>

          <el-form label-position="top">
            <el-form-item label="💬 用自然语言描述投资偏好">
              <el-input
                v-model="strategy.text"
                type="textarea"
                :rows="4"
                placeholder="例如：我想投 A 轮左右的硬科技项目，最好是人工智能和机器人方向，已经有产品雏形和少量付费客户的团队..."
              />
            </el-form-item>

            <el-form-item label="快速选择（可叠加）">
              <div class="quick-chips">
                <el-tag
                  v-for="q in quickStrategies"
                  :key="q"
                  effect="plain"
                  class="chip"
                  @click="toggleStrategy(q)"
                  :type="strategy.keywords.includes(q) ? 'primary' : 'info'"
                >{{ q }}</el-tag>
              </div>
            </el-form-item>

            <el-divider />

            <h4>🧮 量化偏好（可选）</h4>
            <el-form-item label="投资阶段">
              <el-checkbox-group v-model="strategy.stages">
                <el-checkbox value="pre_seed">Pre-Seed</el-checkbox>
                <el-checkbox value="seed">种子轮</el-checkbox>
                <el-checkbox value="series_a">A轮</el-checkbox>
                <el-checkbox value="series_b">B轮</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item label="目标行业">
              <el-select v-model="strategy.industries" multiple placeholder="选择关注行业" class="w-full">
                <el-option v-for="i in industryOptions" :key="i.value" :label="i.label" :value="i.value" />
              </el-select>
            </el-form-item>

            <el-form-item label="目标融资金额(万元)">
              <el-slider v-model="strategy.amountRange" :min="100" :max="50000" :step="100" range show-input />
            </el-form-item>

            <el-button type="primary" size="large" class="w-full" :loading="loading" @click="doFilter">
              🎯 AI 智能筛选（{{ loading ? 'AI 分析中...' : '开始筛选' }}）
            </el-button>
          </el-form>
        </div>

        <div class="cft-card mt-4">
          <h4>📝 使用说明</h4>
          <ul class="tips">
            <li>💡 AI 会综合考虑您的自然语言描述和量化条件</li>
            <li>🎯 匹配度分数基于多维度评分模型</li>
            <li>📊 每个匹配结果支持一键生成评审简报</li>
            <li>🔒 所有筛选结果仅您的机构可见</li>
          </ul>
        </div>
      </el-col>

      <!-- 右侧结果 -->
      <el-col :span="14">
        <div v-if="!hasSearched" class="empty-result cft-card">
          <el-empty description="输入您的投资策略，点击「AI 智能筛选」开始匹配" />
        </div>

        <div v-else class="result-panel">
          <div class="result-header">
            <div>
              <h3>🎯 AI 匹配结果</h3>
              <p class="result-desc">AI 已为您匹配 <strong>{{ results.length }}</strong> 个项目，按匹配度排序</p>
            </div>
            <el-radio-group v-model="resultMode" size="small">
              <el-radio-button value="list">列表</el-radio-button>
              <el-radio-button value="score">评分详情</el-radio-button>
            </el-radio-group>
          </div>

          <!-- 列表视图 -->
          <div v-if="resultMode === 'list'">
            <div v-for="r in results" :key="r.id" class="match-card">
              <div class="score-ring">
                <el-progress
                  type="dashboard"
                  :percentage="r.score"
                  :color="scoreColor(r.score)"
                  :width="70"
                  :stroke-width="6"
                />
              </div>
              <div class="mc-info">
                <h4>{{ r.title }}</h4>
                <p class="mc-desc">{{ r.shortDesc }}</p>
                <div class="mc-tags">
                  <el-tag size="small" effect="plain">{{ r.industry }}</el-tag>
                  <el-tag size="small" effect="plain">{{ r.stageLabel }}</el-tag>
                  <el-tag size="small" type="warning">💰 {{ r.amount }}万</el-tag>
                </div>
              </div>
              <div class="mc-actions">
                <el-button type="primary" size="small" @click="generateBrief(r)"><el-icon><MagicStick /></el-icon>生成简报</el-button>
              </div>
            </div>
          </div>

          <!-- 评分详情视图 -->
          <div v-else class="score-detail">
            <div v-for="r in results" :key="r.id" class="score-card">
              <div class="score-header">
                <h4>{{ r.title }}</h4>
                <el-tag :type="matchTag(r.score)">匹配度 {{ r.score }}%</el-tag>
              </div>
              <div class="score-bars">
                <div v-for="d in r.dimensions" :key="d.name" class="dim-bar">
                  <span class="dim-name">{{ d.name }}</span>
                  <el-progress :percentage="d.score" :stroke-width="14" :color="scoreColor(d.score)" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { MagicStick } from '@element-plus/icons-vue';
import { INDUSTRY_OPTIONS } from '@chuangrongtong/shared';

const router = useRouter();

const industryOptions = INDUSTRY_OPTIONS;
const loading = ref(false);
const hasSearched = ref(false);
const resultMode = ref<'list' | 'score'>('list');

const quickStrategies = ['硬科技', 'Pre-Seed', 'A轮', 'AI/大模型', '机器人', '新能源', '医疗健康', '有付费客户', '团队有大厂背景', '已获知名机构投资'];

const strategy = reactive({
  text: '',
  keywords: [] as string[],
  stages: [] as string[],
  industries: [] as string[],
  amountRange: [0, 50000],
});

const allResults = [
  { id: '1', title: '下一代 AI Agent 协作平台', score: 92, shortDesc: '面向企业级的 AI Agent 协作编排平台', industry: '人工智能', stageLabel: 'Pre-Seed', stageKey: 'pre_seed', amount: 300, dimensions: [{ name: '行业匹配', score: 95 }, { name: '阶段匹配', score: 100 }, { name: '金额匹配', score: 88 }, { name: '团队背景', score: 90 }, { name: '项目热度', score: 86 }] },
  { id: '2', title: '智能影像诊断 SaaS', score: 87, shortDesc: '基于深度学习的医学影像辅助诊断系统', industry: '医疗健康', stageLabel: 'A轮', stageKey: 'series_a', amount: 5000, dimensions: [{ name: '行业匹配', score: 92 }, { name: '阶段匹配', score: 95 }, { name: '金额匹配', score: 78 }, { name: '团队背景', score: 85 }, { name: '项目热度', score: 90 }] },
  { id: '3', title: '工业机器人核心控制器', score: 81, shortDesc: '自主研发的六轴工业机器人控制器', industry: '机器人', stageLabel: 'Pre-Seed', stageKey: 'pre_seed', amount: 500, dimensions: [{ name: '行业匹配', score: 88 }, { name: '阶段匹配', score: 92 }, { name: '金额匹配', score: 95 }, { name: '团队背景', score: 78 }, { name: '项目热度', score: 72 }] },
  { id: '4', title: '固态电池关键材料技术', score: 76, shortDesc: '新型硫化物固态电解质材料', industry: '新能源', stageLabel: '种子轮', stageKey: 'seed', amount: 1200, dimensions: [{ name: '行业匹配', score: 80 }, { name: '阶段匹配', score: 85 }, { name: '金额匹配', score: 82 }, { name: '团队背景', score: 70 }, { name: '项目热度', score: 68 }] },
];

const results = ref<typeof allResults>([]);

function toggleStrategy(q: string) {
  const idx = strategy.keywords.indexOf(q);
  if (idx >= 0) strategy.keywords.splice(idx, 1);
  else strategy.keywords.push(q);
}

async function doFilter() {
  const hasInput = strategy.text.trim() || strategy.keywords.length || strategy.stages.length || strategy.industries.length;
  if (!hasInput) {
    ElMessage.warning('请先描述投资偏好，或选择快速标签/量化条件');
    return;
  }
  loading.value = true;
  await new Promise(r => setTimeout(r, 1500));
  // 演示逻辑：按量化条件确定性过滤，命中的优先展示并提升排序
  const industryLabels = industryOptions
    .filter((o) => strategy.industries.includes(o.value))
    .map((o) => o.label);
  const textPool = strategy.text + strategy.keywords.join(',');
  let pool = [...allResults];
  if (strategy.stages.length) pool = pool.filter((r) => strategy.stages.includes(r.stageKey));
  if (industryLabels.length) pool = pool.filter((r) => industryLabels.includes(r.industry));
  pool = pool.filter((r) => r.amount >= strategy.amountRange[0] && r.amount <= strategy.amountRange[1]);
  if (pool.length === 0) {
    // 量化条件过严时回退为全量，按文本相关性排序
    pool = [...allResults].sort((a, b) => {
      const aHit = textPool.includes(a.industry) ? 1 : 0;
      const bHit = textPool.includes(b.industry) ? 1 : 0;
      return bHit - aHit || b.score - a.score;
    });
  } else {
    pool.sort((a, b) => b.score - a.score);
  }
  results.value = pool;
  loading.value = false;
  hasSearched.value = true;
  ElMessage.success(`AI 分析完成，为您匹配 ${pool.length} 个项目`);
}

function generateBrief(r: any) {
  ElMessage.success(`正在为「${r.title}」生成 AI 评审简报...`);
  setTimeout(() => router.push(`/institution/review/${r.id}`), 600);
}

function scoreColor(s: number) {
  if (s >= 85) return '#22c55e';
  if (s >= 70) return '#f59e0b';
  return '#ef4444';
}

function matchTag(s: number) {
  if (s >= 85) return 'success';
  if (s >= 70) return 'warning';
  return 'danger';
}
</script>

<style lang="scss" scoped>
.ai-filter {
  .page-header { margin-bottom: 20px; h1 { margin: 0 0 4px; font-size: 24px; } p { margin: 0; color: var(--cft-text-secondary); font-size: 14px; } }
  .input-panel h3 { margin: 0 0 16px; }
  .quick-chips { display: flex; flex-wrap: wrap; gap: 8px; }
  .chip { cursor: pointer; transition: all 0.2s; &:hover { opacity: 0.8; } }
  .w-full { width: 100%; }
  .empty-result { min-height: 500px; display: flex; align-items: center; justify-content: center; }
  .result-panel { .result-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; h3 { margin: 0 0 4px; font-size: 16px; } .result-desc { margin: 0; color: var(--cft-text-secondary); font-size: 13px; } } }
  .match-card {
    background: white; border: 1px solid var(--cft-border); border-radius: 12px;
    padding: 20px; margin-bottom: 16px; display: flex; gap: 20px; align-items: center;
    transition: all 0.2s;
    &:hover { border-color: var(--cft-primary); box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
  }
  .score-ring { flex-shrink: 0; }
  .mc-info { flex: 1; min-width: 0; h4 { margin: 0 0 6px; font-size: 15px; } .mc-desc { margin: 0 0 10px; color: var(--cft-text-secondary); font-size: 13px; } .mc-tags { display: flex; gap: 6px; } }
  .mc-actions { flex-shrink: 0; }
  .score-card { background: white; border: 1px solid var(--cft-border); border-radius: 12px; padding: 18px; margin-bottom: 14px; .score-header { display: flex; justify-content: space-between; margin-bottom: 14px; h4 { margin: 0; } } }
  .dim-bar { margin-bottom: 10px; .dim-name { display: inline-block; width: 80px; font-size: 12px; color: var(--cft-text-muted); } }
  .tips { padding-left: 20px; margin: 0; li { font-size: 13px; color: var(--cft-text-secondary); margin-bottom: 6px; } }
  .mt-4 { margin-top: 16px; }
}
</style>
