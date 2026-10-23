<template>
  <div class="entrepreneur-profile">
    <el-row :gutter="20">
      <!-- 左侧 -->
      <el-col :span="8" :lg="8" :md="24" :sm="24">
        <div class="cft-card profile-card">
          <div class="avatar-big">👤</div>
          <h2>张创业</h2>
          <p class="company">智能诊断医疗系统 · 创始人/CEO</p>
          <div class="verification">
            <el-tag type="success" size="small"><el-icon><CircleCheck /></el-icon> 实名认证</el-tag>
            <el-tag type="success" size="small"><el-icon><CircleCheck /></el-icon> 企业认证</el-tag>
          </div>
          <el-divider />
          <div class="stats">
            <div class="stat-item"><strong>2</strong><span>我的项目</span></div>
            <div class="stat-item"><strong>5</strong><span>投递中</span></div>
            <div class="stat-item"><strong>3</strong><span>路演报名</span></div>
          </div>
        </div>

        <div class="cft-card mt-4">
          <h4>⚙️ 设置</h4>
          <ul class="setting-list">
            <li><el-icon><User /></el-icon> 个人资料</li>
            <li><el-icon><Lock /></el-icon> 安全设置</li>
            <li><el-icon><Bell /></el-icon> 消息通知</li>
            <li><el-icon><CreditCard /></el-icon> 会员升级</li>
            <li><el-icon><QuestionFilled /></el-icon> 帮助中心</li>
          </ul>
        </div>
      </el-col>

      <!-- 右侧 -->
      <el-col :span="16" :lg="16" :md="24" :sm="24">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="我的投递" name="applications">
            <div class="cft-card">
              <div v-for="app in applications" :key="app.id" class="app-item">
                <div class="app-info">
                  <div class="app-title">{{ app.institutionName }}</div>
                  <div class="app-meta">投递时间：{{ app.time }} · 项目：{{ app.projectName }}</div>
                </div>
                <el-tag :type="statusType(app.status)">{{ app.statusLabel }}</el-tag>
                <el-button size="small" link>查看详情</el-button>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="消息通知" name="messages">
            <div class="cft-card">
              <div v-for="msg in messages" :key="msg.id" class="msg-item">
                <div class="msg-dot" :class="{ unread: !msg.read }"></div>
                <div class="msg-content">
                  <div class="msg-title">{{ msg.title }}</div>
                  <div class="msg-desc">{{ msg.desc }}</div>
                  <div class="msg-time">{{ msg.time }}</div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="订单中心" name="orders">
            <div class="cft-card">
              <el-empty description="暂无订单" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="知识库" name="knowledge">
            <div class="cft-card">
              <h3>融资知识快速检索</h3>
              <el-input placeholder="搜索融资相关知识..." class="mb-4" />
              <el-row :gutter="12">
                <el-col :span="8" :lg="8" :md="12" :sm="24" v-for="k in knowledgeList" :key="k.title">
                  <div class="knowledge-item">
                    <div class="k-icon">{{ k.icon }}</div>
                    <div class="k-title">{{ k.title }}</div>
                    <div class="k-desc">{{ k.desc }}</div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { CircleCheck, User, Lock, Bell, CreditCard, QuestionFilled } from '@element-plus/icons-vue';
import type { TagType } from '@chuangrongtong/shared';

const activeTab = ref('applications');

const applications = [
  { id: '1', institutionName: '红杉中国', projectName: '智能诊断医疗系统', status: 'viewed', statusLabel: '已查看', time: '2026-10-15' },
  { id: '2', institutionName: '达晨创投', projectName: '智能诊断医疗系统', status: 'shortlisted', statusLabel: '已初选', time: '2026-10-12' },
  { id: '3', institutionName: 'IDG资本', projectName: 'AI 智能客服 SaaS', status: 'submitted', statusLabel: '已投递', time: '2026-10-18' },
];

const messages = [
  { id: '1', title: '红杉中国 已查看您的项目', desc: '机构正在评估您的「智能诊断医疗系统」', time: '10分钟前', read: false },
  { id: '2', title: '路演报名通过', desc: '「人工智能闭门路演 · 第12期」报名已通过', time: '2小时前', read: false },
  { id: '3', title: 'AI 融资诊断报告更新', desc: '您的项目综合评分提升了 3 分', time: '昨天', read: true },
];

const knowledgeList = [
  { icon: '📖', title: 'BP 写作指南', desc: '投资人眼中好 BP 的 10 个标准' },
  { icon: '💹', title: '估值方法', desc: '创业公司常见估值方法解析' },
  { icon: '📋', title: 'Term Sheet 解读', desc: '融资条款逐条解读与谈判要点' },
];

function statusType(s: string): TagType {
  const map: Record<string, TagType> = { submitted: 'info', viewed: 'primary', shortlisted: 'success', interview: 'warning', term_sheet: 'primary', rejected: 'danger', accepted: 'success' };
  return map[s] || 'info';
}
</script>

<style lang="scss" scoped>
.entrepreneur-profile {
  .profile-card { text-align: center; .avatar-big { width: 80px; height: 80px; border-radius: 50%; background: var(--cft-bg); display: flex; align-items: center; justify-content: center; font-size: 36px; margin: 0 auto 12px; } h2 { margin: 0 0 4px; } .company { margin: 0 0 12px; color: var(--cft-text-secondary); font-size: 13px; } .verification { display: flex; gap: 6px; justify-content: center; } }
  .stats { display: flex; .stat-item { flex: 1; text-align: center; strong { display: block; font-size: 20px; color: var(--cft-primary); } span { font-size: 12px; color: var(--cft-text-muted); } } }
  .setting-list { list-style: none; padding: 0; margin: 0; li { display: flex; align-items: center; gap: 10px; padding: 10px 0; font-size: 14px; cursor: pointer; border-bottom: 1px solid var(--cft-border); &:last-child { border: none; } &:hover { color: var(--cft-primary); } } }
  .app-item { display: flex; align-items: center; gap: 16px; padding: 16px; border: 1px solid var(--cft-border); border-radius: 8px; margin-bottom: 12px; .app-info { flex: 1; .app-title { font-weight: 500; margin-bottom: 4px; } .app-meta { font-size: 12px; color: var(--cft-text-muted); } } }
  .msg-item { display: flex; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--cft-border); .msg-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--cft-border); margin-top: 6px; flex-shrink: 0; &.unread { background: var(--cft-primary); } } .msg-title { font-weight: 500; font-size: 14px; } .msg-desc { font-size: 13px; color: var(--cft-text-secondary); margin-top: 2px; } .msg-time { font-size: 11px; color: var(--cft-text-muted); margin-top: 4px; } }
  .knowledge-item { padding: 16px; background: var(--cft-bg); border-radius: 8px; margin-bottom: 12px; .k-icon { font-size: 24px; margin-bottom: 8px; } .k-title { font-weight: 500; font-size: 14px; margin-bottom: 4px; } .k-desc { font-size: 12px; color: var(--cft-text-muted); } }
  .mt-4 { margin-top: 16px; }
  .mb-4 { margin-bottom: 16px; }
}
</style>
