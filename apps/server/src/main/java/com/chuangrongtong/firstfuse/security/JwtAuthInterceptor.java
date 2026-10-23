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
 * JWT 认证拦截器。
 * - 从 Authorization: Bearer xxx 头提取 token
 * - 解析后将 userId/role/tenantId 写入 TenantContext（线程局部）
 * - 放行白名单（/health/**, /auth/**）在 WebMvcConfig 里配置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "缺少 Authorization Bearer Token");
        }

        String token = auth.substring(7);
        try {
            var claims = jwtService.parse(token);
            String userId = claims.get("uid", String.class);
            String role = claims.get("role", String.class);
            String tenantId = claims.get("tid", String.class);

            TenantContext.set(userId, role, tenantId);
            return true;
        } catch (Exception e) {
            log.warn("[JWT 无效] {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
