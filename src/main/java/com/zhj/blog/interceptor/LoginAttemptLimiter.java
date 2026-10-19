package com.zhj.blog.interceptor;

import com.zhj.blog.handler.ApiStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** 单实例登录限频；不保存密码、不写数据库，重启后计数清空。 */
@Component
public class LoginAttemptLimiter {
    private static final int MAX_KEYS = 10000;
    private final Map<String, Window> windows = new HashMap<>();
    private final long windowMillis;
    private final int accountLimit;
    private final int ipLimit;

    public LoginAttemptLimiter(
            @Value("${blog.login-protection.window-seconds:900}") int windowSeconds,
            @Value("${blog.login-protection.account-ip-failures:5}") int accountLimit,
            @Value("${blog.login-protection.ip-failures:30}") int ipLimit) {
        Assert.isTrue(windowSeconds > 0 && accountLimit > 0 && ipLimit > 0, "Login protection limits must be positive");
        this.windowMillis = TimeUnit.SECONDS.toMillis(windowSeconds);
        this.accountLimit = accountLimit;
        this.ipLimit = ipLimit;
    }

    public <T> T authenticate(String address, String username, Supplier<T> authentication) {
        String ipKey = "ip:" + address;
        String accountKey = "account:" + address + ":" + username.trim().toLowerCase(Locale.ROOT);
        Window ip;
        Window account;
        synchronized (windows) {
            long now = now();
            ip = window(ipKey, now);
            check(ip, ipLimit, now);
            account = window(accountKey, now);
            check(account, accountLimit, now);
            // 将正在验证的请求也计入名额，避免并发请求同时通过限频检查。
            ip.pending++;
            account.pending++;
        }
        boolean success = false;
        boolean invalidCredentials = false;
        try {
            T result = authentication.get();
            success = true;
            return result;
        } catch (ApiStatusException exception) {
            invalidCredentials = exception.getStatus() == HttpStatus.UNAUTHORIZED;
            throw exception;
        } finally {
            synchronized (windows) {
                ip.pending--;
                account.pending--;
                if (invalidCredentials) {
                    resetExpired(ip, now());
                    resetExpired(account, now());
                    ip.failures++;
                    account.failures++;
                } else if (success) {
                    account.failures = 0;
                    // 成功登录只清除对应账号/IP的失败记录，不清除该IP对其他账号的失败次数。
                }
                removeEmpty(ipKey, ip);
                removeEmpty(accountKey, account);
            }
        }
    }

    private Window window(String key, long now) {
        Window window = windows.get(key);
        if (window == null) {
            if (windows.size() >= MAX_KEYS) {
                windows.entrySet().removeIf(entry -> entry.getValue().pending == 0 && entry.getValue().expiresAt <= now);
                if (windows.size() >= MAX_KEYS) {
                    throw new ApiStatusException(HttpStatus.TOO_MANY_REQUESTS, "登录请求较多，请在 60 秒后重试", 60);
                }
            }
            window = new Window();
            windows.put(key, window);
        }
        resetExpired(window, now);
        return window;
    }

    private void resetExpired(Window window, long now) {
        if (window.expiresAt <= now) {
            window.failures = 0;
            window.expiresAt = now + windowMillis;
        }
    }

    private void check(Window window, int limit, long now) {
        if (window.failures + window.pending < limit) return;
        int seconds = window.failures >= limit
                ? (int) Math.max(1, (window.expiresAt - now + 999) / 1000) : 1;
        throw new ApiStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "登录尝试过于频繁，请在 " + seconds + " 秒后重试", seconds);
    }

    private void removeEmpty(String key, Window window) {
        if (window.pending == 0 && window.failures == 0) windows.remove(key);
    }

    private long now() { return TimeUnit.NANOSECONDS.toMillis(System.nanoTime()); }

    private static final class Window {
        private long expiresAt = Long.MIN_VALUE;
        private int failures;
        private int pending;
    }
}
