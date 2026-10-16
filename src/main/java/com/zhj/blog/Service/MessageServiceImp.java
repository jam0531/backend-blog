package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Mapper.MessageMapper;
import com.zhj.blog.Pojo.Message;
import org.springframework.stereotype.Service;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:15
 **/
@Service
public class MessageServiceImp extends ServiceImpl<MessageMapper, Message> implements MessageService {
}
