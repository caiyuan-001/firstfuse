/**
 * Axios 请求封装
 * - 自动注入 Authorization Bearer token
 * - 自动注入 X-Tenant-ID 请求头
 * - 统一错误处理
 * - 统一业务 code 判断
 */
import axios, { type AxiosInstance, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/store/user';
import type { ApiResponse } from '@chuangrongtong/shared';

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000,
});

// ========== 请求拦截器 ==========
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore();

    // 注入 token
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`;
    }

    // 注入租户 ID
    if (userStore.tenantId) {
      config.headers['X-Tenant-ID'] = userStore.tenantId;
    }

    return config;
  },
  (error) => Promise.reject(error),
);

// ========== 响应拦截器 ==========
request.interceptors.response.use(
  (response) => {
    const res = response.data;

    // 如果不是标准 ApiResponse 结构，直接返回
    if (res.code === undefined || res.data === undefined) {
      return res;
    }

    // 业务 code 判断
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败');

      // 401xxx 认证域（未登录/Token 过期/签名无效）→ 登出并跳转登录
      if (res.code >= 401000 && res.code < 402000) {
        const userStore = useUserStore();
        userStore.logout();
        // 限流（401103）不跳登录，仅提示
        if (res.code !== 401103 && window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
      }

      return Promise.reject(new Error(res.message || 'Error'));
    }

    return res as ApiResponse;
  },
  (error) => {
    const message = error?.response?.data?.message || error.message || '网络异常';
    ElMessage.error(message);
    return Promise.reject(error);
  },
);

// ========== 类型安全的请求方法 ==========
export function httpGet<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.get(url, config) as Promise<ApiResponse<T>>;
}

export function httpPost<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.post(url, data, config) as Promise<ApiResponse<T>>;
}

export function httpPut<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.put(url, data, config) as Promise<ApiResponse<T>>;
}

export function httpDelete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.delete(url, config) as Promise<ApiResponse<T>>;
}

export function httpPatch<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.patch(url, data, config) as Promise<ApiResponse<T>>;
}

export default request;
