package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Mapper.TagMapper;
import com.zhj.blog.Pojo.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import com.zhj.blog.NotFoundException;
import com.zhj.blog.handler.ApiStatusException;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:20
 **/
@Service
public class TagServiceImp extends ServiceImpl<TagMapper, Tag> implements TagService {
    @Override
    @Transactional
    public void deleteForAdmin(Long id) {
        if (baseMapper.lockById(id) == null) throw new NotFoundException("标签不存在");
        if (baseMapper.countBlogLinks(id) > 0) {
            throw new ApiStatusException(HttpStatus.CONFLICT, "该标签仍被文章使用，请先调整文章标签");
        }
        baseMapper.deleteById(id);
    }
}
