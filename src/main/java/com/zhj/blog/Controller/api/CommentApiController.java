package com.zhj.blog.Controller.api;

import com.zhj.blog.Dto.*;
import com.zhj.blog.Dto.discussion.DiscussionDto.*;
import com.zhj.blog.Service.DiscussionService;
import com.zhj.blog.Pojo.User;
import com.zhj.blog.interceptor.ApiSession;
import com.zhj.blog.interceptor.DiscussionRateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController @RequiredArgsConstructor @Validated
@RequestMapping("/api/blogs/{blogId}/comments")
public class CommentApiController {
    private final DiscussionService discussions;
    private final ApiSession sessions;
    private final DiscussionRateLimit limit;
    @GetMapping
    public ApiResponse<PageResult<Item>> list(@PathVariable @Min(1) Long blogId, @Valid @ModelAttribute PageQuery query) {
        return ApiResponse.success(discussions.comments(blogId, query));
    }
    @PostMapping
    public ApiResponse<Item> create(@PathVariable @Min(1) Long blogId, @Valid @RequestBody Input input, HttpServletRequest request) {
        User user = sessions.currentUser(request);
        return ApiResponse.success(limit.submit(request.getSession(), () -> discussions.addComment(blogId, input, user)));
    }
}
