package com.chuangrongtong.firstfuse.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 登录/注册/重置密码统一响应，对齐前端 LoginResult type。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;

    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String tenantId;
        private String role;
        private String nickname;
        private String avatar;
        private String phone;
        private String company;
        private String position;
        private Boolean realNameVerified;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
