package com.zhj.blog.handler;

import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.NotFoundException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import javax.validation.ConstraintViolationException;

/** 只处理新 API；返回固定错误文案，不把 SQL、异常堆栈或请求内容暴露给客户端。 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RestControllerAdvice(basePackages = "com.zhj.blog.Controller.api")
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> badRequest(Exception exception) {
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(400, "请求参数不合法"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> invalidConstraints(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(400, ApiValidationMessages.violations(exception.getConstraintViolations())));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(404, "资源不存在"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unexpected(Exception exception) {
        log.error("API request failed", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(500, "服务暂时不可用"));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        // 错误响应固定为 JSON，避免 Accept: text/html 令参数错误再次发生内容协商失败。
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(headers);
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(ApiResponse.error(status.value(), parameterMessage(exception, status)), responseHeaders, status);
    }

    private String parameterMessage(Exception exception, HttpStatus status) {
        if (exception instanceof MethodArgumentNotValidException) {
            return ApiValidationMessages.binding(((MethodArgumentNotValidException) exception).getBindingResult());
        }
        if (exception instanceof BindException) {
            return ApiValidationMessages.binding(((BindException) exception).getBindingResult());
        }
        if (exception instanceof TypeMismatchException) {
            String field = exception instanceof MethodArgumentTypeMismatchException
                    ? ((MethodArgumentTypeMismatchException) exception).getName()
                    : ((TypeMismatchException) exception).getPropertyName();
            return ApiValidationMessages.invalidType(field);
        }
        if (exception instanceof MissingServletRequestParameterException) {
            return "请填写" + ApiValidationMessages.label(((MissingServletRequestParameterException) exception).getParameterName());
        }
        if (exception instanceof HttpMessageNotReadableException) {
            Throwable cause = exception.getCause();
            if (cause instanceof MismatchedInputException) {
                for (com.fasterxml.jackson.databind.JsonMappingException.Reference reference
                        : ((MismatchedInputException) cause).getPath()) {
                    if (reference.getFieldName() != null) return ApiValidationMessages.invalidType(reference.getFieldName());
                }
            }
            return "请求内容格式不正确，请检查后重试";
        }
        return status.is5xxServerError() ? "服务暂时不可用" : "请求不合法";
    }
}
