<template>
  <div class="h5-home">
    <!-- 顶部问候 -->
    <header class="home-header">
      <div class="profile">
        <div class="avatar">{{ shortName }}</div>
        <div class="greet">
          <p class="hello">{{ greeting }}，{{ nickname }}</p>
          <p class="sub">今天也为融资推进加把劲</p>
        </div>
      </div>
      <div class="bell" @click="onBell">
        <el-icon :size="20"><Bell /></el-icon>
        <span class="dot"></span>
      </div>
    </header>

    <!-- 项目进展卡 -->
    <section class="project-card" @click="router.push('/h5/project')">
      <div class="pc-top">
        <div>
          <p class="pc-name">{{ project.name }}</p>
          <div class="pc-tags">
            <span class="tag tag-blue">{{ project.stage }}</span>
            <span class="tag tag-green">{{ project.status }}</span>
          </div>
        </div>
        <el-icon :size="18" class="pc-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="pc-progress">
        <div class="bar"><i :style="{ width: `${project.progress}%` }"></i></div>
        <span class="pct">{{ project.progress }}%</span>
      </div>
      <p class="pc-hint">资料完善度 {{ project.progress }}%，完善后 AI 诊断更精准</p>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-grid">
      <div v-for="q in quickEntries" :key="q.path" class="quick-item" @click="router.push(q.path)">
        <div class="qi-icon" :style="{ background: q.bg }">
          <el-icon :size="22" :color="q.color"><component :is="q.icon" /></el-icon>
        </div>
        <span class="qi-label">{{ q.label }}</span>
      </div>
    </section>

    <!-- AI 诊断提示 -->
    <section class="ai-tip" @click="router.push('/h5/diagnosis')">
      <div class="ai-dots"><i></i><i></i><i></i></div>
      <div class="ai-text">
        <p class="ai-title">AI 融资诊断已就绪</p>
        <p class="ai-desc">上次诊断得分 76 分，有 3 条优化建议待查看</p>
      </div>
      <el-icon :size="18"><ArrowRight /></el-icon>
    </section>

    <!-- 数据概览 -->
    <section class="stats">
      <div v-for="s in stats" :key="s.label" class="stat">
        <p class="stat-value">{{ s.value }}</p>
        <p class="stat-label">{{ s.label }}</p>
      </div>
    </section>

    <!-- 匹配投资人 -->
    <section class="block">
      <div class="block-head">
        <h3>匹配投资人</h3>
        <span class="more" @click="router.push('/h5/investors')">
          全部<el-icon :size="12"><ArrowRight /></el-icon>
        </span>
      </div>
      <div class="investor-list">
        <div v-for="inv in investors" :key="inv.name" class="investor-item">
          <div class="inv-logo">{{ inv.short }}</div>
          <div class="inv-info">
            <p class="inv-name">{{ inv.name }}</p>
            <p class="inv-meta">{{ inv.field }} · {{ inv.stage }} · 已投 {{ inv.deals }} 个</p>
          </div>
          <div class="inv-match">{{ inv.match }}<small>%</small></div>
        </div>
      </div>
    </section>

    <!-- 近期路演 -->
    <section class="block">
      <div class="block-head">
        <h3>近期路演</h3>
        <span class="more" @click="router.push('/h5/roadshows')">
          更多<el-icon :size="12"><ArrowRight /></el-icon>
        </span>
      </div>
      <div class="roadshow-list">
        <div v-for="rs in roadshows" :key="rs.title" class="roadshow-item">
          <div class="rs-date">
            <span class="d">{{ rs.day }}</span>
            <span class="m">{{ rs.month }}</span>
          </div>
          <div class="rs-info">
            <p class="rs-title">{{ rs.title }}</p>
            <p class="rs-meta">{{ rs.time }} · {{ rs.location }}</p>
          </div>
          <button class="rs-btn" @click.stop="onSignUp(rs.title)">报名</button>
        </div>
      </div>
    </section>

    <p class="home-footer">{{ PLATFORM_INFO.slogan }} · 前端 Demo（数据均为 Mock 模拟）</p>
  </div>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowRight, Bell, Cpu, MagicStick, OfficeBuilding, Calendar } from '@element-plus/icons-vue';
import { useUserStore } from '@/store';
import { PLATFORM_INFO } from '@chuangrongtong/shared';

const router = useRouter();
const userStore = useUserStore();

const nickname = computed(() => userStore.nickname || '创业者');
const shortName = computed(() => nickname.value.slice(0, 1));
const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '凌晨好';
  if (hour < 12) return '早上好';
  if (hour < 18) return '下午好';
  return '晚上好';
});

const project = {
  name: '智能影像诊断 SaaS',
  stage: 'Pre-A轮',
  status: '融资中',
  progress: 72,
};

interface QuickEntry {
  path: string;
  label: string;
  icon: Component;
  bg: string;
  color: string;
}

const quickEntries: QuickEntry[] = [
  { path: '/h5/diagnosis', label: 'AI融资诊断', icon: Cpu, bg: '#e0edff', color: '#2563eb' },
  { path: '/h5/bp', label: 'BP打磨', icon: MagicStick, bg: '#ede9fe', color: '#7c3aed' },
  { path: '/h5/investors', label: '找投资人', icon: OfficeBuilding, bg: '#dcfce7', color: '#16a34a' },
  { path: '/h5/roadshows', label: '路演报名', icon: Calendar, bg: '#fef3c7', color: '#d97706' },
];

const stats = [
  { label: '项目浏览', value: '1,284' },
  { label: '被收藏', value: '96' },
  { label: '邀约洽谈', value: '12' },
];

const investors = [
  { name: '红杉资本中国', short: '红杉', field: '硬科技', stage: 'A/B轮', deals: 128, match: 92 },
  { name: '深创投', short: '深创', field: '医疗健康', stage: 'Pre-A/A轮', deals: 86, match: 88 },
  { name: '北极光创投', short: '北极', field: '企业服务', stage: '天使/A轮', deals: 64, match: 81 },
];

const roadshows = [
  { day: '15', month: '11月', title: '人工智能闭门路演 · 第13期', time: '14:00', location: '上海·陆家嘴' },
  { day: '22', month: '11月', title: '新能源产业专场路演', time: '10:00', location: '线上腾讯会议' },
];

function onBell() {
  ElMessage.info('暂无新消息');
}

function onSignUp(title: string) {
  ElMessage.success(`已提交「${title}」报名申请`);
}
</script>

<style lang="scss" scoped>
.h5-home {
  min-height: 100%;
  padding-bottom: 20px;
}
.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26px 20px 58px;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
  color: #fff;
  .profile {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    font-weight: 600;
  }
  .hello {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 2px;
  }
  .sub {
    font-size: 12px;
    opacity: 0.78;
    margin: 0;
  }
  .bell {
    position: relative;
    cursor: pointer;
    .dot {
      position: absolute;
      top: -1px;
      right: -1px;
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: #ef4444;
    }
  }
}
.project-card {
  margin: -44px 16px 16px;
  padding: 16px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.1);
  cursor: pointer;
  .pc-top {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
  }
  .pc-name {
    font-size: 16px;
    font-weight: 600;
    color: var(--cft-text-primary);
    margin: 0 0 8px;
  }
  .pc-tags {
    display: flex;
    gap: 6px;
  }
  .tag {
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 4px;
  }
  .tag-blue {
    background: #e0edff;
    color: #2563eb;
  }
  .tag-green {
    background: #dcfce7;
    color: #16a34a;
  }
  .pc-arrow {
    color: var(--cft-text-muted);
  }
  .pc-progress {
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 16px 0 8px;
    .bar {
      flex: 1;
      height: 6px;
      border-radius: 3px;
      background: #eef2f7;
      overflow: hidden;
      i {
        display: block;
        height: 100%;
        border-radius: 3px;
        background: linear-gradient(90deg, #3b82f6, #22c55e);
      }
    }
    .pct {
      font-size: 12px;
      font-weight: 600;
      color: var(--cft-primary);
    }
  }
  .pc-hint {
    font-size: 12px;
    color: var(--cft-text-muted);
    margin: 0;
  }
}
.quick-grid {
  display: flex;
  margin: 0 16px 16px;
  padding: 18px 8px;
  background: #fff;
  border-radius: 16px;
}
.quick-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  .qi-icon {
    width: 46px;
    height: 46px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .qi-label {
    font-size: 12px;
    color: var(--cft-text-secondary);
  }
  &:active {
    opacity: 0.75;
  }
}
.ai-tip {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 16px 16px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #eff6ff, #ecfdf5);
  border: 1px solid #dbeafe;
  border-radius: 14px;
  cursor: pointer;
  .ai-dots {
    display: flex;
    gap: 4px;
    i {
      width: 6px;
      height: 6px;
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
  .ai-text {
    flex: 1;
    min-width: 0;
    .ai-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--cft-text-primary);
      margin: 0 0 2px;
    }
    .ai-desc {
      font-size: 12px;
      color: var(--cft-text-secondary);
      margin: 0;
    }
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
.stats {
  display: flex;
  margin: 0 16px 16px;
  padding: 16px 0;
  background: #fff;
  border-radius: 16px;
  .stat {
    flex: 1;
    text-align: center;
    &:not(:last-child) {
      border-right: 1px solid var(--cft-border);
    }
    .stat-value {
      font-size: 18px;
      font-weight: 700;
      color: var(--cft-text-primary);
      margin: 0 0 2px;
    }
    .stat-label {
      font-size: 12px;
      color: var(--cft-text-muted);
      margin: 0;
    }
  }
}
.block {
  margin: 0 16px 16px;
  padding: 16px;
  background: #fff;
  border-radius: 16px;
  .block-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    h3 {
      font-size: 15px;
      font-weight: 600;
      color: var(--cft-text-primary);
      margin: 0;
    }
    .more {
      display: flex;
      align-items: center;
      gap: 2px;
      font-size: 12px;
      color: var(--cft-text-muted);
      cursor: pointer;
    }
  }
}
.investor-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  &:not(:last-child) {
    border-bottom: 1px solid var(--cft-border);
  }
  .inv-logo {
    width: 38px;
    height: 38px;
    border-radius: 10px;
    background: #f1f5f9;
    color: var(--cft-text-secondary);
    font-size: 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
  .inv-info {
    flex: 1;
    min-width: 0;
    .inv-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--cft-text-primary);
      margin: 0 0 2px;
    }
    .inv-meta {
      font-size: 12px;
      color: var(--cft-text-muted);
      margin: 0;
    }
  }
  .inv-match {
    font-size: 16px;
    font-weight: 700;
    color: var(--cft-primary);
    small {
      font-size: 11px;
      font-weight: 500;
    }
  }
}
.roadshow-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  &:not(:last-child) {
    border-bottom: 1px solid var(--cft-border);
  }
  .rs-date {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    background: #eff6ff;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    .d {
      font-size: 16px;
      font-weight: 700;
      color: var(--cft-primary);
      line-height: 1;
    }
    .m {
      font-size: 10px;
      color: var(--cft-text-muted);
    }
  }
  .rs-info {
    flex: 1;
    min-width: 0;
    .rs-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--cft-text-primary);
      margin: 0 0 2px;
    }
    .rs-meta {
      font-size: 12px;
      color: var(--cft-text-muted);
      margin: 0;
    }
  }
  .rs-btn {
    flex-shrink: 0;
    padding: 6px 14px;
    border: 1px solid var(--cft-primary);
    border-radius: 14px;
    background: #fff;
    color: var(--cft-primary);
    font-size: 12px;
    cursor: pointer;
  }
}
.home-footer {
  text-align: center;
  font-size: 11px;
  color: var(--cft-text-muted);
  margin: 20px 0 0;
}
</style>
