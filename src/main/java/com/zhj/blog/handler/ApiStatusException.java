package com.zhj.blog.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 仅用于服务端定义的业务提示，不能传入 SQL、密码或内部异常文本。 */
@Getter
public class ApiStatusException extends RuntimeException {
    private final HttpStatus status;

    public ApiStatusException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
