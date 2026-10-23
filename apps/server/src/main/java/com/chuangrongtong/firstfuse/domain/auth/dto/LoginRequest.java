package com.chuangrongtong.firstfuse.domain.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求，完全对齐前端 mock/modules/user.ts 的入参结构。
 */
@Data
public class LoginRequest {

    /** 角色：entrepreneur / investor / platform_admin — 演示模式快速登录时必填 */
    private String role;

    /** 手机号（密码或验证码登录都需要） */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 验证码（6 位数字） */
    @Size(min = 6, max = 6, message = "验证码为 6 位")
    private String code;

    /** 密码 */
    @Size(min = 6, max = 32, message = "密码长度 6-32 位")
    private String password;

    /** 前端注册 tab 复用 login 端点时的标记 */
    private Boolean register;
}
