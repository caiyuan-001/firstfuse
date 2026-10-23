package com.chuangrongtong.firstfuse.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。所有异常统一转 ApiResponse，不向上抛。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        log.warn("[业务异常] code={}, msg={}", ex.getErrorCode().getCode(), ex.getMessage());
        return ApiResponse.error(ex.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[参数校验失败] {}", msg);
        return ApiResponse.error(ErrorCode.PARAM_VALIDATION.getCode(), msg);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBind(BindException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[绑定失败] {}", msg);
        return ApiResponse.error(ErrorCode.PARAM_VALIDATION.getCode(), msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraint(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("[约束违反] {}", msg);
        return ApiResponse.error(ErrorCode.PARAM_VALIDATION.getCode(), msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleNotReadable(HttpMessageNotReadableException ex) {
        return ApiResponse.error(ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleAll(Exception ex) {
        log.error("[未处理异常]", ex);
        return ApiResponse.error(ErrorCode.INTERNAL_ERROR);
    }
}
