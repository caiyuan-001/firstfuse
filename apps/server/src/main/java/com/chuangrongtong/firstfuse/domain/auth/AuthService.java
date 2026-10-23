package com.chuangrongtong.firstfuse.domain.auth;

import com.chuangrongtong.firstfuse.common.BusinessException;
import com.chuangrongtong.firstfuse.common.ErrorCode;
import com.chuangrongtong.firstfuse.domain.auth.dto.AuthResponse;
import com.chuangrongtong.firstfuse.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 认证服务。
 *
 * 第一阶段（dev=true）：内存演示模式，不查 PostgreSQL。
 *   - 验证码固定 "123456"
 *   - 密码不校验（或固定 "123456"）
 *   - 按 role 找演示用户直接发 JWT
 * 第二阶段（dev=false）：替换成真实 DB 查询 + BCrypt + Redis 验证码。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;

    @Value("${demo.enabled:true}")
    private boolean demoMode;

    @Value("${jwt.access-token-expire-seconds:7200}")
    private long accessTokenExpireSeconds;

    /** 演示用户：按 role 索引 */
    private static final Map<String, DemoUser> DEMO_USERS = Map.of(
            "platform_admin", new DemoUser("u-001", "t-platform", "platform_admin", "平台管理员", "13800000000"),
            "investor",       new DemoUser("u-002", "t-001",      "investor",       "李投资",     "13800138001"),
            "entrepreneur",   new DemoUser("u-004", "t-001",      "entrepreneur",   "张创业",     "13800138003")
    );

    /** 演示验证码 */
    private static final String DEMO_CODE = "123456";

    /**
     * 登录。支持三种模式：
     *   1) role 非空 phone/code/password 全空 → 一键体验（H5 "创业者一键体验" / PC tab 登录）
     *   2) phone + password → 密码登录
     *   3) phone + code → 验证码登录
     */
    public AuthResponse login(String role, String phone, String code, String password) {
        // 一键体验
        if (role != null && phone == null && code == null && password == null) {
            return quickLogin(role);
        }

        // 密码登录
        if (password != null && !password.isBlank()) {
            return loginByPassword(phone, password);
        }

        // 验证码登录
        if (code != null && !code.isBlank()) {
            return loginByCode(phone, code);
        }

        throw new BusinessException(ErrorCode.LOGIN_FAILED, "请提供密码或验证码");
    }

    /**
     * 注册。出参对齐前端 mock：{ id, registered: true }
     */
    public Map<String, Object> register(String role, String phone, String code, String nickname) {
        // 验证码校验（仅非空）
        if (!demoMode && !DEMO_CODE.equals(code)) {
            throw new BusinessException(ErrorCode.CODE_INVALID);
        }
        // 如果手机号已注册，返回已有的演示用户 id
        if (phone != null) {
            for (DemoUser u : DEMO_USERS.values()) {
                if (phone.equals(u.phone)) {
                    return Map.of("id", u.id(), "registered", true);
                }
            }
        }
        String newId = "u-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("[注册] demo mode: role={}, phone={}, newId={}", role, phone, newId);
        return Map.of("id", newId, "registered", true);
    }

    /**
     * 重置密码。演示模式只要 code=123456 就返回成功（不查 DB）。
     */
    public boolean resetPassword(String phone, String code, String newPassword) {
        if (!demoMode && !DEMO_CODE.equals(code)) {
            throw new BusinessException(ErrorCode.CODE_INVALID);
        }
        log.info("[重置密码] demo mode: phone={}", phone);
        return true;
    }

    public boolean sendCode(String phone) {
        if (demoMode) {
            log.info("[发送验证码] demo mode: phone={}, code={}", phone, DEMO_CODE);
            return true;
        }
        // TODO: Redis 存 code，5min TTL，同手机号限频
        return true;
    }

    /** GET /auth/me — 出参对齐 mock User 结构 */
    public Map<String, Object> currentUser(String userId) {
        DemoUser u = findById(userId);
        if (u == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.id());
        m.put("tenantId", u.tenantId());
        m.put("role", u.role());
        m.put("nickname", u.nickname());
        m.put("avatar", "");
        m.put("phone", u.phone());
        m.put("email", null);
        m.put("company", null);
        m.put("position", null);
        m.put("realNameVerified", true);
        m.put("createdAt", Instant.now());
        m.put("updatedAt", Instant.now());
        return m;
    }

    /** POST /auth/refresh — 用当前有效 token 换新 access token */
    public Map<String, Object> refresh(String userId) {
        DemoUser u = findById(userId);
        if (u == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        String token = jwtService.issueAccessToken(u.id(), u.role(), u.tenantId());
        return Map.of("token", token);
    }

    /** POST /auth/wechat/login — 演示模式返回占位 token（真实对接在 Follow-ups） */
    public Map<String, Object> wechatLogin() {
        return Map.of("token", "demo-wechat-token-" + System.currentTimeMillis());
    }

    // ========== 内部方法 ==========

    private AuthResponse quickLogin(String role) {
        DemoUser u = DEMO_USERS.get(role);
        if (u == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "角色不存在: " + role);
        }
        return buildResponse(u);
    }

    private AuthResponse loginByPassword(String phone, String password) {
        DemoUser u = findByPhone(phone);
        if (u == null) {
            // 演示模式允许任何手机号登录（如果找不到，动态创建一个 entrepreneur）
            u = new DemoUser(
                    "u-" + UUID.randomUUID().toString().substring(0, 8),
                    "t-" + UUID.randomUUID().toString().substring(0, 4),
                    "entrepreneur", "创业者", phone
            );
        }
        return buildResponse(u);
    }

    private AuthResponse loginByCode(String phone, String code) {
        if (!demoMode && !DEMO_CODE.equals(code)) {
            throw new BusinessException(ErrorCode.CODE_INVALID);
        }
        return loginByPassword(phone, "any");
    }

    private DemoUser findByPhone(String phone) {
        for (DemoUser u : DEMO_USERS.values()) {
            if (phone.equals(u.phone)) return u;
        }
        return null;
    }

    private DemoUser findById(String userId) {
        for (DemoUser u : DEMO_USERS.values()) {
            if (u.id().equals(userId)) return u;
        }
        return null;
    }

    private AuthResponse buildResponse(DemoUser u) {
        String token = jwtService.issueAccessToken(u.id, u.role, u.tenantId);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpireSeconds)
                .user(AuthResponse.UserInfo.builder()
                        .id(u.id)
                        .tenantId(u.tenantId)
                        .role(u.role)
                        .nickname(u.nickname)
                        .phone(u.phone)
                        .realNameVerified(false)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build())
                .build();
    }

    /** 内部演示用户结构 */
    private record DemoUser(String id, String tenantId, String role, String nickname, String phone) {}
}
