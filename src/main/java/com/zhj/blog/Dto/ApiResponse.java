package com.zhj.blog.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 新 API 的统一响应；业务 ID 在各 DTO 中使用字符串。 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final int code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
