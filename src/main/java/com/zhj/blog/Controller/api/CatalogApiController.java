package com.zhj.blog.Controller.api;

import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.blog.TagCountDto;
import com.zhj.blog.Dto.home.HomeDto;
import com.zhj.blog.Service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CatalogApiController {
    private final BlogService blogService;

    @GetMapping("/api/types")
    public ApiResponse<List<HomeDto.TypeCountDto>> types() {
        return ApiResponse.success(blogService.getPublicTypes());
    }

    @GetMapping("/api/tags")
    public ApiResponse<List<TagCountDto>> tags() {
        return ApiResponse.success(blogService.getPublicTags());
    }

    @GetMapping("/api/archives")
    public ApiResponse<List<HomeDto.ArchiveDto>> archives() {
        return ApiResponse.success(blogService.getPublicArchives());
    }
}
