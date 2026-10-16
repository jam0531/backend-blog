package com.zhj.blog.Service;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:08
 **/
public interface UserService {
    com.zhj.blog.Pojo.User authenticateAdmin(String username, String password);
    com.zhj.blog.Pojo.User findSessionUser(Long id);
}
