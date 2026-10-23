<template>
  <div class="h5-profile">
    <header class="grad-head">
      <div class="u-row">
        <div class="avatar">{{ shortName }}</div>
        <div class="u-info">
          <p class="u-name">{{ nickname }} <span class="badge">创业者</span></p>
          <p class="u-phone">{{ maskedPhone }}</p>
        </div>
        <span class="verify">已实名</span>
      </div>
    </header>

    <div class="menu-group">
      <div class="menu-item" @click="router.push('/h5/project')">
        <el-icon :size="18" class="mi-icon blue"><Files /></el-icon>
        <span>我的项目</span>
        <el-icon class="arr"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="todo('我的投递')">
        <el-icon :size="18" class="mi-icon green"><Promotion /></el-icon>
        <span>我的投递</span>
        <el-icon class="arr"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="todo('我的收藏')">
        <el-icon :size="18" class="mi-icon amber"><Star /></el-icon>
        <span>我的收藏</span>
        <el-icon class="arr"><ArrowRight /></el-icon>
      </div>
    </div>

    <div class="menu-group">
      <div class="menu-item" @click="todo('账号与安全')">
        <el-icon :size="18" class="mi-icon violet"><Lock /></el-icon>
        <span>账号与安全</span>
        <el-icon class="arr"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="todo('关于创融通')">
        <el-icon :size="18" class="mi-icon gray"><InfoFilled /></el-icon>
        <span>关于创融通</span>
        <el-icon class="arr"><ArrowRight /></el-icon>
      </div>
    </div>

    <button class="logout-btn" @click="logout">退出登录</button>
    <p class="version">{{ PLATFORM_INFO.sloganShort }} · v0.1.0 Frontend Demo</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Files, Promotion, Star, Lock, InfoFilled, ArrowRight } from '@element-plus/icons-vue';
import { useUserStore } from '@/store';
import { PLATFORM_INFO } from '@chuangrongtong/shared';

const router = useRouter();
const userStore = useUserStore();

const nickname = computed(() => userStore.nickname || '创业者');
const shortName = computed(() => nickname.value.slice(0, 1));
const maskedPhone = computed(() => {
  const phone = userStore.userInfo?.phone || '';
  return phone.length === 11 ? `${phone.slice(0, 3)}****${phone.slice(7)}` : '未绑定手机号';
});

function todo(name: string) {
  ElMessage.info(`「${name}」页面建设中（演示 Mock）`);
}

async function logout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning',
    });
    userStore.logout();
    ElMessage.success('已退出登录');
    router.replace('/h5/login');
  } catch {
    // 用户取消
  }
}
</script>

<style lang="scss" scoped>
.h5-profile {
  min-height: 100%;
  padding-bottom: 8px;
}
.grad-head {
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
  color: #fff;
  padding: 30px 20px 34px;
  .u-row {
    display: flex;
    align-items: center;
    gap: 14px;
  }
  .avatar {
    width: 54px;
    height: 54px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    font-weight: 600;
    flex-shrink: 0;
  }
  .u-info {
    flex: 1;
    min-width: 0;
    .u-name {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 17px;
      font-weight: 600;
      margin: 0 0 4px;
      .badge {
        font-size: 10px;
        font-weight: 400;
        padding: 2px 8px;
        border-radius: 10px;
        background: rgba(255, 255, 255, 0.2);
      }
    }
    .u-phone {
      font-size: 13px;
      opacity: 0.8;
      margin: 0;
    }
  }
  .verify {
    flex-shrink: 0;
    font-size: 11px;
    padding: 3px 10px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.18);
  }
}
.menu-group {
  margin: 14px 16px 0;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 16px;
  font-size: 14px;
  color: var(--cft-text-primary);
  cursor: pointer;
  &:not(:last-child) {
    border-bottom: 1px solid var(--cft-border);
  }
  &:active {
    background: #f6f8fb;
  }
  .mi-icon {
    &.blue {
      color: #2563eb;
    }
    &.green {
      color: #16a34a;
    }
    &.amber {
      color: #d97706;
    }
    &.violet {
      color: #7c3aed;
    }
    &.gray {
      color: #64748b;
    }
  }
  span {
    flex: 1;
  }
  .arr {
    color: #c0c8d4;
    font-size: 14px;
  }
}
.logout-btn {
  display: block;
  width: calc(100% - 32px);
  margin: 20px 16px 0;
  height: 46px;
  border: 1px solid #fecaca;
  border-radius: 12px;
  background: #fff;
  color: #dc2626;
  font-size: 15px;
  cursor: pointer;
  &:active {
    background: #fef2f2;
  }
}
.version {
  text-align: center;
  font-size: 11px;
  color: var(--cft-text-muted);
  margin: 16px 0 0;
}
</style>
