package com.zhj.blog.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.blog.Mapper.BlogMapper;
import com.zhj.blog.Mapper.TypeMapper;
import com.zhj.blog.Pojo.Blog;
import com.zhj.blog.Pojo.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import com.zhj.blog.NotFoundException;
import com.zhj.blog.handler.ApiStatusException;

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
    @Transactional
    public void deleteForAdmin(Long id) {
        if (typeMapper.lockById(id) == null) throw new NotFoundException("分类不存在");
        if (blogMapper.selectCount(new QueryWrapper<Blog>().eq("type_id", id)) > 0) {
            throw new ApiStatusException(HttpStatus.CONFLICT, "该分类仍被文章使用，请先调整文章分类");
        }
        typeMapper.deleteById(id);
    }

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
        // 旧首页通过 blogs 的长度显示数量，与分类排序统一为已发布文章口径。
        List<Blog> blogs = blogMapper.selectList(new QueryWrapper<Blog>()
                .select("id", "type_id").eq("publish", 1).in("type_id", typesById.keySet()));
        for (Blog blog : blogs) {
            typesById.get(blog.getTypeId()).getBlogs().add(blog);
        }
        return types;
    }
}
