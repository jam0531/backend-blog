package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Mapper.PictureMapper;
import com.zhj.blog.Pojo.Picture;
import com.zhj.blog.Dto.PageResult;
import com.zhj.blog.Dto.resource.ResourceDto;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
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

    @Override
    public PageResult<ResourceDto.PictureItem> getPicturePage(ResourceDto.PictureQuery query) {
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        LocalDate date = query.getDate();
        if (date != null) {
            if (date.getYear() < 1000 || date.getYear() > 9999) throw new IllegalArgumentException("日期超出数据库支持范围");
            // create_time 为无时区 DATETIME；按原记录的日历日期筛选，不在接口转换时区。
            wrapper.ge("create_time", date.atStartOfDay());
            if (!date.equals(LocalDate.of(9999, 12, 31))) wrapper.lt("create_time", date.plusDays(1).atStartOfDay());
        }
        Page<Picture> page = pictureMapper.selectPage(new Page<>(query.getPage(), query.getSize()), wrapper.orderByDesc("create_time", "id"));
        return new PageResult<>(page.getRecords().stream().map(ResourceDto.PictureItem::from).collect(Collectors.toList()),
                page.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    public List<ResourceDto.PictureArchive> getArchives() { return pictureMapper.selectArchives(); }

    @Override
    public Picture getRandomBackground(Long excludeId) {
        Picture picture = pictureMapper.selectRandomBackground(excludeId);
        // 只有一张图片时保留该图；不因排除当前图而清空欢迎页。
        return picture == null && excludeId != null ? pictureMapper.selectRandomBackground(null) : picture;
    }

    // 随机获得一张图片
    @Override
    public Picture getRandomPic(int num) {
        return pictureMapper.getByRandom(1);
    }

}
