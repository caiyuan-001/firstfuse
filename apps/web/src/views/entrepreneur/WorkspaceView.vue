<template>
  <div class="entrepreneur-workspace">
    <!-- 个人信息头部 -->
    <div class="hero-card">
      <div class="avatar-big">👤</div>
      <div class="info">
        <h2>张创业 <el-tag size="small" type="success">已认证</el-tag></h2>
        <p>融资中 · A轮 · 人工智能</p>
      </div>
      <div class="stats">
        <div class="stat">
          <div class="stat-value">2</div>
          <div class="stat-label">我的项目</div>
        </div>
        <div class="stat">
          <div class="stat-value">5</div>
          <div class="stat-label">投递中</div>
        </div>
        <div class="stat">
          <div class="stat-value">12</div>
          <div class="stat-label">收到回复</div>
        </div>
      </div>
    </div>

    <!-- 快速入口 -->
    <el-row :gutter="16" class="quick-entries">
      <el-col :span="6" :lg="6" :md="12" :sm="12">
        <div class="entry-card" @click="router.push('/entrepreneur/project/new')">
          <div class="entry-icon blue"><el-icon :size="24"><Upload /></el-icon></div>
          <div class="entry-label">项目档案</div>
        </div>
      </el-col>
      <el-col :span="6" :lg="6" :md="12" :sm="12">
        <div class="entry-card" @click="router.push('/entrepreneur/diagnosis')">
          <div class="entry-icon green"><el-icon :size="24"><Monitor /></el-icon></div>
          <div class="entry-label">AI融资诊断</div>
        </div>
      </el-col>
      <el-col :span="6" :lg="6" :md="12" :sm="12">
        <div class="entry-card" @click="router.push('/entrepreneur/bp-assistant')">
          <div class="entry-icon purple"><el-icon :size="24"><ChatDotRound /></el-icon></div>
          <div class="entry-label">BP打磨助手</div>
        </div>
      </el-col>
      <el-col :span="6" :lg="6" :md="12" :sm="12">
        <div class="entry-card" @click="router.push('/entrepreneur/investors')">
          <div class="entry-icon orange"><el-icon :size="24"><Search /></el-icon></div>
          <div class="entry-label">找投资人</div>
        </div>
      </el-col>
    </el-row>

    <!-- 内容区两栏 -->
    <el-row :gutter="16" class="content-row">
      <!-- 我的项目 -->
      <el-col :span="16" :lg="16" :md="24" :sm="24">
        <div class="cft-card">
          <div class="card-header">
            <h3>我的项目</h3>
            <el-button type="primary" size="small" @click="router.push('/entrepreneur/project/new')">新建项目</el-button>
          </div>
          <el-table :data="projectList" style="width: 100%">
            <el-table-column prop="title" label="项目名称" min-width="180">
              <template #default="{ row }">
                <div class="project-name">
                  <span class="name">{{ row.title }}</span>
                  <el-tag :type="statusTag(row.status)" size="small" class="ml-2">{{ statusLabel(row.status) }}</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="stage" label="阶段" width="100">
              <template #default="{ row }">{{ row.stageLabel }}</template>
            </el-table-column>
            <el-table-column prop="amount" label="融资金额" width="120">
              <template #default="{ row }">{{ row.amount }} 万</template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link @click="router.push('/entrepreneur/project/new')">编辑</el-button>
                <el-button size="small" link type="primary" @click="router.push('/entrepreneur/diagnosis')">诊断</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- 右侧边栏 -->
      <el-col :span="8" :lg="8" :md="24" :sm="24">
        <div class="cft-card mb-4">
          <div class="card-header">
            <h3>AI 融资建议</h3>
            <el-icon class="ai-sparkles"><MagicStick /></el-icon>
          </div>
          <div class="ai-suggestion">
            <p class="suggestion-text">基于您的项目「智能诊断医疗系统」，建议优先对接 医疗健康产业基金、红杉中国等机构。</p>
            <el-button size="small" type="primary" plain>查看推荐机构 →</el-button>
          </div>
        </div>

        <div class="cft-card">
          <div class="card-header">
            <h3>最近动态</h3>
          </div>
          <div class="feed-list">
            <div class="feed-item" v-for="(item, i) in feedList" :key="i">
              <div class="feed-dot" :style="{ background: item.color }"></div>
              <div class="feed-content">
                <div class="feed-text">{{ item.text }}</div>
                <div class="feed-time">{{ item.time }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { Upload, Monitor, ChatDotRound, Search, MagicStick } from '@element-plus/icons-vue';
import type { TagType } from '@chuangrongtong/shared';

const router = useRouter();

const projectList = [
  { title: '智能诊断医疗系统', stage: 'series_a', stageLabel: 'A轮', amount: '5000', status: 'published' },
  { title: 'AI 智能客服 SaaS', stage: 'seed', stageLabel: '种子轮', amount: '1500', status: 'pending_review' },
];

const feedList = [
  { text: '红杉中国 已查看您的项目「智能诊断医疗系统」', time: '10分钟前', color: '#2563eb' },
  { text: '您投递的「达晨创投」项目收到回复', time: '2小时前', color: '#22c55e' },
  { text: '新路演活动：医疗健康闭门路演 3/25', time: '昨天', color: '#f59e0b' },
];

function statusTag(s: string): TagType {
  const map: Record<string, TagType> = { draft: 'info', pending_review: 'warning', review_pass: 'success', review_reject: 'danger', published: 'primary' };
  return map[s] || 'info';
}
function statusLabel(s: string) {
  const map: Record<string, string> = { draft: '草稿', pending_review: '待审核', review_pass: '审核通过', review_reject: '审核驳回', published: '已发布' };
  return map[s] || s;
}
</script>

<style lang="scss" scoped>
.entrepreneur-workspace {
  .hero-card {
    background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
    border-radius: 12px; padding: 28px 32px;
    display: flex; align-items: center; gap: 24px; margin-bottom: 20px;
    border: 1px solid #bfdbfe;
    .avatar-big { width: 72px; height: 72px; border-radius: 50%; background: white; display: flex; align-items: center; justify-content: center; font-size: 32px; flex-shrink: 0; }
    .info h2 { margin: 0 0 4px; font-size: 20px; }
    .info p { margin: 0; color: var(--cft-text-secondary); font-size: 14px; }
    .stats { margin-left: auto; display: flex; gap: 40px; }
    .stat { text-align: center; }
    .stat-value { font-size: 28px; font-weight: 700; color: var(--cft-primary); }
    .stat-label { font-size: 12px; color: var(--cft-text-secondary); }
  }
  .quick-entries { margin-bottom: 20px; }
  .entry-card {
    background: white; border-radius: 10px; padding: 20px; text-align: center;
    border: 1px solid var(--cft-border); cursor: pointer; transition: all 0.2s;
    &:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
  }
  .entry-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; color: white; }
  .entry-icon.blue { background: linear-gradient(135deg, #3b82f6, #2563eb); }
  .entry-icon.green { background: linear-gradient(135deg, #22c55e, #16a34a); }
  .entry-icon.purple { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
  .entry-icon.orange { background: linear-gradient(135deg, #f97316, #ea580c); }
  .entry-label { font-size: 14px; font-weight: 500; color: var(--cft-text-primary); }
  .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h3 { margin: 0; font-size: 16px; } }
  .ai-sparkles { color: var(--cft-primary); }
  .ai-suggestion { .suggestion-text { font-size: 14px; color: var(--cft-text-secondary); line-height: 1.8; margin: 0 0 16px; } }
  .feed-list { display: flex; flex-direction: column; gap: 12px; }
  .feed-item { display: flex; gap: 10px; align-items: flex-start; }
  .feed-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; margin-top: 6px; }
  .feed-text { font-size: 13px; color: var(--cft-text-primary); }
  .feed-time { font-size: 11px; color: var(--cft-text-muted); margin-top: 2px; }
  .ml-2 { margin-left: 8px; }
  .mb-4 { margin-bottom: 16px; }
  .project-name { display: flex; align-items: center; .name { font-weight: 500; } }
}

// 1024px 以下：PC 窄屏适配
@media (max-width: 1024px) {
  .entrepreneur-workspace {
    .hero-card {
      flex-wrap: wrap;
      padding: 20px 24px;
      .stats { margin-left: 0; width: 100%; justify-content: flex-start; gap: 24px; margin-top: 16px; }
    }
  }
}
</style>
