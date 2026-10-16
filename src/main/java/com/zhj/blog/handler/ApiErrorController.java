package com.zhj.blog.handler;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** 接住尚未匹配 Controller 的 API 错误；旧页面继续使用 Spring Boot 原有错误处理。 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ApiErrorController extends BasicErrorController {
    public ApiErrorController(ErrorAttributes attributes, ServerProperties properties,
            ObjectProvider<ErrorViewResolver> resolvers) {
        super(attributes, properties.getError(), resolvers.orderedStream().collect(Collectors.toList()));
    }

    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        if (!isApiRequest(request)) {
            return super.errorHtml(request, response);
        }
        HttpStatus status = getStatus(request);
        response.setStatus(status.value());
        return new ModelAndView(new MappingJackson2JsonView(), errorBody(status));
    }

    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        if (!isApiRequest(request)) {
            return super.error(request);
        }
        HttpStatus status = getStatus(request);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(errorBody(status));
    }

    private boolean isApiRequest(HttpServletRequest request) {
        Object originalUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (!(originalUri instanceof String)) {
            return false;
        }
        String prefix = request.getContextPath() + "/api";
        return originalUri.equals(prefix) || ((String) originalUri).startsWith(prefix + "/");
    }

    private Map<String, Object> errorBody(HttpStatus status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", status.value());
        body.put("message", status == HttpStatus.NOT_FOUND ? "资源不存在"
                : status.is5xxServerError() ? "服务暂时不可用" : "请求不合法");
        body.put("data", null);
        return body;
    }
}
