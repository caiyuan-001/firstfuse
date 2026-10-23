<template>
  <div class="entrepreneur-preview">
    <!-- 顶部预览模式切换 -->
    <div class="preview-header">
      <div class="brand">
        <div class="logo-small">CR</div>
        <div class="brand-text">
          <span class="title">创融通 · 创业者端（PC 预览视图）</span>
          <span class="slogan">{{ PLATFORM_INFO.sloganShort }}</span>
        </div>
      </div>
      <div class="actions">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="pc">PC 预览</el-radio-button>
          <el-radio-button label="mobile">手机预览</el-radio-button>
        </el-radio-group>
        <el-button size="small" @click="router.push('/preview')">
          <el-icon><Close /></el-icon>退出预览
        </el-button>
      </div>
    </div>

    <div class="preview-body">
      <!-- PC 预览模式：渲染创业者端 PC 页面 -->
      <div v-if="viewMode === 'pc'" class="pc-mode">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>

      <!--
        手机预览模式：直接内嵌真实的 H5 应用（/h5），而不是把 PC 版式塞进 375px 屏幕。
        此前这里是复用 PC 页面的，桌面版式在 375px 下被压成极窄列、文字逐字换行并出现
        横向溢出，因此改为由 iframe 承载完整 H5 应用（含底部 Tab 栏与应用内跳转）。
      -->
      <div v-else class="mobile-mode">
        <div class="phone-frame">
          <div class="phone-notch"></div>
          <div class="phone-screen">
            <iframe class="phone-iframe" src="/h5/home" title="创业者端手机预览" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Close } from '@element-plus/icons-vue';
import { PLATFORM_INFO } from '@chuangrongtong/shared';

const router = useRouter();

// 真机访问时默认进手机预览，避免桌面版式被压坏
const viewMode = ref<'pc' | 'mobile'>(window.innerWidth <= 768 ? 'mobile' : 'pc');
</script>

<style lang="scss" scoped>
.entrepreneur-preview {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8edf5 100%);

  .preview-header {
    flex-shrink: 0;
    height: 56px;
    background: white;
    border-bottom: 1px solid var(--cft-border);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    position: sticky;
    top: 0;
    z-index: 100;
    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
      min-width: 0;
    }
    .logo-small {
      width: 32px;
      height: 32px;
      border-radius: 6px;
      background: linear-gradient(135deg, #3b82f6, #2563eb);
      color: white;
      font-weight: bold;
      font-size: 13px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .brand-text {
      display: flex;
      flex-direction: column;
      line-height: 1.25;
      min-width: 0;
    }
    .slogan {
      font-size: 10px;
      color: var(--cft-text-muted);
      white-space: nowrap;
    }
    .title {
      font-size: 15px;
      font-weight: 600;
      color: var(--cft-text-primary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .actions {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-shrink: 0;
    }
  }

  .preview-body {
    flex: 1;
    padding: 24px;
  }

  .pc-mode {
    max-width: 1200px;
    margin: 0 auto;
  }

  .mobile-mode {
    display: flex;
    justify-content: center;

    .phone-frame {
      width: 375px;
      height: 760px;
      background: #1f2937;
      border-radius: 40px;
      padding: 12px;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
      position: relative;
    }
    // 顶部 44px 作为状态栏区域，避免 H5 页面标题被刘海遮挡
    .phone-notch {
      position: absolute;
      top: 18px;
      left: 50%;
      transform: translateX(-50%);
      width: 140px;
      height: 26px;
      background: #111827;
      border-radius: 20px;
      z-index: 2;
    }
    .phone-screen {
      width: 100%;
      height: 100%;
      padding-top: 44px;
      background: var(--cft-bg);
      border-radius: 28px;
      overflow: hidden;
    }
    .phone-iframe {
      display: block;
      width: 100%;
      height: 100%;
      border: 0;
      background: var(--cft-bg);
    }
  }

  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 0.2s;
  }
  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }
}

// PC 窄屏（1024px 以下但 ≥768px，Element Plus viewMode='pc' 仍生效）
@media (max-width: 1024px) and (min-width: 768px) {
  .entrepreneur-preview {
    .preview-header { padding: 0 16px; }
    .preview-body { padding: 16px; }
    .pc-mode { max-width: 100%; padding: 0 4px; }
  }
}

// 窄屏（真机 / 小窗口）：手机预览直接占满可视区，PC 预览收窄外边距
@media (max-width: 768px) {
  .entrepreneur-preview {
    .preview-header {
      padding: 0 12px;
      .title,
      .slogan {
        display: none;
      }
    }
    .preview-body {
      padding: 0;
    }
    .pc-mode {
      padding: 12px;
    }
    .mobile-mode {
      .phone-frame {
        width: 100%;
        height: 100vh;
        border-radius: 0;
        padding: 0;
        box-shadow: none;
      }
      .phone-notch {
        display: none;
      }
      .phone-screen {
        padding-top: 0;
        border-radius: 0;
      }
    }
  }
}
</style>
