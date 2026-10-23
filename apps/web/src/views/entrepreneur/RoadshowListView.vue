<template>
  <div class="roadshow-list">
    <div class="page-header">
      <h1>路演活动</h1>
      <p>浏览最新产业闭门路演，与顶级投资机构面对面</p>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-tabs">
      <el-radio-group v-model="statusFilter" size="large">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="published">进行中</el-radio-button>
        <el-radio-button value="ongoing">即将开始</el-radio-button>
        <el-radio-button value="completed">已结束</el-radio-button>
      </el-radio-group>
    </div>

    <el-row :gutter="20">
      <el-col :span="12" :lg="12" :md="12" :sm="24" v-for="r in list" :key="r.id">
        <div class="roadshow-card" @click="router.push(`/entrepreneur/roadshows/${r.id}`)">
          <div class="cover">
            <div class="cover-img" :style="{ background: r.coverGradient }"></div>
            <el-tag v-if="r.isClosedDoor" type="warning" size="small" class="closed-door">闭门</el-tag>
            <el-tag :type="r.statusType" size="small" class="status">{{ r.statusLabel }}</el-tag>
          </div>
          <div class="body">
            <div class="title-row">
              <h3>{{ r.title }}</h3>
            </div>
            <div class="meta">
              <span><el-icon><Calendar /></el-icon> {{ r.time }}</span>
              <span><el-icon><Location /></el-icon> {{ r.location }}</span>
            </div>
            <p class="desc">{{ r.description }}</p>
            <div class="footer">
              <div class="participants">
                <el-avatar v-for="(av, i) in r.avatars" :key="i" :size="24" :src="av" />
                <span class="participant-count">{{ r.participants }} 人已报名</span>
              </div>
              <el-button size="small" type="primary">查看详情 →</el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Calendar, Location } from '@element-plus/icons-vue';
import type { TagType } from '@chuangrongtong/shared';

const router = useRouter();
const statusFilter = ref('all');

interface RoadshowCard {
  id: string; title: string; time: string; location: string; description: string;
  isClosedDoor: boolean; statusType: TagType; statusLabel: string;
  participants: number; avatars: string[]; coverGradient: string;
}

const list: RoadshowCard[] = [
  { id: '1', title: '人工智能闭门路演 · 第12期', time: '2026-11-15 14:00', location: '上海 · 陆家嘴', description: '聚焦大模型、AI agent、AIGC 方向，8 家项目限时 3 分钟 pitch', isClosedDoor: true, statusType: 'success', statusLabel: '即将开始', participants: 45, avatars: [], coverGradient: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { id: '2', title: '新能源产业专场路演', time: '2026-11-20 10:00', location: '线上 · 腾讯会议', description: '聚焦储能、氢能、固态电池等前沿方向，红杉、高瓴等机构参与', isClosedDoor: false, statusType: 'primary', statusLabel: '进行中', participants: 128, avatars: [], coverGradient: 'linear-gradient(135deg, #11998e, #38ef7d)' },
  { id: '3', title: '医疗健康创新路演', time: '2026-12-05 14:00', location: '北京 · 中关村', description: '精准医疗、创新药、医疗 AI 专场，10 家精选项目', isClosedDoor: true, statusType: 'warning', statusLabel: '即将开始', participants: 62, avatars: [], coverGradient: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { id: '4', title: '半导体/芯片产业闭门路演', time: '2026-12-10 09:30', location: '深圳 · 南山', description: '国产替代核心赛道，EDA、AI 芯片、先进封装方向', isClosedDoor: true, statusType: 'info', statusLabel: '报名中', participants: 38, avatars: [], coverGradient: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
];
</script>

<style lang="scss" scoped>
.roadshow-list {
  .page-header { margin-bottom: 20px; h1 { margin: 0 0 4px; font-size: 24px; } p { margin: 0; color: var(--cft-text-secondary); font-size: 14px; } }
  .filter-tabs { margin-bottom: 20px; }
  .roadshow-card {
    background: white; border: 1px solid var(--cft-border); border-radius: 12px;
    overflow: hidden; margin-bottom: 20px; cursor: pointer; transition: all 0.2s;
    &:hover { transform: translateY(-2px); box-shadow: 0 12px 32px rgba(0,0,0,0.08); border-color: var(--cft-primary); }
  }
  .cover { position: relative; height: 140px; .cover-img { width: 100%; height: 100%; } .closed-door { position: absolute; top: 12px; left: 12px; } .status { position: absolute; top: 12px; right: 12px; } }
  .body { padding: 16px 20px; }
  .title-row h3 { margin: 0 0 10px; font-size: 16px; }
  .meta { display: flex; gap: 20px; font-size: 12px; color: var(--cft-text-muted); margin-bottom: 10px; span { display: inline-flex; align-items: center; gap: 4px; } }
  .desc { font-size: 13px; color: var(--cft-text-secondary); line-height: 1.6; margin: 0 0 14px; height: 44px; overflow: hidden; }
  .footer { display: flex; justify-content: space-between; align-items: center; }
  .participants { display: flex; align-items: center; .participant-count { font-size: 12px; color: var(--cft-text-muted); margin-left: 8px; } }
}
</style>
