package com.zhj.blog.Controller.api;

import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.navigation.NavigationDto;
import com.zhj.blog.Dto.navigation.NavigationPage;
import com.zhj.blog.Service.NavigationService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/navigation")
@RequiredArgsConstructor
@Validated
public class AdminNavigationApiController {
    private final NavigationService navigation;

    @GetMapping
    public ApiResponse<List<NavigationDto.AdminItem>> list() {
        return ApiResponse.success(navigation.list(false).stream()
                .map(NavigationDto::toAdmin).collect(Collectors.toList()));
    }

    @GetMapping("/pages")
    public ApiResponse<List<Map<String, String>>> pages() {
        return ApiResponse.success(Arrays.stream(NavigationPage.values()).map(page -> {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("pageKey", page.getPageKey());
            item.put("pageName", page.getPageName());
            item.put("path", page.getPath());
            return item;
        }).collect(Collectors.toList()));
    }

    @PostMapping
    public ApiResponse<NavigationDto.AdminItem> create(@Valid @RequestBody NavigationDto.Input input) {
        return ApiResponse.success(NavigationDto.toAdmin(navigation.save(null, input)));
    }

    @PutMapping("/{id}")
    public ApiResponse<NavigationDto.AdminItem> update(@PathVariable @Min(1) Long id,
            @Valid @RequestBody NavigationDto.Input input) {
        return ApiResponse.success(NavigationDto.toAdmin(navigation.save(id, input)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable @Min(1) Long id) {
        navigation.delete(id);
        return ApiResponse.success(null);
    }
}
