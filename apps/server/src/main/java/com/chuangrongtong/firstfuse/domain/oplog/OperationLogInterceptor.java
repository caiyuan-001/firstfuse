package com.chuangrongtong.firstfuse.domain.oplog;

import com.chuangrongtong.firstfuse.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * 操作日志拦截器：记录所有写操作（POST/PUT/PATCH/DELETE）与文件下载。
 * 排除 /admin/logs（查询自身）、/health；请求体不记录（避免密码泄露），仅记录 query string。
 */
@Component
@RequiredArgsConstructor
public class OperationLogInterceptor implements HandlerInterceptor {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final String ATTR_START = "oplog.start";

    private final OperationLogService operationLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATTR_START, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        boolean download = "GET".equals(method) && path.matches(".*/files/[^/]+$");
        if (!WRITE_METHODS.contains(method) && !download) return;
        if (path.endsWith("/admin/logs") || path.endsWith("/health")) return;

        Long start = (Long) request.getAttribute(ATTR_START);
        long cost = start == null ? -1 : System.currentTimeMillis() - start;
        operationLogService.record(
                TenantContext.getUserId(),
                TenantContext.getRole(),
                TenantContext.getTenantId(),
                method,
                path,
                response.getStatus(),
                cost,
                clientIp(request),
                request.getQueryString());
    }

    static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
