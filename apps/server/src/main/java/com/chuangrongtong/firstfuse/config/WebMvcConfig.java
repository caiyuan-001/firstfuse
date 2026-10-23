package com.chuangrongtong.firstfuse.config;

import com.chuangrongtong.firstfuse.domain.oplog.OperationLogInterceptor;
import com.chuangrongtong.firstfuse.security.JwtAuthInterceptor;
import com.chuangrongtong.firstfuse.security.RateLimitInterceptor;
import com.chuangrongtong.firstfuse.security.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器注册 + 白名单。
 * JwtAuthInterceptor → TenantInterceptor 顺序重要（后者依赖前者写入的 TenantContext）
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final TenantInterceptor tenantInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final OperationLogInterceptor operationLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String allPath = "/**";
        String[] publicPaths = {
                "/auth/login",
                "/auth/register",
                "/auth/reset-password",
                "/auth/send-code",
                "/auth/demo-login",
                "/auth/wechat/login",
                "/health",
                "/error",
                "/swagger-ui/**",
                "/v3/api-docs/**"
        };

        // JWT 认证
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns(allPath)
                .excludePathPatterns(publicPaths);

        // 租户校验（比 JWT 更严格，publicPaths 也排除）
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns(allPath)
                .excludePathPatterns(publicPaths);

        // 限流：登录/验证码按 IP、AI 按用户（注册在 JWT 之后，AI 限流可取 userId）
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/auth/login", "/auth/send-code", "/ai/**");

        // 操作日志：写操作 + 文件下载
        registry.addInterceptor(operationLogInterceptor)
                .addPathPatterns(allPath)
                .excludePathPatterns("/health", "/error");
    }
}
