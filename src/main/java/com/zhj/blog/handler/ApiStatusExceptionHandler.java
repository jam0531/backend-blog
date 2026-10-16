package com.zhj.blog.handler;

import com.zhj.blog.Dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** 同时覆盖拦截器及尚无 Controller 的管理路径，只处理 API 专用业务异常。 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiStatusExceptionHandler implements HandlerExceptionResolver {
    private final ObjectMapper objectMapper;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception exception) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!(exception instanceof ApiStatusException) || !(path.equals("/api") || path.startsWith("/api/"))) {
            return null;
        }
        // 资源 Handler 没有 Controller 方法，使用独立解析器保证拦截器异常仍返回 JSON。
        ApiStatusException statusException = (ApiStatusException) exception;
        try {
            response.setStatus(statusException.getStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Cache-Control", "no-store");
            objectMapper.writeValue(response.getWriter(), ApiResponse.error(
                    statusException.getStatus().value(), statusException.getMessage()));
            return new ModelAndView();
        } catch (IOException writeFailure) {
            log.warn("Unable to write API error response", writeFailure);
            return null;
        }
    }
}
