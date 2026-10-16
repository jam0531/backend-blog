package com.zhj.blog.interceptor;

import com.zhj.blog.Pojo.User;
import com.zhj.blog.Service.UserService;
import com.zhj.blog.handler.ApiStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/** 会话只保存 ID；权限每次由数据库确认，不相信客户端角色或旧实体快照。 */
@Component
@RequiredArgsConstructor
public class ApiSession {
    public static final String USER_ID = "api.userId";
    public static final String CSRF = "api.csrf";
    private final UserService userService;
    private final SecureRandom random = new SecureRandom();

    public User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object id = session == null ? null : session.getAttribute(USER_ID);
        return id instanceof Long ? userService.findSessionUser((Long) id) : null;
    }

    public User requireAdmin(HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) throw new ApiStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        if (!Integer.valueOf(1).equals(user.getType())) {
            throw new ApiStatusException(HttpStatus.FORBIDDEN, "没有管理权限");
        }
        return user;
    }

    public String csrf(HttpSession session) {
        synchronized (session) {
            String token = (String) session.getAttribute(CSRF);
            if (token == null) {
                byte[] bytes = new byte[32];
                random.nextBytes(bytes);
                token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                session.setAttribute(CSRF, token);
            }
            return token;
        }
    }
}
