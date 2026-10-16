package com.zhj.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhj.blog.Pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author:zouhuajun
 * @date:2022/7/10 0:53
 **/
@Repository
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
