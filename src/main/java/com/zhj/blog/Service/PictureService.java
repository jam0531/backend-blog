package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhj.blog.Pojo.Picture;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.resource.ResourceDto.PictureItem;
import com.zhj.blog.Dto.resource.ResourceDto.PictureQuery;
import com.zhj.blog.Dto.resource.ResourceDto.PictureArchive;
import java.util.List;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:18
 **/
public interface PictureService extends IService<Picture> {
    PageResult<PictureItem> getPicturePage(PictureQuery query);
    List<PictureArchive> getArchives();
    Picture getRandomBackground(Long excludeId);
    public Picture getRandomPic(int num);
}
