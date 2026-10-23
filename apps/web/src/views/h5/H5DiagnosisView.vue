<template>
  <div class="h5-diagnosis">
    <header class="grad-head">
      <h2>AI 融资诊断</h2>
      <p>基于 200+ 成功融资案例训练，6 大维度立体评估</p>
    </header>

    <!-- 待诊断 -->
    <section v-if="phase === 'idle'" class="block">
      <div class="intro-card">
        <p>将对「智能影像诊断 SaaS」从团队、产品、市场、财务、商业模式、风险六大维度进行体检，输出融资得分与优化建议。</p>
      </div>
      <button class="primary-btn" @click="start">
        <span class="mini-dots"><i></i><i></i><i></i></span>
        开始 AI 诊断
      </button>
      <p class="hint">诊断约需 3 秒，结果仅供参考</p>
    </section>

    <!-- 诊断中 -->
    <section v-else-if="phase === 'loading'" class="block loading">
      <div class="ai-dots"><i></i><i></i><i></i></div>
      <p class="step">{{ stepText }}</p>
      <p class="hint">AI 正在交叉验证行业数据…</p>
    </section>

    <!-- 诊断结果 -->
    <section v-else class="result">
      <div class="score-card">
        <div class="ring" :style="ringStyle">
          <span class="num">{{ total }}</span>
          <span class="unit">分</span>
        </div>
        <p class="verdict">{{ verdict }}</p>
      </div>

      <div class="block">
        <h3>维度得分</h3>
        <div v-for="d in dimensions" :key="d.name" class="dim">
          <div class="dim-head">
            <span>{{ d.name }}</span>
            <b>{{ d.score }}</b>
          </div>
          <div class="bar"><i :style="{ width: `${d.score}%`, background: d.color }"></i></div>
        </div>
      </div>

      <div class="block">
        <h3>优化建议</h3>
        <div v-for="(s, i) in suggestions" :key="i" class="sug">
          <span class="sug-tag" :class="s.level === '高' ? 'hot' : 'mid'">{{ s.level }}</span>
          <p>{{ s.text }}</p>
        </div>
      </div>

      <div class="btn-row">
        <button class="ghost-btn" @click="exportReport">导出报告</button>
        <button class="ghost-btn" @click="phase = 'idle'">重新诊断</button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref } from 'vue';
import { ElMessage } from 'element-plus';

type Phase = 'idle' | 'loading' | 'result';

const phase = ref<Phase>('idle');
const total = 76;
const verdict = '整体质量良好，财务与市场维度有提升空间，优化后可冲击 85 分';

const steps = ['正在解析项目档案…', '正在分析团队与股权结构…', '正在对标行业融资案例…', '正在生成优化建议…'];
const stepText = ref(steps[0]);
let stepTimer: ReturnType<typeof setInterval> | null = null;
let doneTimer: ReturnType<typeof setTimeout> | null = null;

const dimensions = [
  { name: '团队', score: 82, color: '#2563eb' },
  { name: '产品与技术', score: 78, color: '#3b82f6' },
  { name: '市场空间', score: 70, color: '#8b5cf6' },
  { name: '商业模式', score: 74, color: '#0ea5e9' },
  { name: '财务健康', score: 65, color: '#f59e0b' },
  { name: '风险合规', score: 80, color: '#22c55e' },
];

const suggestions = [
  { level: '高', text: '补充 3 年财务预测与关键假设（收入模型、获客成本、回款周期），投资人尽调必看' },
  { level: '高', text: '市场数据更新至 2026 年最新行业报告，替换目前引用的 2023 年数据' },
  { level: '中', text: '团队页突出 CTO 的影像 AI 顶会论文与算法量产经验，强化技术信任状' },
];

const ringStyle = computed(() => ({
  background: `conic-gradient(#2563eb ${total * 3.6}deg, #eef2f7 0deg)`,
}));

function start() {
  phase.value = 'loading';
  stepText.value = steps[0];
  let i = 0;
  stepTimer = setInterval(() => {
    i = (i + 1) % steps.length;
    stepText.value = steps[i];
  }, 650);
  doneTimer = setTimeout(() => {
    if (stepTimer) clearInterval(stepTimer);
    phase.value = 'result';
  }, 2600);
}

function exportReport() {
  ElMessage.success('诊断报告已生成（演示 Mock）：diag_report_1024.pdf');
}

onUnmounted(() => {
  if (stepTimer) clearInterval(stepTimer);
  if (doneTimer) clearTimeout(doneTimer);
});
</script>

<style lang="scss" scoped>
.h5-diagnosis {
  min-height: 100%;
}
.grad-head {
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
  color: #fff;
  padding: 26px 20px 24px;
  h2 {
    font-size: 20px;
    font-weight: 700;
    margin: 0 0 6px;
  }
  p {
    font-size: 12px;
    opacity: 0.8;
    margin: 0;
  }
}
.block {
  margin: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 16px;
  h3 {
    font-size: 15px;
    font-weight: 600;
    color: var(--cft-text-primary);
    margin: 0 0 14px;
  }
}
.intro-card {
  padding: 14px;
  background: #f6f8fb;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.8;
  color: var(--cft-text-secondary);
  margin-bottom: 18px;
  p {
    margin: 0;
  }
}
.primary-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.mini-dots {
  display: flex;
  gap: 3px;
  i {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: #fff;
    animation: ai-bounce 1.2s infinite ease-in-out;
    &:nth-child(2) {
      animation-delay: 0.15s;
    }
    &:nth-child(3) {
      animation-delay: 0.3s;
    }
  }
}
.hint {
  text-align: center;
  font-size: 11px;
  color: var(--cft-text-muted);
  margin: 12px 0 0;
}
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 16px;
  .ai-dots {
    display: flex;
    gap: 6px;
    margin-bottom: 16px;
    i {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      background: var(--cft-primary);
      animation: ai-bounce 1.2s infinite ease-in-out;
      &:nth-child(2) {
        animation-delay: 0.15s;
      }
      &:nth-child(3) {
        animation-delay: 0.3s;
      }
    }
  }
  .step {
    font-size: 14px;
    color: var(--cft-text-primary);
    font-weight: 500;
    margin: 0 0 6px;
  }
}
@keyframes ai-bounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-5px);
    opacity: 1;
  }
}
.score-card {
  margin: 16px;
  padding: 24px 16px;
  background: #fff;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  .ring {
    width: 112px;
    height: 112px;
    border-radius: 50%;
    display: flex;
    align-items: baseline;
    justify-content: center;
    align-content: center;
    flex-wrap: wrap;
    position: relative;
    &::before {
      content: '';
      position: absolute;
      inset: 10px;
      background: #fff;
      border-radius: 50%;
    }
    .num {
      position: relative;
      font-size: 36px;
      font-weight: 700;
      color: var(--cft-primary);
      line-height: 112px;
    }
    .unit {
      position: relative;
      font-size: 12px;
      color: var(--cft-text-muted);
      line-height: 100px;
    }
  }
  .verdict {
    font-size: 13px;
    color: var(--cft-text-secondary);
    text-align: center;
    margin: 14px 8px 0;
  }
}
.dim {
  margin-bottom: 12px;
  &:last-child {
    margin-bottom: 0;
  }
  .dim-head {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    color: var(--cft-text-secondary);
    margin-bottom: 6px;
    b {
      color: var(--cft-text-primary);
    }
  }
  .bar {
    height: 6px;
    border-radius: 3px;
    background: #eef2f7;
    overflow: hidden;
    i {
      display: block;
      height: 100%;
      border-radius: 3px;
    }
  }
}
.sug {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  align-items: flex-start;
  &:not(:last-child) {
    border-bottom: 1px solid var(--cft-border);
  }
  .sug-tag {
    flex-shrink: 0;
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 4px;
    margin-top: 2px;
  }
  .hot {
    background: #fee2e2;
    color: #dc2626;
  }
  .mid {
    background: #fef3c7;
    color: #d97706;
  }
  p {
    font-size: 13px;
    line-height: 1.7;
    color: var(--cft-text-primary);
    margin: 0;
  }
}
.btn-row {
  display: flex;
  gap: 12px;
  margin: 0 16px;
  .ghost-btn {
    flex: 1;
    height: 44px;
    border: 1px solid var(--cft-primary);
    border-radius: 12px;
    background: #fff;
    color: var(--cft-primary);
    font-size: 14px;
    cursor: pointer;
  }
}
</style>
