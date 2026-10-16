package com.zhj.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhj.blog.Pojo.Blog;
import com.zhj.blog.Pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author:zouhuajun
 * @date:2022/7/10 0:56
 **/
@Repository
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    /** 同一留言树的回复与删除锁定根记录，避免删除后又产生孤立回复。 */
    @org.apache.ibatis.annotations.Select("SELECT * FROM message WHERE id = #{id} FOR UPDATE")
    Message lockById(@org.apache.ibatis.annotations.Param("id") Long id);
}
