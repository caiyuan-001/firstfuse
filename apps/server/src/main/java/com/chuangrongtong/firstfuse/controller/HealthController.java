package com.chuangrongtong.firstfuse.controller;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * 健康检查 + 版本信息，前端 dev proxy 探活用。
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @Value("${spring.application.name:firstfuse-server}")
    private String appName;

    @Value("${jwt.access-token-expire-seconds:7200}")
    private long accessTokenExpire;

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", appName,
                "timestamp", Instant.now().toEpochMilli(),
                "jwt_access_expire_sec", accessTokenExpire
        ));
    }
}
