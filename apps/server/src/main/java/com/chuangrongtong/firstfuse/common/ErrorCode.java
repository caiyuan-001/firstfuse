package com.chuangrongtong.firstfuse.common;

import lombok.Getter;

/**
 * 统一错误码，与前端 shared types 的 HTTP 状态语义 + 业务码一一对应。
 * 错误码结构：6 位数字，前 3 位对齐 HTTP 语义（400/401/403/404/500），
 * 后 3 位为业务细分（000=通用，001=参数校验，1xx=认证域，2xx=租户域...）
 */
@Getter
public enum ErrorCode {

    // === 通用 4xx ===
    BAD_REQUEST(400_000, "请求参数错误"),
    PARAM_VALIDATION(400_001, "参数校验失败"),
    UNAUTHORIZED(401_000, "未登录或 Token 已过期"),
    TENANT_CONTEXT_MISSING(401_001, "缺少租户上下文（X-Tenant-ID）"),
    FORBIDDEN(403_000, "无权访问该资源"),
    ROLE_DENIED(403_001, "当前角色无权限执行该操作"),
    NOT_FOUND(404_000, "资源不存在"),

    // === 认证域 401_1xx ===
    LOGIN_FAILED(401_100, "手机号或密码错误"),
    CODE_INVALID(401_101, "验证码无效"),
    CODE_EXPIRED(401_102, "验证码已过期"),
    RATE_LIMIT(401_103, "操作过于频繁，请稍后再试"),
    REGISTER_DUPLICATE(401_104, "该手机号已注册"),
    TOKEN_INVALID(401_105, "Token 签名无效"),
    TOKEN_EXPIRED(401_106, "Token 已过期，请重新登录"),

    // === 租户域 403_2xx ===
    TENANT_FROZEN(403_200, "租户已被冻结"),
    TENANT_NOT_FOUND(403_201, "租户不存在"),
    TENANT_EXPIRED(403_202, "租户订阅已过期"),

    // === 项目域 4xx_3xx ===
    PROJECT_NOT_FOUND(404_300, "项目不存在"),
    PROJECT_STATUS_INVALID(400_301, "项目当前状态不允许该操作"),

    // === 服务端 5xx ===
    INTERNAL_ERROR(500_000, "服务器内部错误"),
    DB_ERROR(500_001, "数据库操作异常"),
    EXTERNAL_API_FAIL(500_002, "外部服务调用失败");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
