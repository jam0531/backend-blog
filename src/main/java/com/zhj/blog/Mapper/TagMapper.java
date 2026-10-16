package com.zhj.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhj.blog.Pojo.Tag;
import com.zhj.blog.Dto.blog.TagCountDto;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author:zouhuajun
 * @date:2022/7/10 0:57
 **/
@Repository
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    @Select("SELECT id, tag_name FROM tag WHERE id = #{id} FOR UPDATE")
    Tag lockById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM blog_table_tags WHERE tags_id = #{id}")
    long countBlogLinks(@Param("id") Long id);

    @Select("SELECT CAST(t.id AS CHAR) AS id, t.tag_name, COUNT(DISTINCT b.id) AS blog_count " +
            "FROM tag t JOIN blog_table_tags bt ON bt.tags_id = t.id " +
            "JOIN blog_table b ON b.id = bt.blogs_id WHERE b.publish = 1 " +
            "GROUP BY t.id, t.tag_name ORDER BY blog_count DESC, t.id DESC")
    List<TagCountDto> selectPublishedTags();
}
