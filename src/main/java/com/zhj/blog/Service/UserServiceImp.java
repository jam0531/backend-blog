package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Mapper.UserMapper;
import com.zhj.blog.Pojo.User;
import org.springframework.stereotype.Service;

/**
 * @author:zouhuajun
 * @date:2022/7/10 14:57
 **/
@Service
public class UserServiceImp extends ServiceImpl<UserMapper, User> implements UserService {
}
