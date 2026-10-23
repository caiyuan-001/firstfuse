package com.chuangrongtong.firstfuse.security;

import com.chuangrongtong.firstfuse.common.ErrorCode;
import com.chuangrongtong.firstfuse.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 多租户校验拦截器。
 * - 要求所有业务请求携带 X-Tenant-ID 头
 * - 校验其与 JWT 里的 tid 一致（platform_admin 角色例外——可跨租户操作）
 * - 仅在 JwtAuthInterceptor 之后执行，因此此时 TenantContext 已有值
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        String headerTenantId = req.getHeader("X-Tenant-ID");
        if (headerTenantId == null || headerTenantId.isBlank()) {
            throw new BusinessException(ErrorCode.TENANT_CONTEXT_MISSING);
        }

        String userTenantId = TenantContext.getTenantId();
        String userRole = TenantContext.getRole();

        // platform_admin 可跨租户操作
        if ("platform_admin".equals(userRole)) {
            return true;
        }

        if (!headerTenantId.equals(userTenantId)) {
            log.warn("[租户越权] header={}, token={}, role={}", headerTenantId, userTenantId, userRole);
            throw new BusinessException(ErrorCode.ROLE_DENIED,
                    "请求头 X-Tenant-ID 与当前登录用户所属租户不一致");
        }

        return true;
    }
}
