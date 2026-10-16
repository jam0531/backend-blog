package com.zhj.blog.interceptor;

import com.zhj.blog.handler.ApiStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ApiCsrfInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) return true;
        HttpSession session = request.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute(ApiSession.CSRF);
        String actual = request.getHeader("X-CSRF-Token");
        if (expected == null || actual == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII), actual.getBytes(StandardCharsets.US_ASCII))) {
            throw new ApiStatusException(HttpStatus.FORBIDDEN, "会话校验失败，请刷新页面后重试");
        }
        return true;
    }
}
