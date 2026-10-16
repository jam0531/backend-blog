package com.zhj.blog.interceptor;

import com.zhj.blog.handler.ApiStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpSession;
import java.util.function.Supplier;

/** 评论与留言共用会话限频；失败不占用间隔，事务提交成功后才记录时间。 */
@Component
public class DiscussionRateLimit {
    private static final String LAST_SUBMISSION = "api.discussion.lastSubmission";
    public <T> T submit(HttpSession session, Supplier<T> action) {
        synchronized (session) {
            Long previous = (Long) session.getAttribute(LAST_SUBMISSION);
            if (previous != null && System.currentTimeMillis() - previous < 15000L) {
                throw new ApiStatusException(HttpStatus.TOO_MANY_REQUESTS, "提交太快，请间隔 15 秒后再试");
            }
            T result = action.get();
            session.setAttribute(LAST_SUBMISSION, System.currentTimeMillis());
            return result;
        }
    }
}
