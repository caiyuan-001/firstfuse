<template>
  <div class="h5-investors">
    <header class="page-head">
      <h2>找投资人</h2>
    </header>

    <div class="search-bar">
      <el-icon :size="16"><Search /></el-icon>
      <input v-model.trim="keyword" placeholder="搜索机构名称 / 关注领域" />
    </div>

    <div class="chip-row">
      <span
        v-for="f in filters"
        :key="f"
        class="chip"
        :class="{ active: activeFilter === f }"
        @click="activeFilter = f"
      >{{ f }}</span>
    </div>

    <p class="count">为你匹配 {{ filtered.length }} 家机构</p>

    <div v-for="inv in filtered" :key="inv.name" class="inv-card">
      <div class="row1">
        <div class="logo">{{ inv.short }}</div>
        <div class="info">
          <p class="name">{{ inv.name }}</p>
          <p class="meta">{{ inv.field }} · {{ inv.stage }} · 已投 {{ inv.deals }} 个项目</p>
        </div>
        <div class="match">
          {{ inv.match }}<small>%</small>
          <span>匹配度</span>
        </div>
      </div>
      <p class="desc">{{ inv.desc }}</p>
      <div class="row2">
        <button class="mini ghost" :class="{ on: collected.has(inv.name) }" @click="toggleCollect(inv.name)">
          {{ collected.has(inv.name) ? '已收藏' : '收藏' }}
        </button>
        <button class="mini primary" @click="contact(inv.name)">约谈</button>
      </div>
    </div>

    <p v-if="filtered.length === 0" class="empty">没有匹配的机构，换个筛选条件试试</p>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

interface Investor {
  name: string;
  short: string;
  field: string;
  stage: string;
  deals: number;
  match: number;
  desc: string;
}

const filters = ['全部', '硬科技', '医疗健康', '企业服务', '新能源', '消费科技'];
const activeFilter = ref('全部');
const keyword = ref('');

const investors = ref<Investor[]>([
  { name: '红杉资本中国', short: '红杉', field: '硬科技', stage: 'A/B轮', deals: 128, match: 92, desc: '重点关注企业软件与半导体，单笔投资 2000 万起' },
  { name: '深创投', short: '深创', field: '医疗健康', stage: 'Pre-A/A轮', deals: 86, match: 88, desc: '国资背景，医疗赛道累计投资超 40 家，重视临床落地' },
  { name: '北极光创投', short: '北极', field: '企业服务', stage: '天使/A轮', deals: 64, match: 81, desc: '偏好技术驱动型团队，创始人背景是重要加分项' },
  { name: '云启资本', short: '云启', field: '硬科技', stage: 'Pre-A/B轮', deals: 52, match: 78, desc: '聚焦「AI+产业」方向，投后赋能资源丰富' },
  { name: '礼来亚洲基金', short: '礼来', field: '医疗健康', stage: 'A/B/C轮', deals: 47, match: 74, desc: '全球顶级医疗基金，可对接海外临床与注册资源' },
]);

const collected = reactive(new Set<string>());

const filtered = computed(() =>
  investors.value.filter((inv) => {
    const byFilter = activeFilter.value === '全部' || inv.field === activeFilter.value;
    const kw = keyword.value.toLowerCase();
    const byKeyword = !kw || inv.name.toLowerCase().includes(kw) || inv.field.toLowerCase().includes(kw);
    return byFilter && byKeyword;
  }),
);

function toggleCollect(name: string) {
  if (collected.has(name)) {
    collected.delete(name);
    ElMessage.info(`已取消收藏「${name}」`);
  } else {
    collected.add(name);
    ElMessage.success(`已收藏「${name}」，可在「我的-我的收藏」查看`);
  }
}

function contact(name: string) {
  ElMessage.success(`已向「${name}」发送约谈请求，机构将在 1-3 个工作日内回复（演示 Mock）`);
}
</script>

<style lang="scss" scoped>
.h5-investors {
  min-height: 100%;
  padding-bottom: 8px;
}
.page-head {
  padding: 22px 16px 14px;
  h2 {
    font-size: 20px;
    font-weight: 700;
    color: var(--cft-text-primary);
    margin: 0;
  }
}
.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 16px 12px;
  height: 42px;
  padding: 0 14px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--cft-border);
  color: var(--cft-text-muted);
  input {
    flex: 1;
    min-width: 0;
    border: none;
    outline: none;
    background: transparent;
    font-size: 15px;
    color: var(--cft-text-primary);
    &::placeholder {
      color: #a3aec0;
    }
  }
}
.chip-row {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 0 16px 4px;
  &::-webkit-scrollbar {
    display: none;
  }
}
.chip {
  flex-shrink: 0;
  font-size: 13px;
  padding: 7px 14px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid var(--cft-border);
  color: var(--cft-text-secondary);
  cursor: pointer;
  &.active {
    background: #e0edff;
    border-color: var(--cft-primary);
    color: var(--cft-primary);
    font-weight: 500;
  }
  &:active {
    transform: scale(0.96);
  }
}
.count {
  font-size: 12px;
  color: var(--cft-text-muted);
  margin: 12px 16px 10px;
}
.inv-card {
  margin: 0 16px 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 16px;
  .row1 {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .logo {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: #f1f5f9;
    color: var(--cft-text-secondary);
    font-size: 13px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
  .info {
    flex: 1;
    min-width: 0;
    .name {
      font-size: 15px;
      font-weight: 600;
      color: var(--cft-text-primary);
      margin: 0 0 3px;
    }
    .meta {
      font-size: 12px;
      color: var(--cft-text-muted);
      margin: 0;
    }
  }
  .match {
    text-align: center;
    font-size: 18px;
    font-weight: 700;
    color: var(--cft-primary);
    line-height: 1.1;
    small {
      font-size: 11px;
    }
    span {
      display: block;
      font-size: 10px;
      font-weight: 400;
      color: var(--cft-text-muted);
    }
  }
  .desc {
    font-size: 12px;
    line-height: 1.6;
    color: var(--cft-text-secondary);
    background: #f6f8fb;
    border-radius: 8px;
    padding: 8px 10px;
    margin: 10px 0;
  }
  .row2 {
    display: flex;
    gap: 10px;
    .mini {
      flex: 1;
      height: 34px;
      border-radius: 10px;
      font-size: 13px;
      cursor: pointer;
      &.ghost {
        border: 1px solid var(--cft-border);
        background: #fff;
        color: var(--cft-text-secondary);
        &.on {
          border-color: var(--cft-warning);
          color: var(--cft-warning);
        }
      }
      &.primary {
        border: none;
        background: linear-gradient(135deg, #3b82f6, #2563eb);
        color: #fff;
      }
    }
  }
}
.empty {
  text-align: center;
  font-size: 13px;
  color: var(--cft-text-muted);
  padding: 32px 0;
}
</style>
