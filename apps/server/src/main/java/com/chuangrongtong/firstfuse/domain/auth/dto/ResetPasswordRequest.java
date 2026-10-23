package com.chuangrongtong.firstfuse.domain.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 找回密码请求。
 */
@Data
public class ResetPasswordRequest {

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(min = 6, max = 6)
    private String code;

    @Size(min = 6, max = 32)
    private String newPassword;
}
