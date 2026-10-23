<template>
  <div class="login-page">
    <div class="login-bg"></div>
    <div class="login-container">
      <!-- 左侧品牌区 -->
      <div class="brand-side">
        <div class="brand-content">
          <div class="brand-logo">CR</div>
          <h1 class="brand-title">创融通 FirstFuse</h1>
          <p class="brand-subtitle">{{ PLATFORM_INFO.subtitle }} · {{ PLATFORM_INFO.sloganShort }}</p>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="form-side">
        <div class="form-wrapper">
          <h2 class="form-title">注册账号</h2>
          <p class="form-desc">选择您的身份，开启产融对接之旅</p>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-form-item label="注册身份" prop="role">
              <el-radio-group v-model="form.role" size="large">
                <el-radio-button value="entrepreneur">👤 创业者</el-radio-button>
                <el-radio-button value="investor">🏛️ 投资机构</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="form.role === 'investor'" label="机构名称" prop="organization">
              <el-input v-model="form.organization" placeholder="请输入所属投资机构名称" :prefix-icon="OfficeBuilding" size="large" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Iphone" size="large" />
            </el-form-item>
            <el-form-item label="设置密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请设置密码（6-20 位）" :prefix-icon="Lock" show-password size="large" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" :prefix-icon="Lock" show-password size="large" />
            </el-form-item>
            <el-form-item prop="agreed">
              <el-checkbox v-model="form.agreed">
                我已阅读并同意
                <el-link type="primary" :underline="false">《用户服务协议》</el-link>
                和
                <el-link type="primary" :underline="false">《隐私政策》</el-link>
              </el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="w-full" :loading="loading" @click="handleSubmit">
                注册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="back-entry">
            已有账号？<el-link type="primary" :underline="false" @click="router.push('/login')">直接登录</el-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { Lock, Iphone, OfficeBuilding } from '@element-plus/icons-vue';
import { httpPost } from '@/utils/request';
import { PLATFORM_INFO } from '@chuangrongtong/shared';

const router = useRouter();

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  role: 'entrepreneur' as 'entrepreneur' | 'investor',
  organization: '',
  phone: '',
  password: '',
  confirmPassword: '',
  agreed: false,
});

const rules: FormRules = {
  role: [{ required: true, message: '请选择注册身份', trigger: 'change' }],
  organization: [{ required: true, message: '请输入机构名称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) callback(new Error('两次输入的密码不一致'));
        else callback();
      },
      trigger: 'blur',
    },
  ],
  agreed: [
    {
      validator: (_rule, value, callback) => {
        if (!value) callback(new Error('请先阅读并同意用户协议'));
        else callback();
      },
      trigger: 'change',
    },
  ],
};

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;
  try {
    await httpPost('/api/v1/auth/register', {
      role: form.role,
      organization: form.role === 'investor' ? form.organization : undefined,
      phone: form.phone,
      password: form.password,
    });
    ElMessage.success('注册成功，请登录');
    router.push('/login');
  } finally {
    loading.value = false;
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  .login-bg {
    position: absolute; inset: 0;
    background:
      radial-gradient(ellipse at 20% 50%, rgba(37, 99, 235, 0.15) 0%, transparent 50%),
      radial-gradient(ellipse at 80% 80%, rgba(34, 197, 94, 0.1) 0%, transparent 40%),
      linear-gradient(135deg, #f5f7fa 0%, #e8edf5 100%);
  }
  .login-container {
    position: relative; z-index: 1;
    display: flex; min-height: 100vh;
    max-width: 1200px; margin: 0 auto; padding: 40px;
    gap: 48px; align-items: center;
  }
  .brand-side {
    flex: 1; min-width: 0;
    .brand-content { display: flex; flex-direction: column; gap: 16px; }
    .brand-logo {
      width: 64px; height: 64px; border-radius: 16px;
      background: linear-gradient(135deg, #3b82f6, #1d4ed8);
      color: white; font-size: 24px; font-weight: bold;
      display: flex; align-items: center; justify-content: center;
      box-shadow: 0 10px 30px rgba(37, 99, 235, 0.3);
    }
    .brand-title { font-size: 36px; font-weight: 700; color: var(--cft-text-primary); margin: 0; }
    .brand-subtitle { font-size: 18px; color: var(--cft-text-secondary); margin: 0; }
  }
  .form-side {
    width: 420px; flex-shrink: 0;
    background: white; border-radius: 16px;
    border: 1px solid var(--cft-border);
    padding: 40px;
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.08);
  }
  .form-title { font-size: 24px; font-weight: 600; margin: 0 0 4px; }
  .form-desc { color: var(--cft-text-muted); margin: 0 0 24px; font-size: 14px; }
  .back-entry { text-align: center; font-size: 14px; color: var(--cft-text-muted); margin-top: 4px; }
  .w-full { width: 100%; }
}
</style>
