package com.zhj.blog.Controller.api;

import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.home.HomeDto;
import com.zhj.blog.Service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HomeApiController {
    private final BlogService blogService;

    @GetMapping("/api/home")
    public ApiResponse<HomeDto> home() {
        return ApiResponse.success(blogService.getPublicHome());
    }
}
