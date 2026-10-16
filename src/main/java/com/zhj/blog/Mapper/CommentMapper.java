package com.zhj.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhj.blog.Pojo.Blog;
import com.zhj.blog.Pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author:zouhuajun
 * @date:2022/7/10 0:55
 **/
@Repository
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
