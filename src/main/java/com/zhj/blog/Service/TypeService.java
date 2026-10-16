package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhj.blog.Pojo.Type;

import java.util.List;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:20
 **/
public interface TypeService extends IService<Type> {
    void deleteForAdmin(Long id);
    // 获取
    List<Type> TypeTopList(Integer size);
}
