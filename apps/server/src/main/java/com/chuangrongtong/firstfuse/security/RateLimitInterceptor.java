package com.chuangrongtong.firstfuse.security;

import com.chuangrongtong.firstfuse.common.BusinessException;
import com.chuangrongtong.firstfuse.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * 接口限流（Redis 固定窗口，每分钟）：
 * - /auth/login、/auth/send-code：按客户端 IP 限流
 * - /ai/**：按用户 ID（未登录按 IP）限流
 * 触发后抛 RATE_LIMIT(401103)。Redis 异常时降级放行，不阻塞主流程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redis;

    @Value("${rate-limit.login-per-minute:60}")
    private int loginPerMinute;

    @Value("${rate-limit.ai-per-minute:30}")
    private int aiPerMinute;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (path.contains("/auth/login") || path.contains("/auth/send-code")) {
            check("rl:login:" + clientIp(request), loginPerMinute);
        } else if (path.contains("/ai/")) {
            String uid = TenantContext.getUserId();
            check("rl:ai:" + (uid == null ? clientIp(request) : uid), aiPerMinute);
        }
        return true;
    }

    private void check(String key, int limit) {
        try {
            Long count = redis.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redis.expire(key, Duration.ofMinutes(1));
            }
            if (count != null && count > limit) {
                throw new BusinessException(ErrorCode.RATE_LIMIT);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[RateLimit] Redis 异常，降级放行：{}", e.getMessage());
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
