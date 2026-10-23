package com.chuangrongtong.firstfuse.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一 API 响应格式，对齐前端 shared types 的 ApiResponse<T>
 *
 * @param <T> data 泛型
 */
@Data
public class ApiResponse<T> implements Serializable {

    /** 业务状态码：200=成功，其他为错误码 */
    private Integer code;

    /** 状态描述 */
    private String message;

    /** 业务数据 */
    private T data;

    /** 服务端响应时间戳（毫秒） */
    private Long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }
}
