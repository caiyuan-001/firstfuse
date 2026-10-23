<template>
  <div class="investor-detail">
    <el-button link :icon="ArrowLeft" @click="router.back()" class="mb-4">返回列表</el-button>

    <el-row :gutter="20">
      <!-- 左侧机构信息 -->
      <el-col :span="16" :lg="16" :md="24" :sm="24">
        <div class="cft-card">
          <div class="inst-header">
            <div class="inst-logo big">🌲</div>
            <div class="inst-info">
              <h1>红杉中国</h1>
              <p class="inst-desc">红杉中国成立于2005年，是全球领先的风险投资机构，专注于科技创新领域的早期和成长期投资。</p>
              <div class="inst-meta">
                <span><el-icon><Location /></el-icon> 上海 · 北京 · 深圳</span>
                <span><el-icon><Money /></el-icon> 管理规模 ¥500亿+</span>
                <span><el-icon><Link /></el-icon> sequoiacap.com</span>
              </div>
            </div>
          </div>
        </div>

        <div class="cft-card mt-4">
          <h3>📋 机构概况</h3>
          <p>红杉中国由沈南鹏和沈南鹏先生于2005年创立，专注于TMT、医疗健康、新能源、硬科技等领域的风险投资。红杉中国累计投资超过400家企业，其中超过80家成功上市，包括美团、拼多多、字节跳动、京东等行业龙头。</p>
        </div>

        <div class="cft-card mt-4">
          <h3>🎯 关注方向</h3>
          <div class="tag-group">
            <el-tag size="large" v-for="i in focusIndustries" :key="i">{{ i }}</el-tag>
          </div>
          <h4>投资阶段偏好</h4>
          <div class="tag-group">
            <el-tag type="info" v-for="s in preferredStages" :key="s">{{ s }}</el-tag>
          </div>
        </div>

        <div class="cft-card mt-4">
          <h3>👥 核心团队</h3>
          <el-row :gutter="16">
            <el-col :span="8" :lg="8" :md="12" :sm="24" v-for="m in team" :key="m.name">
              <div class="team-member">
                <div class="avatar-sm">{{ m.name[0] }}</div>
                <div>
                  <div class="name">{{ m.name }}</div>
                  <div class="position">{{ m.position }}</div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-col>

      <!-- 右侧投递面板 -->
      <el-col :span="8" :lg="8" :md="24" :sm="24">        <div class="cft-card sticky-panel">
          <h3>向该机构投递</h3>
          <el-form label-position="top">
            <el-form-item label="选择项目">
              <el-select placeholder="请选择要投递的项目" class="w-full">
                <el-option label="智能诊断医疗系统" value="1" />
                <el-option label="AI 智能客服 SaaS" value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="Cover Letter">
              <el-input type="textarea" :rows="5" placeholder="简要介绍你的项目亮点，为什么适合这家机构..." />
            </el-form-item>
            <el-button type="primary" size="large" class="w-full" @click="doApply">提交投递</el-button>
          </el-form>
        </div>

        <div class="cft-card mt-4">
          <h4>🤝 已投案例</h4>
          <div class="case-list">
            <div v-for="c in cases" :key="c.name" class="case-item">
              <span class="case-name">{{ c.name }}</span>
              <el-tag size="small">{{ c.stage }}</el-tag>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Location, Money, Link } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const instId = route.params.id;

const focusIndustries = ['人工智能', '硬科技', '企业软件/SaaS', '医疗健康', '新能源'];
const preferredStages = ['Pre-Seed', '种子轮', 'A轮', 'B轮'];
const team = [
  { name: '沈南鹏', position: '创始合伙人' },
  { name: '张泉灵', position: '合伙人' },
  { name: '红杉君', position: '投资总监' },
];
const cases = [
  { name: '字节跳动', stage: '战略投资' },
  { name: '美团点评', stage: 'E轮' },
  { name: '拼多多', stage: 'B轮' },
  { name: '京东物流', stage: 'Pre-IPO' },
];

function doApply() {
  ElMessage.success('投递成功！机构将在 48 小时内查看您的项目。');
}
</script>

<style lang="scss" scoped>
.investor-detail {
  .inst-header { display: flex; gap: 20px; .inst-logo.big { width: 72px; height: 72px; border-radius: 16px; font-size: 36px; } .inst-info h1 { margin: 0 0 8px; font-size: 22px; } .inst-desc { margin: 0 0 12px; color: var(--cft-text-secondary); font-size: 14px; line-height: 1.8; } .inst-meta { display: flex; gap: 24px; flex-wrap: wrap; span { display: inline-flex; align-items: center; gap: 4px; color: var(--cft-text-muted); font-size: 13px; } } }
  h3 { margin: 0 0 16px; font-size: 16px; }
  h4 { margin: 16px 0 10px; font-size: 14px; color: var(--cft-text-secondary); }
  .tag-group { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
  .team-member { display: flex; gap: 12px; align-items: center; padding: 12px; background: var(--cft-bg); border-radius: 8px; .avatar-sm { width: 40px; height: 40px; border-radius: 50%; background: var(--cft-primary); color: white; display: flex; align-items: center; justify-content: center; font-weight: 600; } .name { font-weight: 500; } .position { font-size: 12px; color: var(--cft-text-muted); } }
  .sticky-panel { position: sticky; top: 20px; }
  .case-list { display: flex; flex-direction: column; gap: 8px; }
  .case-item { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; border-bottom: 1px solid var(--cft-border); &:last-child { border: none; } }
  .w-full { width: 100%; }
  .mt-4 { margin-top: 16px; }
  .mb-4 { margin-bottom: 16px; }
}
</style>
