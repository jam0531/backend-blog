package com.zhj.blog.Controller.api;

import com.zhj.blog.Dto.ApiResponse;
import com.zhj.blog.Dto.auth.LoginRequest;
import com.zhj.blog.Dto.auth.SessionUserDto;
import com.zhj.blog.Pojo.User;
import com.zhj.blog.Service.UserService;
import com.zhj.blog.interceptor.ApiSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {
    private final UserService userService;
    private final ApiSession sessions;

    @ModelAttribute
    public void noCache(HttpServletResponse response) { response.setHeader("Cache-Control", "no-store"); }

    @GetMapping("/csrf")
    public ApiResponse<Map<String, String>> csrf(HttpServletRequest request) {
        return ApiResponse.success(Collections.singletonMap("token", sessions.csrf(request.getSession())));
    }

    @PostMapping("/login")
    public ApiResponse<SessionUserDto> login(@Valid @RequestBody LoginRequest input, HttpServletRequest request) {
        User user = userService.authenticateAdmin(input.getUsername(), input.getPassword());
        request.changeSessionId();
        HttpSession session = request.getSession();
        session.setAttribute(ApiSession.USER_ID, user.getId());
        session.removeAttribute(ApiSession.CSRF);
        return ApiResponse.success(SessionUserDto.from(user));
    }

    @GetMapping("/me")
    public ApiResponse<SessionUserDto> me(HttpServletRequest request) {
        return ApiResponse.success(SessionUserDto.from(sessions.requireAdmin(request)));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ApiResponse.success(null);
    }
}
