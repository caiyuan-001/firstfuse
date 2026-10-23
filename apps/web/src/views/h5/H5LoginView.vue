<template>
  <div class="h5-login-shell">
    <div class="h5-login-frame">
      <!-- 品牌头部 -->
      <header class="hero">
        <div class="logo">CR</div>
        <h1>创融通 FirstFuse</h1>
        <p class="slogan">{{ PLATFORM_INFO.sloganShort }}</p>
      </header>

      <!-- 表单面板 -->
      <section class="panel">
        <div class="mode-tabs">
          <div class="mode-tab" :class="{ active: mode === 'login' }" @click="switchMode('login')">登录</div>
          <div class="mode-tab" :class="{ active: mode === 'register' }" @click="switchMode('register')">注册</div>
        </div>

        <!-- forgot 模式：重置密码 -->
        <template v-if="mode === 'forgot'">
          <div class="mode-hint">请输入手机号、验证码和新密码</div>
          <div class="field">
            <span class="prefix">+86</span>
            <input v-model.trim="form.phone" type="tel" maxlength="11" placeholder="请输入手机号" />
          </div>
          <div class="field">
            <input v-model.trim="form.code" maxlength="6" inputmode="numeric" placeholder="请输入验证码" />
            <button class="send-btn" :disabled="countdown > 0" @click="sendCode">
              {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
            </button>
          </div>
          <div class="field">
            <input v-model="form.newPassword" type="password" placeholder="请输入新密码（6-20位）" />
          </div>
          <div class="field">
            <input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" />
          </div>
          <button class="submit-btn" :disabled="loading" @click="handleSubmit">
            {{ loading ? '处理中...' : '重置密码' }}
          </button>
          <p class="back-login" @click="switchMode('login')">← 返回登录</p>
        </template>

        <!-- login/register 模式 -->
        <template v-else>
          <div class="field">
            <span class="prefix">+86</span>
            <input v-model.trim="form.phone" type="tel" maxlength="11" placeholder="请输入手机号" />
          </div>

          <div v-if="mode === 'login' && loginMethod === 'password'" class="field">
            <input v-model="form.password" type="password" placeholder="请输入密码" />
          </div>
          <div v-else class="field">
            <input v-model.trim="form.code" maxlength="6" inputmode="numeric" placeholder="请输入验证码" />
            <button class="send-btn" :disabled="countdown > 0" @click="sendCode">
              {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
            </button>
          </div>

          <div v-if="mode === 'register'" class="field">
            <input v-model.trim="form.inviteCode" placeholder="邀请码（选填）" />
          </div>

          <div v-if="mode === 'login'" class="login-method">
            <span @click="toggleMethod">{{ loginMethod === 'code' ? '使用密码登录' : '使用验证码登录' }}</span>
            <span class="forget" @click="switchMode('forgot')">忘记密码</span>
          </div>

          <label v-if="mode === 'register'" class="agree">
            <input v-model="form.agree" type="checkbox" />
            <span>我已阅读并同意 <em>《用户协议》</em> 与 <em>《隐私政策》</em></span>
          </label>

          <button class="submit-btn" :disabled="loading" @click="handleSubmit">
            {{ loading ? '处理中...' : mode === 'login' ? '登 录' : '注册并登录' }}
          </button>
        </template>

        <template v-if="mode !== 'forgot'">
          <button class="ghost-btn" :disabled="loading" @click="quickLogin">创业者一键体验</button>

          <div class="divider"><span>其他方式</span></div>
          <button class="wechat-btn" @click="onWechat">
            <el-icon :size="18"><ChatDotRound /></el-icon>
            微信授权登录
          </button>
        </template>

        <p class="tip">演示环境：验证码固定为 <strong>123456</strong>，手机号任意填写</p>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ChatDotRound } from '@element-plus/icons-vue';
import { useUserStore } from '@/store';
import { httpPost } from '@/utils/request';
import { PLATFORM_INFO, type User } from '@chuangrongtong/shared';

interface LoginResult {
  token: string;
  user: User;
}

const router = useRouter();
const userStore = useUserStore();

const mode = ref<'login' | 'register' | 'forgot'>('login');
const loginMethod = ref<'code' | 'password'>('code');
const loading = ref(false);
const countdown = ref(0);

const form = reactive({
  phone: '',
  code: '',
  password: '',
  newPassword: '',
  confirmPassword: '',
  inviteCode: '',
  agree: false,
});

let timer: ReturnType<typeof setInterval> | null = null;

const phoneValid = computed(() => /^1[3-9]\d{9}$/.test(form.phone));

function switchMode(next: 'login' | 'register' | 'forgot') {
  mode.value = next;
}

function toggleMethod() {
  loginMethod.value = loginMethod.value === 'code' ? 'password' : 'code';
}

function sendCode() {
  if (!phoneValid.value) {
    ElMessage.warning('请输入正确的手机号');
    return;
  }
  ElMessage.success('验证码已发送（演示环境请填写 123456）');
  countdown.value = 60;
  timer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0 && timer) {
      clearInterval(timer);
      timer = null;
    }
  }, 1000);
}

/** 登录成功后写入全局状态 */
function applyLogin(result: LoginResult) {
  userStore.login({ token: result.token, user: result.user });
}

async function handleSubmit() {
  if (!phoneValid.value) {
    ElMessage.warning('请输入正确的手机号');
    return;
  }
  if (mode.value === 'register' && !form.agree) {
    ElMessage.warning('请先阅读并同意用户协议与隐私政策');
    return;
  }

  // forgot 模式：重置密码
  if (mode.value === 'forgot') {
    if (!form.code) {
      ElMessage.warning('请输入验证码');
      return;
    }
    if (!form.newPassword || form.newPassword.length < 6) {
      ElMessage.warning('密码至少 6 位');
      return;
    }
    if (form.newPassword !== form.confirmPassword) {
      ElMessage.warning('两次密码输入不一致');
      return;
    }
    loading.value = true;
    try {
      await httpPost('/api/v1/auth/reset-password', {
        phone: form.phone,
        code: form.code,
        newPassword: form.newPassword,
      });
      ElMessage.success('密码已重置，请使用新密码登录');
      // 清空 forgot 相关字段，切回登录模式
      form.code = '';
      form.newPassword = '';
      form.confirmPassword = '';
      switchMode('login');
      // 自动切到密码登录方便用户输入新密码
      loginMethod.value = 'password';
    } catch {
      // 错误提示由请求拦截器统一处理
    } finally {
      loading.value = false;
    }
    return;
  }

  const usePassword = mode.value === 'login' && loginMethod.value === 'password';
  if (usePassword && !form.password) {
    ElMessage.warning('请输入密码');
    return;
  }
  if (!usePassword && !form.code) {
    ElMessage.warning('请输入验证码');
    return;
  }

  loading.value = true;
  try {
    const res = await httpPost<LoginResult>('/api/v1/auth/login', {
      role: 'entrepreneur',
      phone: form.phone,
      code: form.code,
      password: form.password,
      register: mode.value === 'register',
    });
    applyLogin(res.data);
    ElMessage.success(
      mode.value === 'login' ? `欢迎回来，${res.data.user.nickname}` : '注册成功，欢迎加入创融通',
    );
    router.replace('/h5/home');
  } catch {
    // 错误提示由请求拦截器统一处理
  } finally {
    loading.value = false;
  }
}

/** 演示用一键登录，跳过表单校验 */
async function quickLogin() {
  if (loading.value) return;
  loading.value = true;
  try {
    const res = await httpPost<LoginResult>('/api/v1/auth/login', { role: 'entrepreneur' });
    applyLogin(res.data);
    ElMessage.success('已进入创业者端体验');
    router.replace('/h5/home');
  } catch {
    // 错误提示由请求拦截器统一处理
  } finally {
    loading.value = false;
  }
}

function onWechat() {
  ElMessage.info('微信授权登录为 UI 占位，后续接入');
}

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style lang="scss" scoped>
.h5-login-shell {
  min-height: 100vh;
  background: #eef2f7;
  display: flex;
  justify-content: center;
}
.h5-login-frame {
  width: 100%;
  max-width: 430px;
  min-height: 100vh;
  background: var(--cft-bg);
  display: flex;
  flex-direction: column;
  box-shadow: 0 0 48px rgba(15, 23, 42, 0.12);
}
.hero {
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
  color: #fff;
  padding: 60px 28px 44px;
  .logo {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    background: rgba(255, 255, 255, 0.18);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 700;
    margin-bottom: 18px;
  }
  h1 {
    font-size: 24px;
    font-weight: 700;
    margin: 0 0 8px;
  }
  .slogan {
    font-size: 13px;
    opacity: 0.8;
    margin: 0;
  }
}
.panel {
  flex: 1;
  margin-top: -22px;
  background: #fff;
  border-radius: 22px 22px 0 0;
  padding: 26px 24px 34px;
}
.mode-tabs {
  display: flex;
  gap: 28px;
  margin-bottom: 22px;
}
.mode-tab {
  font-size: 17px;
  color: var(--cft-text-muted);
  padding-bottom: 8px;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  transition: all 0.18s;
  &.active {
    color: var(--cft-text-primary);
    font-weight: 600;
    border-bottom-color: var(--cft-primary);
  }
}
.field {
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 14px;
  margin-bottom: 12px;
  background: #f6f8fb;
  border-radius: 12px;
  .prefix {
    font-size: 14px;
    color: var(--cft-text-secondary);
    padding-right: 10px;
    margin-right: 10px;
    border-right: 1px solid var(--cft-border);
  }
  input {
    flex: 1;
    min-width: 0;
    border: none;
    outline: none;
    background: transparent;
    font-size: 15px;
    color: var(--cft-text-primary);
    &::placeholder {
      color: #a3aec0;
    }
  }
}
.send-btn {
  border: none;
  background: transparent;
  color: var(--cft-primary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  flex-shrink: 0;
  &:disabled {
    color: #a3aec0;
    cursor: not-allowed;
  }
}
.login-method {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--cft-primary);
  margin: 2px 2px 16px;
  span {
    cursor: pointer;
  }
  .forget {
    color: var(--cft-text-muted);
  }
}
.agree {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 12px;
  color: var(--cft-text-secondary);
  margin: 2px 2px 16px;
  line-height: 1.6;
  input {
    margin-top: 3px;
  }
  em {
    font-style: normal;
    color: var(--cft-primary);
  }
}
.submit-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.18s;
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}
.ghost-btn {
  width: 100%;
  height: 46px;
  margin-top: 12px;
  border: 1px solid var(--cft-primary);
  border-radius: 12px;
  background: #fff;
  color: var(--cft-primary);
  font-size: 14px;
  cursor: pointer;
  &:disabled {
    opacity: 0.6;
  }
}
.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 24px 0 16px;
  color: var(--cft-text-muted);
  font-size: 12px;
  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: var(--cft-border);
  }
}
.wechat-btn {
  width: 100%;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  border-radius: 12px;
  background: #f6f8fb;
  color: #16a34a;
  font-size: 14px;
  cursor: pointer;
}
.mode-hint {
  font-size: 13px;
  color: var(--cft-text-muted);
  margin-bottom: 14px;
}
.back-login {
  text-align: center;
  margin: 16px 0 0;
  font-size: 13px;
  color: var(--cft-primary);
  cursor: pointer;
}
.tip {
  margin: 18px 0 0;
  font-size: 12px;
  color: var(--cft-text-muted);
  text-align: center;
  strong {
    color: var(--cft-primary);
  }
}
</style>
