<template>
  <div class="roadshow-detail">
    <el-button link :icon="ArrowLeft" @click="router.back()" class="mb-4">返回列表</el-button>

    <div class="hero" :style="{ background: roadshow.coverGradient }">
      <el-tag v-if="roadshow.isClosedDoor" type="warning" effect="dark" size="large" class="mb-3">🔒 闭门路演</el-tag>
      <h1>{{ roadshow.title }}</h1>
      <p class="desc">{{ roadshow.description }}</p>
    </div>

    <el-row :gutter="20" class="mt-4">
      <el-col :span="16" :lg="16" :md="24" :sm="24">
        <div class="cft-card">
          <h3>🎯 活动介绍</h3>
          <p>本期人工智能闭门路演聚焦大模型应用落地、AI Agent、多模态等前沿方向。8 家精选项目将各获得 3 分钟 pitch + 5 分钟 Q&A 机会，直面来自红杉、达晨、经纬、启明等头部机构的投资人。</p>

          <el-divider />

          <h3>📋 活动议程</h3>
          <el-timeline>
            <el-timeline-item v-for="(item, i) in agenda" :key="i" :timestamp="item.time" placement="top">
              <h4>{{ item.title }}</h4>
              <p v-if="item.speaker">主讲人：{{ item.speaker }}</p>
            </el-timeline-item>
          </el-timeline>

          <el-divider />

          <h3>🎤 路演项目（8 家）</h3>
          <div class="project-list">
            <div v-for="p in roadshow.projects" :key="p.name" class="project-row">
              <div class="rank">{{ p.rank }}</div>
              <div class="p-info">
                <div class="p-name">{{ p.name }}</div>
                <div class="p-tag">{{ p.stage }} · {{ p.time }}min pitch</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="8" :lg="8" :md="24" :sm="24">
        <div class="cft-card sticky-panel">
          <h3>📌 活动信息</h3>
          <div class="info-list">
            <div class="info-item">
              <span class="label">时间</span>
              <span class="value">{{ roadshow.time }}</span>
            </div>
            <div class="info-item">
              <span class="label">地点</span>
              <span class="value">{{ roadshow.location }}</span>
            </div>
            <div class="info-item">
              <span class="label">已报名</span>
              <span class="value highlight">{{ roadshow.participants }} / {{ roadshow.maxParticipants }} 人</span>
            </div>
            <div class="info-item">
              <span class="label">主办方</span>
              <span class="value">创融通 · 慧联创融</span>
            </div>
          </div>

          <el-progress :percentage="(roadshow.participants / roadshow.maxParticipants) * 100" class="mt-4" />

          <el-button type="primary" size="large" class="w-full mt-4" @click="doRegister">
            🔔 我要报名
          </el-button>
          <el-button size="large" class="w-full mt-2">
            👀 加入收藏
          </el-button>
        </div>

        <div class="cft-card mt-4">
          <h4>🏢 参与机构</h4>
          <div class="inst-grid">
            <div v-for="inst in roadshow.institutions" :key="inst" class="inst-badge">{{ inst }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const id = route.params.id;

const roadshow = {
  id,
  title: '人工智能闭门路演 · 第12期',
  description: '聚焦大模型、AI agent、AIGC 方向，8 家精选项目限时 3 分钟 pitch，直面顶级投资机构',
  time: '2026-11-15 14:00 - 17:30',
  location: '上海 · 陆家嘴金融中心 28F',
  isClosedDoor: true,
  participants: 45,
  maxParticipants: 80,
  coverGradient: 'linear-gradient(135deg, #667eea, #764ba2)',
  institutions: ['红杉中国', '达晨创投', '经纬创投', '启明创投', 'IDG资本', '高瓴创投', '真格基金', '线性资本'],
  projects: [
    { rank: 1, name: '智能诊断医疗系统', stage: 'A轮', time: 3 },
    { rank: 2, name: 'AI Agent 协作平台', stage: 'Pre-Seed', time: 3 },
    { rank: 3, name: '多模态数据分析 SaaS', stage: '种子轮', time: 3 },
    { rank: 4, name: 'AIGC 设计工具', stage: 'A轮', time: 3 },
  ],
};

const agenda = [
  { time: '14:00 - 14:10', title: '开场致辞 & 嘉宾介绍', speaker: '主办方' },
  { time: '14:10 - 15:10', title: '上半场 Pitch（4 家）' },
  { time: '15:10 - 15:25', title: '茶歇 & 自由交流' },
  { time: '15:25 - 16:25', title: '下半场 Pitch（4 家）' },
  { time: '16:25 - 17:00', title: '圆桌讨论：AI 行业投资趋势' },
  { time: '17:00 - 17:30', title: '自由交流 & 合影' },
];

function doRegister() {
  ElMessage.success('报名成功！我们将审核您的申请，通过后发送确认邮件。');
}
</script>

<style lang="scss" scoped>
.roadshow-detail {
  .hero {
    padding: 40px; border-radius: 16px; color: white;
    h1 { font-size: 28px; margin: 0 0 12px; }
    .desc { margin: 0; opacity: 0.9; font-size: 14px; line-height: 1.8; }
  }
  h3 { margin: 0 0 16px; font-size: 16px; }
  h4 { margin: 0 0 8px; font-size: 14px; }
  .info-list { .info-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed var(--cft-border); &:last-child { border: none; } .label { color: var(--cft-text-muted); font-size: 13px; } .value { font-weight: 500; font-size: 13px; &.highlight { color: var(--cft-primary); } } } }
  .sticky-panel { position: sticky; top: 20px; }
  .project-list { .project-row { display: flex; align-items: center; gap: 14px; padding: 10px 0; border-bottom: 1px solid var(--cft-border); &:last-child { border: none; } .rank { width: 24px; height: 24px; border-radius: 50%; background: var(--cft-bg); color: var(--cft-text-secondary); display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; } .p-name { font-weight: 500; } .p-tag { font-size: 12px; color: var(--cft-text-muted); margin-top: 2px; } } }
  .inst-grid { display: flex; flex-wrap: wrap; gap: 8px; }
  .inst-badge { background: var(--cft-bg); padding: 6px 12px; border-radius: 16px; font-size: 12px; color: var(--cft-text-secondary); }
  .w-full { width: 100%; }
  .mt-2 { margin-top: 8px; }
  .mt-4 { margin-top: 16px; }
  .mb-3 { margin-bottom: 12px; }
  .mb-4 { margin-bottom: 16px; }
}
</style>
