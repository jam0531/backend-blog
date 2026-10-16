package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Mapper.UserMapper;
import com.zhj.blog.Pojo.User;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhj.blog.handler.ApiStatusException;
import org.springframework.http.HttpStatus;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

/**
 * @author:zouhuajun
 * @date:2022/7/10 14:57
 **/
@Service
public class UserServiceImp extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public User authenticateAdmin(String username, String password) {
        List<User> matches = baseMapper.selectList(new QueryWrapper<User>()
                .select("id", "aliasname", "avatar", "password", "type")
                .eq("username", username.trim()).last("LIMIT 2"));
        User user = matches.size() == 1 ? matches.get(0) : null;
        // 用户已确认旧库为无盐 MD5；只兼容校验，不迁移或重写密码。
        String stored = user == null || user.getPassword() == null ? "" : user.getPassword().toLowerCase(Locale.ROOT);
        boolean valid = MessageDigest.isEqual(md5(password).getBytes(StandardCharsets.US_ASCII),
                stored.getBytes(StandardCharsets.US_ASCII));
        if (!valid || user == null || !Integer.valueOf(1).equals(user.getType())) {
            throw new ApiStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误，或账号没有管理权限");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public User findSessionUser(Long id) {
        return baseMapper.selectOne(new QueryWrapper<User>().select("id", "aliasname", "avatar", "type").eq("id", id));
    }

    private String md5(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(32);
            for (byte b : bytes) {
                hex.append(Character.forDigit((b >>> 4) & 15, 16)).append(Character.forDigit(b & 15, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("MD5 unavailable", exception);
        }
    }
}
