package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhj.blog.Pojo.Notice;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:16
 **/
public interface NoticeService extends IService<Notice> {
    // 获取最新的公告
    Notice getLatestNotice();
}
