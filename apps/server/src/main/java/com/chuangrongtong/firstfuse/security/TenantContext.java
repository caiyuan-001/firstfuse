package com.chuangrongtong.firstfuse.security;

/**
 * 线程局部租户/用户上下文。
 * - JwtAuthInterceptor 认证成功后 set
 * - TenantInterceptor 在此基础上校验 X-Tenant-ID 是否与 token 里的 tid 一致
 * - Controller / Service 直接 TenantContext.getUserId() / getTenantId() / getRole() 取
 */
public final class TenantContext {

    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> tenantIdHolder = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(String userId, String role, String tenantId) {
        userIdHolder.set(userId);
        roleHolder.set(role);
        tenantIdHolder.set(tenantId);
    }

    public static String getUserId() {
        return userIdHolder.get();
    }

    public static String getRole() {
        return roleHolder.get();
    }

    public static String getTenantId() {
        return tenantIdHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
        roleHolder.remove();
        tenantIdHolder.remove();
    }
}
