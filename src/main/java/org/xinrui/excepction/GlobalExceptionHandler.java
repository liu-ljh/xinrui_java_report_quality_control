package org.xinrui.excepction;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.xinrui.dto.ApiResponse;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理（医疗质控场景专用）
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 业务参数异常（前端传入数据不合法）
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
                ApiResponse.fail(ApiResponse.PARAM_ERROR, e.getMessage())
        );
    }

    // 2. 数据校验异常（@Size等JSR-303校验失败）
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMsg = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(
                ApiResponse.fail(ApiResponse.PARAM_ERROR, errorMsg)
        );
    }

    // 3. 模型服务异常（DeepSeek API调用失败）
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Void>> handleIOException(IOException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                ApiResponse.fail(ApiResponse.ERROR, "模型服务不可用: " + e.getMessage())
        );
    }

    // 4. 未知系统异常（预防性兜底）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // 记录详细错误（生产环境需脱敏日志）
        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.fail(ApiResponse.ERROR, "系统内部错误: " + e.getMessage())
        );
    }

    // 5. 自定义业务异常（示例：报告内容无效）
    @ExceptionHandler(QualityException.class)
    public ResponseEntity<ApiResponse<Void>> handleQualityException(QualityException e) {
        return ResponseEntity.badRequest().body(
                ApiResponse.fail(e.getCode(), e.getMessage())
        );
    }

    // 6. 业务异常辅助类（医疗质控专用）
    public static class QualityException extends RuntimeException {
        private final int code;

        public QualityException(int code, String message) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}