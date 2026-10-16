package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Mapper.CommentMapper;
import com.zhj.blog.Pojo.Comment;
import org.springframework.stereotype.Service;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:13
 **/
@Service
public class CommentServiceImp extends ServiceImpl<CommentMapper, Comment> implements CommentService {
}
