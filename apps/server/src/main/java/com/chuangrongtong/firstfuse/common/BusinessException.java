package com.chuangrongtong.firstfuse.common;

import lombok.Getter;

/**
 * 业务异常，携带 ErrorCode + 可选自定义 message。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
        // 允许临时构造自定义 code（较少用）
        // 这里直接用 INTERNAL_ERROR 但 message 可覆盖
    }
}
