package com.zhj.blog.Controller.api;

import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.blog.BlogSummaryDto;
import com.zhj.blog.Dto.blog.BlogDetailDto;
import com.zhj.blog.Dto.blog.BlogQuery;
import javax.validation.Valid;
import com.zhj.blog.Service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
public class BlogApiController {
    private final BlogService blogService;

    @GetMapping("/api/blogs/{id}")
    public ApiResponse<BlogDetailDto> detail(@PathVariable Long id) {
        return ApiResponse.success(blogService.getPublicBlogDetail(id));
    }

    @GetMapping("/api/blogs")
    public ApiResponse<PageResult<BlogSummaryDto>> blogs(@Valid @ModelAttribute BlogQuery query) {
        return ApiResponse.success(blogService.getPublicBlogPage(query));
    }
}
