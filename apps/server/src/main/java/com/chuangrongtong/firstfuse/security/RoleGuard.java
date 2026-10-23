package com.chuangrongtong.firstfuse.security;

import com.chuangrongtong.firstfuse.common.BusinessException;
import com.chuangrongtong.firstfuse.common.ErrorCode;

/**
 * 角色/租户访问守卫。
 * 依赖 JwtAuthInterceptor + TenantInterceptor 已写入的 TenantContext。
 */
public final class RoleGuard {

    private RoleGuard() {}

    public static final String PLATFORM_ADMIN = "platform_admin";

    public static boolean isPlatformAdmin() {
        return PLATFORM_ADMIN.equals(TenantContext.getRole());
    }

    /** 仅平台管理员可执行（管理后台接口） */
    public static void requirePlatformAdmin() {
        if (!isPlatformAdmin()) {
            throw new BusinessException(ErrorCode.ROLE_DENIED, "仅平台管理员可执行该操作");
        }
    }

    /** 校验可访问目标租户资源：平台管理员放行，否则必须同租户 */
    public static void requireSameTenant(String resourceTenantId) {
        if (isPlatformAdmin()) return;
        if (resourceTenantId == null || !resourceTenantId.equals(TenantContext.getTenantId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问其他租户的数据");
        }
    }
}
