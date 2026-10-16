package com.zhj.blog.Controller.api;

import com.zhj.blog.Dto.*;
import com.zhj.blog.Dto.discussion.DiscussionDto.*;
import com.zhj.blog.Service.DiscussionService;
import com.zhj.blog.Pojo.User;
import com.zhj.blog.interceptor.ApiSession;
import com.zhj.blog.interceptor.DiscussionRateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageApiController {
    private final DiscussionService discussions;
    private final ApiSession sessions;
    private final DiscussionRateLimit limit;
    @GetMapping
    public ApiResponse<PageResult<Item>> list(@Valid @ModelAttribute MessageQuery query) { return ApiResponse.success(discussions.messages(query)); }
    @PostMapping
    public ApiResponse<Item> create(@Valid @RequestBody MessageInput input, HttpServletRequest request) {
        User user = sessions.currentUser(request);
        return ApiResponse.success(limit.submit(request.getSession(), () -> discussions.addMessage(input, user)));
    }
}
