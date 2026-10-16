package com.zhj.blog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author:zouhuajun
 * @date:2021/12/12 22:11
 **/
@ResponseStatus(HttpStatus.NOT_FOUND )
public class NotFoundException extends RuntimeException{
    public NotFoundException() {

    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
