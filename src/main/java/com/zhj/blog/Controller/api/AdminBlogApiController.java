package com.zhj.blog.Controller.api;

import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.blog.AdminBlogDto;
import com.zhj.blog.Service.BlogService;
import com.zhj.blog.Utils.MarkdownUtils;
import com.zhj.blog.interceptor.ApiSession;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/blogs")
@RequiredArgsConstructor
@Validated
public class AdminBlogApiController {
    private final BlogService blogs;
    private final ApiSession sessions;

    @GetMapping
    public ApiResponse<PageResult<AdminBlogDto.Item>> list(@Valid @ModelAttribute AdminBlogDto.Query query) {
        return ApiResponse.success(blogs.getAdminBlogPage(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminBlogDto.Edit> detail(@PathVariable @Min(1) Long id) {
        return ApiResponse.success(blogs.getAdminBlogEdit(id));
    }

    @PostMapping
    public ApiResponse<AdminBlogDto.Saved> create(@Valid @RequestBody AdminBlogDto.Input input, HttpServletRequest request) {
        return ApiResponse.success(blogs.saveAdminBlog(null, input, sessions.requireAdmin(request).getId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminBlogDto.Saved> update(@PathVariable @Min(1) Long id, @Valid @RequestBody AdminBlogDto.Input input) {
        return ApiResponse.success(blogs.saveAdminBlog(id, input, null));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable @Min(1) Long id) {
        blogs.deleteAdminBlog(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/preview")
    public ApiResponse<Map<String, String>> preview(@Valid @RequestBody AdminBlogDto.PreviewInput input) {
        return ApiResponse.success(Collections.singletonMap("contentHtml", MarkdownUtils.renderPublicContent(input.getContentMarkdown())));
    }
}
