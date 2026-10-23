<template>
  <div class="h5-roadshows">
    <header class="page-head">
      <h2>路演活动</h2>
    </header>

    <div class="chip-row">
      <span
        v-for="t in tabs"
        :key="t"
        class="chip"
        :class="{ active: activeTab === t }"
        @click="activeTab = t"
      >{{ t }}</span>
    </div>

    <div v-for="rs in filtered" :key="rs.id" class="rs-card">
      <div class="date-block">
        <span class="d">{{ rs.day }}</span>
        <span class="m">{{ rs.month }}</span>
      </div>
      <div class="info">
        <p class="title">
          {{ rs.title }}
          <span v-if="rs.closed" class="closed-tag">闭门</span>
        </p>
        <p class="meta">{{ rs.time }} · {{ rs.location }}</p>
        <span class="status" :class="statusClass(rs.status)">{{ rs.status }}</span>
      </div>
      <button
        class="action"
        :disabled="rs.status === '已结束' || signed.has(rs.id)"
        @click="signup(rs)"
      >
        {{ signed.has(rs.id) ? '已报名' : rs.status === '已结束' ? '已结束' : '报名' }}
      </button>
    </div>

    <p v-if="filtered.length === 0" class="empty">暂无相关路演</p>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';

type RoadshowStatus = '报名中' | '即将开始' | '已结束';

interface Roadshow {
  id: string;
  title: string;
  day: string;
  month: string;
  time: string;
  location: string;
  closed: boolean;
  status: RoadshowStatus;
}

const tabs = ['全部', '报名中', '即将开始', '已结束'];
const activeTab = ref('全部');

const roadshows = ref<Roadshow[]>([
  { id: '1', title: '人工智能闭门路演 · 第13期', day: '15', month: '11月', time: '11-15 14:00', location: '上海·陆家嘴', closed: true, status: '报名中' },
  { id: '2', title: '新能源产业专场路演', day: '22', month: '11月', time: '11-22 10:00', location: '线上腾讯会议', closed: false, status: '报名中' },
  { id: '3', title: '医疗健康创新路演', day: '05', month: '12月', time: '12-05 14:00', location: '北京·中关村', closed: true, status: '即将开始' },
  { id: '4', title: '企业服务数字化专场', day: '28', month: '10月', time: '10-28 14:00', location: '深圳·南山', closed: false, status: '已结束' },
]);

const signed = reactive(new Set<string>());

const filtered = computed(() =>
  roadshows.value.filter((rs) => activeTab.value === '全部' || rs.status === activeTab.value),
);

function statusClass(s: RoadshowStatus) {
  return { green: s === '报名中', blue: s === '即将开始', gray: s === '已结束' };
}

function signup(rs: Roadshow) {
  if (rs.status !== '报名中' || signed.has(rs.id)) return;
  signed.add(rs.id);
  ElMessage.success(`已提交「${rs.title}」报名申请，等待主办方审核（演示 Mock）`);
}
</script>

<style lang="scss" scoped>
.h5-roadshows {
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
}
.rs-card {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 12px 16px 0;
  padding: 14px 16px;
  background: #fff;
  border-radius: 16px;
}
.date-block {
  width: 46px;
  height: 46px;
  border-radius: 10px;
  background: #eff6ff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  .d {
    font-size: 17px;
    font-weight: 700;
    color: var(--cft-primary);
    line-height: 1;
  }
  .m {
    font-size: 10px;
    color: var(--cft-text-muted);
  }
}
.info {
  flex: 1;
  min-width: 0;
  .title {
    font-size: 14px;
    font-weight: 600;
    color: var(--cft-text-primary);
    margin: 0 0 3px;
    display: flex;
    align-items: center;
    gap: 6px;
  }
  .closed-tag {
    flex-shrink: 0;
    font-size: 10px;
    font-weight: 400;
    padding: 1px 6px;
    border-radius: 4px;
    background: #fef3c7;
    color: #d97706;
  }
  .meta {
    font-size: 12px;
    color: var(--cft-text-muted);
    margin: 0 0 6px;
  }
  .status {
    display: inline-block;
    font-size: 11px;
    padding: 1px 8px;
    border-radius: 4px;
    &.green {
      background: #dcfce7;
      color: #16a34a;
    }
    &.blue {
      background: #e0edff;
      color: #2563eb;
    }
    &.gray {
      background: #f1f5f9;
      color: #94a3b8;
    }
  }
}
.action {
  flex-shrink: 0;
  padding: 8px 16px;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  &:disabled {
    background: #f1f5f9;
    color: #94a3b8;
    cursor: not-allowed;
  }
}
.empty {
  text-align: center;
  font-size: 13px;
  color: var(--cft-text-muted);
  padding: 32px 0;
}
</style>
