package com.zhj.blog;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.blog.Mapper.BlogMapper;
import com.zhj.blog.Mapper.TypeMapper;
import com.zhj.blog.Mapper.UserMapper;
import com.zhj.blog.Pojo.Blog;
import com.zhj.blog.Pojo.User;
import com.zhj.blog.Service.BlogService;
import com.zhj.blog.Service.NoticeService;
import com.zhj.blog.Utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@SpringBootTest
class BlogApplicationTests {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BlogService blogService;
    @Autowired
    private BlogMapper blogMapper;
    @Autowired
    private TypeMapper typeMapper;

    @Autowired
    private RedisUtil redisUtil;


    @Autowired
    private NoticeService noticeService;


    @Test
    void contextLoads() {
    }

    @Test
    public void test(){
        Page page = new Page();
        System.out.println(blogService.PublishedBlogList(page).getRecords());
    }

}
