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
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date, COUNT(*) AS picture_count " +
            "FROM picture WHERE create_time IS NOT NULL GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') ORDER BY date DESC")
    java.util.List<com.zhj.blog.Dto.resource.ResourceDto.PictureArchive> selectArchives();

    @Select({"<script>", "SELECT id, pic_name, piclink FROM picture",
            "<if test='excludeId != null'>WHERE id != #{excludeId}</if>",
            "ORDER BY RAND() LIMIT 1", "</script>"})
    Picture selectRandomBackground(@Param("excludeId") Long excludeId);

    @Select("select * from picture order by rand() limit #{num}")
    Picture getByRandom(@Param("num") int num);
}
