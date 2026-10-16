package com.zhj.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhj.blog.Pojo.Picture;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author:zouhuajun
 * @date:2022/7/10 0:57
 **/
@Repository
@Mapper
public interface PictureMapper extends BaseMapper<Picture> {
    @Select("select * from picture order by rand() limit #{num}")
    Picture getByRandom(@Param("num") int num);
}
