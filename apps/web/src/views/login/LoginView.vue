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
          <div class="brand-features">
            <div class="feature-item">
              <el-icon :size="20"><Cpu /></el-icon>
              <span>AI 智能融资诊断</span>
            </div>
            <div class="feature-item">
              <el-icon :size="20"><MagicStick /></el-icon>
              <span>智能 BP 打磨助手</span>
            </div>
            <div class="feature-item">
              <el-icon :size="20"><Share /></el-icon>
              <span>企业关系图谱可视化</span>
            </div>
            <div class="feature-item">
              <el-icon :size="20"><Calendar /></el-icon>
              <span>闭门路演高效撮合</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-side">
        <div class="form-wrapper">
          <h2 class="form-title">欢迎登录</h2>
          <p class="form-desc">选择身份快速体验</p>

          <el-tabs v-model="activeRole" class="role-tabs">
            <el-tab-pane label="创业者" name="entrepreneur" />
            <el-tab-pane label="投资机构" name="investor" />
            <el-tab-pane label="平台运营" name="platform_admin" />
          </el-tabs>

          <el-form ref="formRef" :model="form" label-position="top" class="login-form">
            <el-form-item label="手机号 / 账号">
              <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item>
              <template #label>
                <div class="password-label">
                  <span>密码</span>
                  <el-link type="primary" :underline="false" @click="router.push('/forgot-password')">忘记密码？</el-link>
                </div>
              </template>
              <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password size="large" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="w-full" :loading="loading" @click="handleLogin">
                登录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="register-entry">
            还没有账号？<el-link type="primary" :underline="false" @click="router.push('/register')">注册账号</el-link>
          </div>

          <div class="quick-login">
            <p class="quick-title">快速体验（一键登录）</p>
            <div class="quick-buttons">
              <el-button @click="quickLogin('entrepreneur')" plain>👤 创业者体验</el-button>
              <el-button @click="quickLogin('investor')" plain>🏛️ 机构体验</el-button>
              <el-button @click="quickLogin('platform_admin')" plain type="danger">⚙️ 运营体验</el-button>
            </div>
          </div>

          <div class="wechat-login">
            <el-button size="large" class="w-full" plain>
              <el-icon><ChatDotRound /></el-icon>
              微信授权登录（创业者端）
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, Cpu, MagicStick, Share, Calendar, ChatDotRound } from '@element-plus/icons-vue';
import { useUserStore } from '@/store';
import { httpPost } from '@/utils/request';
import { PLATFORM_INFO, type User as UserInfo } from '@chuangrongtong/shared';

interface LoginResult {
  token: string;
  user: UserInfo;
}

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const loading = ref(false);
const activeRole = ref<'entrepreneur' | 'investor' | 'platform_admin'>('investor');
const form = ref({
  username: 'demo',
  password: 'demo',
});

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入账号和密码');
    return;
  }
  await doLogin(activeRole.value);
}

async function quickLogin(role: 'entrepreneur' | 'investor' | 'platform_admin') {
  activeRole.value = role;
  await doLogin(role);
}

async function doLogin(role: string) {
  loading.value = true;
  try {
    const res = await httpPost<LoginResult>('/api/v1/auth/login', { role });
    userStore.login({ token: res.data.token, user: res.data.user });

    const pathMap: Record<string, string> = {
      entrepreneur: '/entrepreneur/workspace',
      investor: '/institution/dashboard',
      platform_admin: '/admin/overview',
    };

    ElMessage.success(`欢迎回来，${res.data.user.nickname}！`);

    const redirect = (route.query.redirect as string) || pathMap[role];
    router.push(redirect);
  } catch {
    // 错误提示由请求拦截器统一处理
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
    .brand-subtitle { font-size: 18px; color: var(--cft-text-secondary); margin: 0 0 16px; }
    .brand-features {
      display: grid; grid-template-columns: 1fr 1fr; gap: 12px;
      .feature-item {
        display: flex; align-items: center; gap: 10px;
        padding: 12px 16px; background: white; border-radius: 10px;
        border: 1px solid var(--cft-border);
        color: var(--cft-text-secondary); font-size: 14px;
      }
    }
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
  .role-tabs { margin-bottom: 24px; }
  .login-form { .w-full { width: 100%; } }
  .password-label { display: flex; justify-content: space-between; align-items: center; width: 100%; }
  .register-entry { text-align: center; font-size: 14px; color: var(--cft-text-muted); margin-top: -4px; }
  .quick-login {
    margin-top: 24px; padding-top: 20px;
    border-top: 1px dashed var(--cft-border);
    .quick-title { font-size: 13px; color: var(--cft-text-muted); margin: 0 0 12px; }
    .quick-buttons { display: flex; flex-wrap: wrap; gap: 8px; }
  }
  .wechat-login { margin-top: 20px; }
  .w-full { width: 100%; }
}
</style>
