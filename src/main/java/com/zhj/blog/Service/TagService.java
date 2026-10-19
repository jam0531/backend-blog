package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhj.blog.Pojo.Tag;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:20
 **/
public interface TagService extends IService<Tag> {
    void deleteForAdmin(Long id);
}
