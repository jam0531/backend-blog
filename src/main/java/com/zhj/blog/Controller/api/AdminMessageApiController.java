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
@RequestMapping("/api/admin/messages")
public class AdminMessageApiController {
    private final DiscussionService discussions;
    private final ApiSession sessions;
    private final DiscussionRateLimit limit;
    @GetMapping
    public ApiResponse<PageResult<AdminItem>> list(@Valid @ModelAttribute AdminQuery query) { return ApiResponse.success(discussions.adminMessages(query)); }
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable @Min(1) Long id) { discussions.deleteMessage(id); return ApiResponse.success(null); }
    @PostMapping("/{id}/replies")
    public ApiResponse<Item> reply(@PathVariable @Min(1) Long id, @Valid @RequestBody ReplyInput input, HttpServletRequest request) {
        User admin = sessions.requireAdmin(request);
        return ApiResponse.success(limit.submit(request.getSession(), () -> discussions.replyMessage(id, input, admin)));
    }
}
