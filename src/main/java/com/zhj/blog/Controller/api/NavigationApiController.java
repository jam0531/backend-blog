package com.zhj.blog.Controller.api;

import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.navigation.NavigationDto;
import com.zhj.blog.Service.NavigationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class NavigationApiController {
    private final NavigationService navigation;

    @GetMapping("/api/navigation")
    public ApiResponse<List<NavigationDto.PublicItem>> list(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return ApiResponse.success(navigation.list(true).stream()
                .map(NavigationDto::toPublic).collect(Collectors.toList()));
    }
}
