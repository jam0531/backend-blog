package com.zhj.blog.Dto.auth;

import com.zhj.blog.Pojo.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 会话响应只包含页面必需字段，不序列化账号实体。 */
@Getter
@AllArgsConstructor
public class SessionUserDto {
    private final String id;
    private final String aliasname;
    private final String avatar;

    public static SessionUserDto from(User user) {
        return new SessionUserDto(user.getId().toString(), user.getAliasname(), user.getAvatar());
    }
}
