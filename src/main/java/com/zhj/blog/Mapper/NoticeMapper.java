package com.zhj.blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhj.blog.Pojo.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author:zouhuajun
 * @date:2022/7/10 0:57
 **/
@Repository
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
