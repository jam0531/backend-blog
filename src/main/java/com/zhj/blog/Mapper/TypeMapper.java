package com.zhj.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhj.blog.Pojo.Type;
import com.zhj.blog.Dto.home.HomeDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author:zouhuajun
 * @date:2022/7/10 0:58
 **/
@Repository
@Mapper
public interface TypeMapper extends BaseMapper<Type> {
    /** API 单独统计已发布文章，不改动旧页面依赖的 type_count 视图。 */
    @Select("SELECT CAST(t.id AS CHAR) AS id, t.type_name, COUNT(*) AS blog_count " +
            "FROM type t JOIN blog_table b ON b.type_id = t.id WHERE b.publish = 1 " +
            "GROUP BY t.id, t.type_name ORDER BY blog_count DESC, t.id DESC LIMIT #{limit}")
    List<HomeDto.TypeCountDto> selectPublishedTopTypes(@Param("limit") int limit);

    @Select("SELECT type.id, type_name from type LEFT JOIN type_count on type.id = type_count.type_id ORDER BY count DESC limit #{num};")
    List<Type> getTopTypeList(@Param("num") Integer num);
}
