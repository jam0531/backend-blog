package com.zhj.blog.Dto.blog;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/** 公开文章查询白名单；业务 ID 由 Spring 从十进制字符串绑定为 Long。 */
@Data
public class BlogQuery {
    @Min(1)
    private int page = 1;
    @Min(1)
    @Max(100)
    private int size = 10;
    @Min(1)
    private Long typeId;
    @Min(1)
    private Long tagId;
    @Min(1000)
    @Max(9999)
    private Integer year;
    @Size(max = 255)
    private String keyword;
}
