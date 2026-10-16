package com.zhj.blog.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.RequiredArgsConstructor;

/**
 * @author:zouhuajun
 * @date:2021/12/14 12:52
 **/
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final ApiLoginInterceptor apiLoginInterceptor;
    private final ApiCsrfInterceptor apiCsrfInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiLoginInterceptor).addPathPatterns("/api/admin", "/api/admin/**").order(0);
        registry.addInterceptor(apiCsrfInterceptor).addPathPatterns("/api/**").order(1);
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/admin/*")
                .excludePathPatterns("/admin")
                .excludePathPatterns("/admin/login");
    }
}
