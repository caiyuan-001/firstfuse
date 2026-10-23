<template>
  <div class="project-form">
    <div class="page-header">
      <h1>项目档案</h1>
      <p>填写项目核心信息，上传 BP 文件，AI 将自动为您预填部分内容</p>
    </div>

    <el-steps :active="step" class="mb-6">
      <el-step title="基础信息" />
      <el-step title="商业计划" />
      <el-step title="融资需求" />
      <el-step title="团队 & 市场" />
      <el-step title="BP 上传" />
      <el-step title="提交审核" />
    </el-steps>

    <div class="form-container">
      <el-form :model="form" label-width="120px" class="project-form-content">
        <div class="form-section">
          <h3><el-icon><Document /></el-icon> 基础信息</h3>
          <el-form-item label="项目名称">
            <el-input v-model="form.title" placeholder="例如：智能诊断医疗系统" />
          </el-form-item>
          <el-form-item label="一句话介绍">
            <el-input v-model="form.subtitle" placeholder="用一句话描述您的项目" />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="8" :lg="8" :md="12" :sm="24">
              <el-form-item label="所属行业">
                <el-select v-model="form.industry" placeholder="请选择" class="w-full">
                  <el-option v-for="i in industryOptions" :key="i.value" :label="i.label" :value="i.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8" :lg="8" :md="12" :sm="24">
              <el-form-item label="所在地区">
                <el-select v-model="form.region" placeholder="请选择" class="w-full">
                  <el-option v-for="r in regionOptions" :key="r.value" :label="r.label" :value="r.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8" :lg="8" :md="12" :sm="24">
              <el-form-item label="成立时间">
                <el-date-picker v-model="form.foundedAt" type="month" placeholder="选择成立月份" class="w-full" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section">
          <h3><el-icon><TrendCharts /></el-icon> 融资需求</h3>
          <el-row :gutter="16">
            <el-col :span="8" :lg="8" :md="12" :sm="24">
              <el-form-item label="融资阶段">
                <el-select v-model="form.stage" placeholder="请选择" class="w-full">
                  <el-option v-for="s in stageOptions" :key="s.value" :label="s.label" :value="s.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8" :lg="8" :md="12" :sm="24">
              <el-form-item label="融资金额(万元)">
                <el-input-number v-model="form.amountSought" :min="100" :step="500" class="w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="8" :lg="8" :md="12" :sm="24">
              <el-form-item label="出让股权(%)">
                <el-input-number v-model="form.equityOffered" :min="5" :max="40" :step="1" class="w-full" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section">
          <h3><el-icon><Files /></el-icon> BP 文件上传</h3>
          <el-upload
            class="upload-area"
            drag
            :auto-upload="false"
            :limit="1"
            accept=".pdf,.ppt,.pptx,.doc,.docx"
            :on-change="handleFileChange"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">将 BP 文件拖到此处，或 <em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 PDF/PPT/Word 格式，单个文件不超过 50MB</div>
            </template>
          </el-upload>
          <div v-if="uploadedFile" class="uploaded-info">
            <el-tag type="success"><el-icon><Document /></el-icon> {{ uploadedFile.name }}</el-tag>
            <span class="ml-2 text-muted">AI 正在分析您的 BP，完成后将自动预填表单...</span>
          </div>
        </div>
      </el-form>

      <div class="form-footer">
        <el-button @click="router.back()">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          保存并提交审核
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { UploadFile } from 'element-plus';
import { Document, TrendCharts, Files, UploadFilled } from '@element-plus/icons-vue';
import { INDUSTRY_OPTIONS, REGION_OPTIONS, STAGE_OPTIONS } from '@chuangrongtong/shared';

const router = useRouter();
const submitting = ref(false);
const step = ref(0);
const uploadedFile = ref<UploadFile | null>(null);

const form = reactive({
  title: '',
  subtitle: '',
  industry: '',
  region: '',
  foundedAt: '',
  stage: '',
  amountSought: 1000,
  equityOffered: 15,
});

const industryOptions = INDUSTRY_OPTIONS;
const regionOptions = REGION_OPTIONS;
const stageOptions = STAGE_OPTIONS;

function handleFileChange(file: UploadFile) {
  uploadedFile.value = file;
  ElMessage.success(`已选择文件：${file.name}（Mock 模式，仅模拟上传）`);
}

async function handleSubmit() {
  submitting.value = true;
  await new Promise(r => setTimeout(r, 800));
  submitting.value = false;
  ElMessage.success('项目档案已保存，提交审核成功！');
  router.push('/entrepreneur/workspace');
}
</script>

<style lang="scss" scoped>
.project-form {
  .page-header { margin-bottom: 24px; h1 { margin: 0 0 4px; font-size: 24px; } p { margin: 0; color: var(--cft-text-secondary); font-size: 14px; } }
  .form-container {
    background: white; border-radius: 12px; padding: 32px;
    border: 1px solid var(--cft-border);
  }
  .form-section { margin-bottom: 32px; h3 { font-size: 16px; margin: 0 0 20px; display: flex; align-items: center; gap: 8px; color: var(--cft-text-primary); } }
  .upload-area { margin-bottom: 12px; }
  .uploaded-info { display: flex; align-items: center; .text-muted { color: var(--cft-text-muted); font-size: 13px; } }
  .form-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 32px; padding-top: 20px; border-top: 1px solid var(--cft-border); }
  .w-full { width: 100%; }
  .ml-2 { margin-left: 8px; }
  .mb-6 { margin-bottom: 24px; }
}
</style>
