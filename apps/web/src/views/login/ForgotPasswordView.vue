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

      <!-- 右侧找回密码表单 -->
      <div class="form-side">
        <div class="form-wrapper">
          <h2 class="form-title">忘记密码</h2>
          <p class="form-desc">通过验证码重置您的登录密码</p>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-form-item label="手机号 / 邮箱" prop="account">
              <el-input v-model="form.account" placeholder="请输入注册时使用的手机号或邮箱" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div class="code-row">
                <el-input v-model="form.code" placeholder="请输入验证码" :prefix-icon="Message" size="large" />
                <el-button size="large" :disabled="countdown > 0" @click="sendCode" class="code-btn">
                  {{ countdown > 0 ? `${countdown}s 后重发` : '发送验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="新密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入新密码（6-20 位）" :prefix-icon="Lock" show-password size="large" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" :prefix-icon="Lock" show-password size="large" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="w-full" :loading="loading" @click="handleSubmit">
                重置密码
              </el-button>
            </el-form-item>
          </el-form>

          <div class="back-entry">
            <el-link type="primary" :underline="false" @click="router.push('/login')">
              <el-icon><ArrowLeft /></el-icon> 返回登录
            </el-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { User, Lock, Message, ArrowLeft } from '@element-plus/icons-vue';
import { httpPost } from '@/utils/request';
import { PLATFORM_INFO } from '@chuangrongtong/shared';

const router = useRouter();

const formRef = ref<FormInstance>();
const loading = ref(false);
const countdown = ref(0);
let timer: ReturnType<typeof setInterval> | null = null;

const form = reactive({
  account: '',
  code: '',
  password: '',
  confirmPassword: '',
});

const rules: FormRules = {
  account: [{ required: true, message: '请输入手机号或邮箱', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) callback(new Error('两次输入的密码不一致'));
        else callback();
      },
      trigger: 'blur',
    },
  ],
};

function sendCode() {
  if (!form.account) {
    ElMessage.warning('请先输入手机号或邮箱');
    return;
  }
  // Mock 发送验证码（前端模拟，Demo 固定验证码 123456）
  countdown.value = 60;
  timer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0 && timer) {
      clearInterval(timer);
      timer = null;
    }
  }, 1000);
  ElMessage.success('验证码已发送（Demo 模拟验证码：123456）');
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;
  try {
    await httpPost('/api/v1/auth/reset-password', {
      account: form.account,
      code: form.code,
      password: form.password,
    });
    ElMessage.success('密码重置成功，请使用新密码登录');
    router.push('/login');
  } finally {
    loading.value = false;
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
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
  .code-row {
    display: flex; gap: 8px; width: 100%;
    .el-input { flex: 1; }
    .code-btn { flex-shrink: 0; width: 120px; }
  }
  .back-entry { text-align: center; margin-top: 4px; }
  .w-full { width: 100%; }
}
</style>
