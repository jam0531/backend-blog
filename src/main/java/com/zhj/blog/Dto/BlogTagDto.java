package com.zhj.blog.Dto;

import lombok.Data;

/**
 * 文章标签关联查询结果，blogId 用于将标签归入对应文章。
 */
@Data
public class BlogTagDto {
    private Long blogId;
    private Long tagId;
    private String tagName;
}
