package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Mapper.PictureMapper;
import com.zhj.blog.Pojo.Picture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:19
 **/
@Service
public class PictureServiceImp extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Autowired
    private PictureMapper pictureMapper;

    // 随机获得一张图片
    @Override
    public Picture getRandomPic(int num) {
        return pictureMapper.getByRandom(1);
    }

}
