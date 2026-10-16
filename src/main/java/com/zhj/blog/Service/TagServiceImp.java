package com.zhj.blog.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Mapper.TagMapper;
import com.zhj.blog.Pojo.Tag;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:20
 **/
@Service
public class TagServiceImp extends ServiceImpl<TagMapper, Tag> implements TagService {
    @Override
    public List<Tag> TopTagList(Integer size) {
        return null;
    }
}
