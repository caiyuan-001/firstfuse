package com.chuangrongtong.firstfuse.controller;

import com.chuangrongtong.firstfuse.common.ApiResponse;
import com.chuangrongtong.firstfuse.domain.auth.AuthService;
import com.chuangrongtong.firstfuse.domain.auth.dto.AuthResponse;
import com.chuangrongtong.firstfuse.domain.auth.dto.LoginRequest;
import com.chuangrongtong.firstfuse.domain.auth.dto.RegisterRequest;
import com.chuangrongtong.firstfuse.domain.auth.dto.ResetPasswordRequest;
import com.chuangrongtong.firstfuse.security.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Auth 控制器。
 * 完全对齐前端 mock/modules/user.ts 的端点签名：
 *   POST /api/v1/auth/login            ← role/phone/code/password/register → {token, user}
 *   POST /api/v1/auth/register         ← role/phone/code/inviteCode → {id, registered:true}
 *   POST /api/v1/auth/reset-password   ← phone/code/newPassword → {reset:true}
 *   POST /api/v1/auth/send-code        ← phone → {sent:true}
 *   GET  /api/v1/auth/me               → User
 *   POST /api/v1/auth/refresh          → {token}      （需携带有效 Bearer）
 *   POST /api/v1/auth/wechat/login     → {token}
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录（一键体验 / 密码 / 验证码三种模式，register=true 时走注册） */
    @PostMapping("/login")
    public ApiResponse<Object> login(@Valid @RequestBody LoginRequest req) {
        if (Boolean.TRUE.equals(req.getRegister())) {
            return ApiResponse.success(authService.register(req.getRole(), req.getPhone(), req.getCode(), null));
        }
        AuthResponse resp = authService.login(req.getRole(), req.getPhone(), req.getCode(), req.getPassword());
        return ApiResponse.success(resp);
    }

    /** 显式注册端点，出参对齐 mock：{id, registered:true} */
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.success(authService.register(req.getRole(), req.getPhone(), req.getCode(), req.getNickname()));
    }

    /** 当前登录用户信息（需 Bearer） */
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        return ApiResponse.success(authService.currentUser(TenantContext.getUserId()));
    }

    /** 刷新 token（需 Bearer） */
    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh() {
        return ApiResponse.success(authService.refresh(TenantContext.getUserId()));
    }

    /** 微信授权登录（演示模式） */
    @PostMapping("/wechat/login")
    public ApiResponse<Map<String, Object>> wechatLogin() {
        return ApiResponse.success(authService.wechatLogin());
    }

    /** 重置密码 */
    @PostMapping("/reset-password")
    public ApiResponse<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.getPhone(), req.getCode(), req.getNewPassword());
        return ApiResponse.success(Map.of("reset", true));
    }

    /** 发送验证码 */
    @PostMapping("/send-code")
    public ApiResponse<Map<String, Object>> sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        authService.sendCode(phone);
        return ApiResponse.success(Map.of("sent", true, "demo_code", "123456"));
    }
}
