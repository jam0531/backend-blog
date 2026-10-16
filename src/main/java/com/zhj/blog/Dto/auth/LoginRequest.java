package com.zhj.blog.Dto.auth;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class LoginRequest {
    @NotBlank @Size(max = 255)
    private String username;
    @NotBlank @Size(max = 255)
    private String password;
}
