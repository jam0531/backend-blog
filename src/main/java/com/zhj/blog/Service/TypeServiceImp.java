package com.zhj.blog.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Mapper.BlogMapper;
import com.zhj.blog.Mapper.TypeMapper;
import com.zhj.blog.Pojo.Blog;
import com.zhj.blog.Pojo.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * @author:zouhuajun
 * @date:2022/7/10 15:27
 **/
@Service
public class TypeServiceImp extends ServiceImpl<TypeMapper, Type> implements TypeService {
    @Autowired
    private TypeMapper typeMapper;

    @Autowired
    private BlogMapper blogMapper;

    @Override
    public List<Type> TypeTopList(Integer size) {
        List<Type> types = typeMapper.getTopTypeList(size);
        if (types.isEmpty()) {
            return types;
        }
        Map<Long, Type> typesById = new HashMap<>();
        for (Type type : types) {
            type.setBlogs(new ArrayList<>());
            typesById.put(type.getId(), type);
        }
        // 首页通过 blogs 的长度显示数量，沿用 type_count 统计全部文章的口径。
        List<Blog> blogs = blogMapper.selectList(new QueryWrapper<Blog>()
                .select("id", "type_id").in("type_id", typesById.keySet()));
        for (Blog blog : blogs) {
            typesById.get(blog.getTypeId()).getBlogs().add(blog);
        }
        return types;
    }
}
