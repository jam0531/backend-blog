package com.zhj.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhj.blog.Dto.BlogTagDto;
import com.zhj.blog.Dto.home.HomeDto;
import com.zhj.blog.Pojo.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author:zouhuajun
 * @date:2022/7/10 0:55
 **/
@Repository
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
    /** 原子计数，仅更新阅读量，不触碰正文、更新时间或任何关联字段。 */
    @Update("UPDATE blog_table SET views = COALESCE(views, 0) + 1 WHERE id = #{id} AND publish = 1")
    int incrementPublishedViews(@Param("id") Long id);

    @Select("SELECT COALESCE(views, 0) FROM blog_table WHERE id = #{id} AND publish = 1")
    Integer selectPublishedViews(@Param("id") Long id);

    /** 公开首页统计，所有文章来源统一限定 publish = 1。 */
    @Select("SELECT (SELECT COUNT(*) FROM blog_table WHERE publish = 1) AS blog_count, " +
            "(SELECT COUNT(DISTINCT t.id) FROM type t JOIN blog_table b ON b.type_id = t.id " +
            "WHERE b.publish = 1) AS type_count, " +
            "(SELECT COUNT(DISTINCT t.id) FROM tag t JOIN blog_table_tags bt ON bt.tags_id = t.id " +
            "JOIN blog_table b ON b.id = bt.blogs_id WHERE b.publish = 1) AS tag_count")
    HomeDto.StatsDto selectPublishedStats();

    @Select("SELECT YEAR(create_time) AS year, COUNT(*) AS blog_count FROM blog_table " +
            "WHERE publish = 1 AND create_time IS NOT NULL GROUP BY YEAR(create_time) ORDER BY year DESC")
    List<HomeDto.ArchiveDto> selectPublishedArchives();

    /**
     * 批量查询文章的标签；调用方需保证文章 ID 集合非空。
     */
    @Select({"<script>",
            "SELECT bt.blogs_id AS blog_id, t.id AS tag_id, t.tag_name",
            "FROM blog_table_tags bt JOIN tag t ON t.id = bt.tags_id",
            "WHERE bt.blogs_id IN",
            "<foreach collection='blogIds' item='blogId' open='(' separator=',' close=')'>",
            "#{blogId}",
            "</foreach>",
            "ORDER BY bt.blogs_id, t.id",
            "</script>"})
    List<BlogTagDto> selectTagsByBlogIds(@Param("blogIds") List<Long> blogIds);
}
