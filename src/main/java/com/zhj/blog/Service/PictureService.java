package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhj.blog.Pojo.Picture;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:18
 **/
public interface PictureService extends IService<Picture> {
    public Picture getRandomPic(int num);
}
