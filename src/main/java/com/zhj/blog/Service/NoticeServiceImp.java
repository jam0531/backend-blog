package com.zhj.blog.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Mapper.NoticeMapper;
import com.zhj.blog.Pojo.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:17
 **/
@Service
public class NoticeServiceImp extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {
    @Autowired
    private NoticeMapper mapper;

    // 获取最新的公告
    @Override
    public Notice getLatestNotice() {
        return mapper.selectOne(new QueryWrapper<Notice>().orderByDesc("create_time").last("limit 1"));
    }
}
