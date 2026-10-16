package com.zhj.blog.Dto.blog;

import lombok.Data;

@Data
public class TagCountDto {
    private String id;
    private String tagName;
    private long blogCount;
}
