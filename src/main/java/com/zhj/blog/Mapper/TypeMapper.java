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
    /** 与文章保存共用行锁，避免无外键模式下删除和建立关联并发穿透。 */
    @Select("SELECT id, type_name FROM type WHERE id = #{id} FOR UPDATE")
    Type lockById(@Param("id") Long id);

    /** 按已发布文章统计，无需修改现有 type_count 视图。 */
    @Select("SELECT CAST(t.id AS CHAR) AS id, t.type_name, COUNT(*) AS blog_count " +
            "FROM type t JOIN blog_table b ON b.type_id = t.id WHERE b.publish = 1 " +
            "GROUP BY t.id, t.type_name ORDER BY blog_count DESC, t.id DESC LIMIT #{limit}")
    List<HomeDto.TypeCountDto> selectPublishedTopTypes(@Param("limit") int limit);

    @Select("SELECT CAST(t.id AS CHAR) AS id, t.type_name, COUNT(*) AS blog_count " +
            "FROM type t JOIN blog_table b ON b.type_id = t.id WHERE b.publish = 1 " +
            "GROUP BY t.id, t.type_name ORDER BY blog_count DESC, t.id DESC")
    List<HomeDto.TypeCountDto> selectPublishedTypes();

    @Select("SELECT t.id, t.type_name FROM type t JOIN blog_table b ON b.type_id = t.id " +
            "WHERE b.publish = 1 GROUP BY t.id, t.type_name ORDER BY COUNT(*) DESC, t.id DESC LIMIT #{num}")
    List<Type> getTopTypeList(@Param("num") Integer num);
}
