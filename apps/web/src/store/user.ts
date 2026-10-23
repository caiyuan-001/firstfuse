import { defineStore } from 'pinia';
import type { User, Tenant, UserRole } from '@chuangrongtong/shared';
import { ref, computed } from 'vue';

/**
 * 用户全局状态
 * - token / 登录态
 * - 当前用户信息
 * - 当前租户信息
 * - 当前角色
 */
export const useUserStore = defineStore(
  'user',
  () => {
    // ========== 状态 ==========
    const token = ref<string>('');
    const userInfo = ref<User | null>(null);
    const tenantInfo = ref<Tenant | null>(null);

    // ========== 计算属性 ==========
    const isLoggedIn = computed(() => !!token.value);
    const userId = computed(() => userInfo.value?.id || '');
    const tenantId = computed(() => userInfo.value?.tenantId || tenantInfo.value?.id || '');
    const role = computed<UserRole | ''>(() => userInfo.value?.role || '');
    const nickname = computed(() => userInfo.value?.nickname || '');
    const avatar = computed(() => userInfo.value?.avatar || '');

    const isEntrepreneur = computed(() => role.value === 'entrepreneur');
    const isInvestor = computed(() => role.value === 'investor');
    const isPlatformAdmin = computed(() => role.value === 'platform_admin');

    // ========== 方法 ==========
    function setToken(t: string) {
      token.value = t;
    }

    function setUserInfo(u: User) {
      userInfo.value = u;
    }

    function setTenantInfo(t: Tenant) {
      tenantInfo.value = t;
    }

    function login(payload: { token: string; user: User; tenant?: Tenant }) {
      token.value = payload.token;
      userInfo.value = payload.user;
      if (payload.tenant) {
        tenantInfo.value = payload.tenant;
      }
    }

    function logout() {
      token.value = '';
      userInfo.value = null;
      tenantInfo.value = null;
    }

    return {
      token,
      userInfo,
      tenantInfo,
      isLoggedIn,
      userId,
      tenantId,
      role,
      nickname,
      avatar,
      isEntrepreneur,
      isInvestor,
      isPlatformAdmin,
      setToken,
      setUserInfo,
      setTenantInfo,
      login,
      logout,
    };
  },
  {
    persist: {
      key: 'cft_user',
      storage: sessionStorage,
      pick: ['token', 'userInfo', 'tenantInfo'],
    },
  },
);
