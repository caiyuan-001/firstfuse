<template>
  <div class="h5-project">
    <header class="page-head">
      <h2>项目档案</h2>
      <span class="status-tag">融资中</span>
    </header>

    <section class="summary-card">
      <p class="name">{{ form.name || '未命名项目' }}</p>
      <div class="tags">
        <span class="tag tag-blue">{{ form.industry }}</span>
        <span class="tag tag-green">{{ form.stage }}</span>
      </div>
      <div class="progress">
        <div class="bar"><i :style="{ width: `${completeness}%` }"></i></div>
        <span class="pct">完整度 {{ completeness }}%</span>
      </div>
    </section>

    <section class="block">
      <h3>基本信息</h3>
      <div class="field">
        <label>项目名称</label>
        <input v-model.trim="form.name" placeholder="请输入项目名称" />
      </div>
      <div class="field">
        <label>所属行业</label>
        <div class="chips">
          <span
            v-for="opt in industryOptions"
            :key="opt"
            class="chip"
            :class="{ active: form.industry === opt }"
            @click="form.industry = opt"
          >{{ opt }}</span>
        </div>
      </div>
      <div class="field">
        <label>所处阶段</label>
        <div class="chips">
          <span
            v-for="opt in stageOptions"
            :key="opt"
            class="chip"
            :class="{ active: form.stage === opt }"
            @click="form.stage = opt"
          >{{ opt }}</span>
        </div>
      </div>
      <div class="field">
        <label>项目简介</label>
        <textarea
          v-model.trim="form.intro"
          rows="3"
          maxlength="200"
          placeholder="一句话说清：为谁、解决什么问题、怎么赚钱"
        ></textarea>
      </div>
    </section>

    <section class="block">
      <h3>融资需求</h3>
      <div class="field">
        <label>拟融资金额</label>
        <div class="chips">
          <span
            v-for="opt in amountOptions"
            :key="opt"
            class="chip"
            :class="{ active: form.amount === opt }"
            @click="form.amount = opt"
          >{{ opt }}</span>
        </div>
      </div>
      <div class="field">
        <label>出让比例 <em>{{ form.equity }}%</em></label>
        <input v-model.number="form.equity" type="range" min="1" max="49" />
      </div>
      <div class="field">
        <label>资金用途</label>
        <textarea
          v-model.trim="form.usage"
          rows="2"
          maxlength="100"
          placeholder="如：研发 50%、市场 30%、团队 20%"
        ></textarea>
      </div>
    </section>

    <button class="save-btn" :disabled="saving" @click="save">
      {{ saving ? '保存中...' : '保存项目档案' }}
    </button>
    <p class="foot-hint">档案完整度越高，AI 诊断与投资人匹配越精准</p>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';

const industryOptions = ['医疗健康', '硬科技', '企业服务', '新能源', '消费科技'];
const stageOptions = ['天使轮', 'Pre-A轮', 'A轮', 'B轮', 'C轮及以后'];
const amountOptions = ['300万', '500万', '1000万', '2000万', '3000万+'];

const form = reactive({
  name: '智能影像诊断 SaaS',
  industry: '医疗健康',
  stage: 'Pre-A轮',
  intro: '',
  amount: '500万',
  equity: 10,
  usage: '',
});

const saving = ref(false);

const completeness = computed(() => {
  const fields = [form.name, form.industry, form.stage, form.intro, form.amount, form.usage];
  return Math.round((fields.filter(Boolean).length / fields.length) * 100);
});

function save() {
  if (!form.name) {
    ElMessage.warning('请填写项目名称');
    return;
  }
  saving.value = true;
  setTimeout(() => {
    saving.value = false;
    ElMessage.success('保存成功，AI 诊断与匹配结果将同步更新');
  }, 700);
}
</script>

<style lang="scss" scoped>
.h5-project {
  min-height: 100%;
  padding-bottom: 8px;
}
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 16px 14px;
  h2 {
    font-size: 20px;
    font-weight: 700;
    color: var(--cft-text-primary);
    margin: 0;
  }
  .status-tag {
    font-size: 11px;
    padding: 3px 10px;
    border-radius: 12px;
    background: #dcfce7;
    color: #16a34a;
  }
}
.summary-card {
  margin: 0 16px 14px;
  padding: 16px;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
  border-radius: 16px;
  color: #fff;
  .name {
    font-size: 17px;
    font-weight: 600;
    margin: 0 0 10px;
  }
  .tags {
    display: flex;
    gap: 6px;
    margin-bottom: 14px;
    .tag {
      font-size: 11px;
      padding: 2px 8px;
      border-radius: 4px;
      background: rgba(255, 255, 255, 0.18);
    }
  }
  .progress {
    display: flex;
    align-items: center;
    gap: 10px;
    .bar {
      flex: 1;
      height: 6px;
      border-radius: 3px;
      background: rgba(255, 255, 255, 0.25);
      overflow: hidden;
      i {
        display: block;
        height: 100%;
        border-radius: 3px;
        background: #fff;
      }
    }
    .pct {
      font-size: 12px;
      opacity: 0.9;
    }
  }
}
.block {
  margin: 0 16px 14px;
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
.field {
  margin-bottom: 16px;
  &:last-child {
    margin-bottom: 0;
  }
  label {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    color: var(--cft-text-secondary);
    margin-bottom: 8px;
    em {
      font-style: normal;
      color: var(--cft-primary);
      font-weight: 600;
    }
  }
  input:not([type='range']),
  textarea {
    width: 100%;
    border: none;
    outline: none;
    background: #f6f8fb;
    border-radius: 12px;
    padding: 12px 14px;
    font-size: 15px;
    color: var(--cft-text-primary);
    resize: none;
    &::placeholder {
      color: #a3aec0;
    }
  }
  input[type='range'] {
    width: 100%;
    accent-color: var(--cft-primary);
  }
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chip {
  font-size: 13px;
  padding: 7px 14px;
  border-radius: 16px;
  background: #f6f8fb;
  color: var(--cft-text-secondary);
  cursor: pointer;
  transition: all 0.15s;
  &.active {
    background: #e0edff;
    color: var(--cft-primary);
    font-weight: 500;
  }
  &:active {
    transform: scale(0.96);
  }
}
.save-btn {
  display: block;
  width: calc(100% - 32px);
  margin: 6px 16px 0;
  height: 48px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  &:disabled {
    opacity: 0.6;
  }
}
.foot-hint {
  text-align: center;
  font-size: 11px;
  color: var(--cft-text-muted);
  margin: 12px 0 0;
}
</style>
